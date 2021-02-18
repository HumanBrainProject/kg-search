package eu.ebrains.kg.search.controller.kg;

import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.services.KGServiceClient;
import org.springframework.stereotype.Component;

@Component
public class KGv2 {
    private final KGServiceClient kgServiceClient;

    public KGv2(KGServiceClient kgServiceClient) {
        this.kgServiceClient = kgServiceClient;
    }

    public <T> T fetchInstances(Class<T> clazz, String query, String authorization, DataStage dataStage){
        return kgServiceClient.executeQuery(query, dataStage, clazz, authorization);
    }

    public <T> T fetchInstance(Class<T> clazz, String query, String id, String authorization, DataStage dataStage) {
        return  kgServiceClient.executeQuery(query, id, dataStage, clazz, authorization);
    }

    public <T> T fetchInstanceByIdentifier(Class<T> clazz, String query, String identifier, String authorization, DataStage dataStage) {
        return  kgServiceClient.executeQueryByIdentifier(query, identifier, dataStage, clazz, authorization);
    }

}
