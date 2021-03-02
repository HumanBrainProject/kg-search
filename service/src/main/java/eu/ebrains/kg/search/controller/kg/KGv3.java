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

    public <T> T executeQueryForIndexing(Class<T> clazz, String queryId, DataStage dataStage){
        return kgServiceClient.executeQueryForIndexing(queryId, dataStage, clazz);
    }

    public <T> T executeQueryForLive(Class<T> clazz, String queryId, String id, DataStage dataStage) {
        return  kgServiceClient.executeQueryForLiveMode(queryId, id, dataStage, clazz);
    }

    public Map fetchInstanceForLive(String id, DataStage dataStage) {
        return  kgServiceClient.getInstanceForLiveMode(id, dataStage);
    }

}
