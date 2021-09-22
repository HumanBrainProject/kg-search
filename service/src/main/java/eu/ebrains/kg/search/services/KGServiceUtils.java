package eu.ebrains.kg.search.services;

import eu.ebrains.kg.search.configuration.OauthClient;
import eu.ebrains.kg.search.model.ErrorReport;
import eu.ebrains.kg.search.model.source.ResultsOfKG;
import eu.ebrains.kg.search.model.source.SourceInstanceV1andV2;

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
                if(instance instanceof SourceInstanceV1andV2){
                    final String identifier = ((SourceInstanceV1andV2) instance).getIdentifier();
                    if(resultsOfKG.getErrors()==null){
                        resultsOfKG.setErrors(new ErrorReport());
                    }
                    resultsOfKG.getErrors().put(identifier, errorMap.get(index));
                }
            }
        }
    }

}
