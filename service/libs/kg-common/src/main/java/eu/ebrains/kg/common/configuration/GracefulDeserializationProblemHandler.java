package eu.ebrains.kg.common.configuration;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import eu.ebrains.kg.common.model.ErrorReport;
import eu.ebrains.kg.common.model.source.ResultsOfKG;
import eu.ebrains.kg.common.model.source.SourceInstanceV1andV2;
import eu.ebrains.kg.common.model.source.openMINDSv3.SourceInstanceV3;
import eu.ebrains.kg.common.utils.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GracefulDeserializationProblemHandler extends DeserializationProblemHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    public static ThreadLocal<Map<Integer, List<String>>> ERROR_REPORTING_THREAD_LOCAL = new ThreadLocal<>();

    private void recordError( JsonParser p, Type targetType){
        final int rootObjectIndex = p.getParsingContext().pathAsPointer().tail().getMatchingIndex();
        final String path = p.getParsingContext().pathAsPointer().tail().tail().toString();
        if(ERROR_REPORTING_THREAD_LOCAL.get()==null){
            ERROR_REPORTING_THREAD_LOCAL.set(new HashMap<>());
        }
        final String error = String.format("Was not able to parse %s - target type is %s", path, targetType.getTypeName());
        logger.error(error);
        ERROR_REPORTING_THREAD_LOCAL.get().computeIfAbsent(rootObjectIndex, k -> new ArrayList<>());
        ERROR_REPORTING_THREAD_LOCAL.get().get(rootObjectIndex).add(error);
        //We need to make sure we jump over to the next step, this is why we read the value as a tree
        try{
            p.readValueAsTree();
        }
        catch (IOException e){
            logger.error("Was not able to read", e);
        }
    }

    @Override
    public Object handleUnexpectedToken(DeserializationContext ctxt, JavaType targetType, JsonToken t, JsonParser p, String failureMsg) throws IOException {
        recordError(p, targetType);
        return null;
    }


    @Override
    public Object handleWeirdNumberValue(DeserializationContext ctxt, Class<?> targetType, Number valueToConvert, String failureMsg) throws IOException {
        recordError(ctxt.getParser(), targetType);
        return null;
    }

    @Override
    public Object handleWeirdStringValue(DeserializationContext ctxt, Class<?> targetType, String valueToConvert, String failureMsg) throws IOException {
        recordError(ctxt.getParser(), targetType);
        return null;
    }


    @Override
    public Object handleWeirdNativeValue(DeserializationContext ctxt, JavaType targetType, Object valueToConvert, JsonParser p) throws IOException {
        recordError(ctxt.getParser(), targetType);
        return null;
    }

    public static <T> void parsingErrorHandler(T result){
        final Map<Integer, List<String>> errorMap = GracefulDeserializationProblemHandler.ERROR_REPORTING_THREAD_LOCAL.get();
        if(errorMap!=null && result instanceof ResultsOfKG){
            final ResultsOfKG<?> resultsOfKG = (ResultsOfKG<?>) result;
            final List<?> data = resultsOfKG.getData();
            for (Integer index : errorMap.keySet()) {
                final Object instance = data.get(index);
                String identifier;
                if(instance instanceof SourceInstanceV1andV2) {
                    identifier = ((SourceInstanceV1andV2) instance).getIdentifier();
                }
                else if (instance instanceof SourceInstanceV3){
                    identifier = IdUtils.getUUID(((SourceInstanceV3)instance).getId());
                }
                else{
                    throw new RuntimeException(String.format("Unexpected type in error handling: %s", instance.getClass().getName()));
                }
                if(identifier!=null) {
                    if (resultsOfKG.getErrors() == null) {
                        resultsOfKG.setErrors(new ErrorReport());
                    }
                    resultsOfKG.getErrors().put(identifier, errorMap.get(index));
                }
            }
        }
    }


}
