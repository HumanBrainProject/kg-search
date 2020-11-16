package eu.ebrains.kg.search.controller.kg;

import eu.ebrains.kg.search.configuration.Setup;
import eu.ebrains.kg.search.model.source.ResultOfKGv2;
import org.springframework.stereotype.Component;

@Component
public class KGv2 {

    private final Setup setup;

    public KGv2(Setup setup) {
        this.setup = setup;
    }

    public <T> ResultOfKGv2<T> fetchInstances(Class<T> clazz){
        String endpoint = setup.getEndpointForType(clazz.getSimpleName());

        return null;
    }



}
