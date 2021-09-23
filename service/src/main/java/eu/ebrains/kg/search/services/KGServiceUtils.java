package eu.ebrains.kg.search.services;

import eu.ebrains.kg.search.configuration.OauthClient;
import eu.ebrains.kg.search.model.ErrorReport;
import eu.ebrains.kg.search.model.source.ResultsOfKG;
import eu.ebrains.kg.search.model.source.SourceInstanceV1andV2;
import eu.ebrains.kg.search.model.source.openMINDSv3.SourceInstanceV3;

import java.util.List;
import java.util.Map;

public class KGServiceUtils {

    public static <T> void parsingErrorHandler(T result){
        final Map<Integer, List<String>> errorMap = OauthClient.ERROR_REPORTING_THREAD_LOCAL.get();
        if(errorMap!=null && result instanceof ResultsOfKG){
            final ResultsOfKG<?> resultsOfKG = (ResultsOfKG) result;
            final List<?> data = resultsOfKG.getData();
            for (Integer index : errorMap.keySet()) {
                final Object instance = data.get(index);
                String identifier;
                if(instance instanceof SourceInstanceV1andV2) {
                    identifier = ((SourceInstanceV1andV2) instance).getIdentifier();
                }
                else if (instance instanceof SourceInstanceV3){
                    identifier = ((SourceInstanceV3)instance).getId();
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
