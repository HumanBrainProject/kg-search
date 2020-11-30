package eu.ebrains.kg.search.controller.kg;

import eu.ebrains.kg.search.configuration.Setup;
import eu.ebrains.kg.search.model.DatabaseScope;
import eu.ebrains.kg.search.model.source.ResultOfKGv2;
import eu.ebrains.kg.search.services.ServiceClient;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import javax.xml.crypto.Data;

@Component
public class KGv2 {
    private final ServiceClient serviceClient;

    public KGv2(ServiceClient serviceClient) {
        this.serviceClient = serviceClient;
    }

    public <T> ResultOfKGv2<T> fetchInstances(Class<T> clazz){
        return null;
    }

    public <T> T fetchInstance(Class<T> clazz, String query, String id, String authorization, DatabaseScope databaseScope) {
        return  serviceClient.executeQuery(query, id, databaseScope, clazz, authorization);
    }



}
