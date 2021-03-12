package eu.ebrains.kg.search.controller.mapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import eu.ebrains.kg.search.controller.Constants;
import eu.ebrains.kg.search.utils.MetaModelUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

class MappingControllerTest {

    @Test
    void generateMapping() {
        //Given
        ObjectMapper mapper = new ObjectMapper().configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        MappingController mappingController = new MappingController(new MetaModelUtils());

        //When
        Constants.TARGET_MODELS_MAP.forEach((type, clazz) -> {
            System.out.printf("Now handling type: %s%n", type);
            Path path;
            String json;
            try {
                path = Paths.get(MappingControllerTest.class.getClassLoader().getResource(String.format("mappings/%sMapping.json", type.toLowerCase())).toURI());
                json = Files.lines(path).collect(Collectors.joining("\n"));
                Map expected = mapper.readValue(json, Map.class);
                Map<String, Object> mapping = mappingController.generateMapping(clazz);
                //Then
                Assertions.assertEquals(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(expected), mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapping));
            } catch (URISyntaxException | IOException e) {
                Assert.fail(e.getMessage());
            }
        });
    }
}