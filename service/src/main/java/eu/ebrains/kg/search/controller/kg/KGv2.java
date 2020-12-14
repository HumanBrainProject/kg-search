package eu.ebrains.kg.search.controller.kg;

import eu.ebrains.kg.search.model.DatabaseScope;
import eu.ebrains.kg.search.services.KGServiceClient;
import org.springframework.stereotype.Component;

@Component
public class KGv2 {
    private final KGServiceClient kgServiceClient;

    public KGv2(KGServiceClient kgServiceClient) {
        this.kgServiceClient = kgServiceClient;
    }

    public <T> T fetchInstances(Class<T> clazz, String query, String authorization, DatabaseScope databaseScope){
        return kgServiceClient.executeQuery(query, databaseScope, clazz, authorization);
    }

    public <T> T fetchInstance(Class<T> clazz, String query, String id, String authorization, DatabaseScope databaseScope) {
        return  kgServiceClient.executeQuery(query, id, databaseScope, clazz, authorization);
    }

    public <T> T fetchInstanceByIdentifier(Class<T> clazz, String query, String identifier, String authorization, DatabaseScope databaseScope) {
        return  kgServiceClient.executeQueryByIdentifier(query, identifier, databaseScope, clazz, authorization);
    }

}
