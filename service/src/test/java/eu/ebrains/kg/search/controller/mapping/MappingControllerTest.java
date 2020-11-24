package eu.ebrains.kg.search.controller.mapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import eu.ebrains.kg.search.utils.MetaModelUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.junit.Ignore;
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
    @Ignore("Not working because of different data states - more complex assertions needed")
    void generateMapping() throws IOException, URISyntaxException {
        //Given
        Path path = Paths.get(MappingControllerTest.class.getClassLoader().getResource("mappings_result.json").toURI());
        String json = Files.lines(path).collect(Collectors.joining("\n"));
        ObjectMapper mapper = new ObjectMapper().configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        Map expected = mapper.readValue(json, Map.class);

        MappingController mappingController = new MappingController(new MetaModelUtils());

        //When
        Map<String, Object> mapping = mappingController.generateMapping();

        //Then
        Assertions.assertEquals(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(expected), mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapping));

    }
}