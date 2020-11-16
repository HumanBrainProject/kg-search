package eu.ebrains.kg.search.configuration;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Setup {

    private final Map<String, String> typeToEndpoint = new HashMap<>();

    @PostConstruct
    public void uploadQueries(){
        System.out.println("Upload the queries please");
    }

    public String getEndpointForType(String type){
        return typeToEndpoint.get(type);
    }

}
