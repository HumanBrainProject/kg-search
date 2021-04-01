package eu.ebrains.kg.search.model.source.commonsV1andV2;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ListOrSingleStringAsListDeserializer extends JsonDeserializer<List> {

    @Override
    public List<?> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        try {
            String s = jsonParser.readValueAs(String.class);
            if(StringUtils.isNotBlank(s)) {
                return Collections.singletonList(s);
            }
            return null;
        } catch (MismatchedInputException e) {
            List<?> list = jsonParser.readValueAs(List.class);
            if(CollectionUtils.isEmpty(list)){
                return null;
            }
            else {
                return list;
            }
        }
    }
}