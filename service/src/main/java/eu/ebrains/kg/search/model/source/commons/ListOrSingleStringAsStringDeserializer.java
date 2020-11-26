package eu.ebrains.kg.search.model.source.commons;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ListOrSingleStringAsStringDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        try {
            return jsonParser.readValueAs(String.class);
        } catch (MismatchedInputException e) {
            List<?> list = jsonParser.readValueAs(List.class);
            if(CollectionUtils.isEmpty(list)){
                return null;
            }
            else {
                return list.stream().map(Object::toString).collect(Collectors.joining(", "));
            }
        }
    }
}