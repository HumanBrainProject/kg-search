package eu.ebrains.kg.search.controller.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public class JsonAdapter {

    private final ObjectMapper objectMapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public String toJson(Object object) {
        try {
            if(object == null){
                return null;
            }
            else if (object instanceof String){
                return (String)object;
            }
            return objectMapper.writer().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T fromJson(String payload, Class<T> clazz) {
        try {
            if(payload == null){
                return null;
            }
            return objectMapper.readValue(payload, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
