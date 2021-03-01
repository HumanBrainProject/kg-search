package eu.ebrains.kg.search.controller.kg;

import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.services.KGV3ServiceClient;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component
public class KGv3 {
    private final KGV3ServiceClient kgServiceClient;

    public KGv3(KGV3ServiceClient kgServiceClient) {
        this.kgServiceClient = kgServiceClient;
    }


    public <T> T fetchInstances(Class<T> clazz, String queryId, String authorization, DataStage dataStage){
        return kgServiceClient.executeQuery(queryId, dataStage, clazz, authorization);
    }


    public <T> T fetchInstance(Class<T> clazz, String queryId, String id, String authorization, DataStage dataStage) {
        return  kgServiceClient.executeQuery(queryId, id, dataStage, clazz, authorization);
    }

    public Map fetchInstance(String id, DataStage dataStage, String authorization) {
        return  kgServiceClient.getInstance(id, dataStage, authorization);
    }

}
