package eu.ebrains.kg.search.controller.labels;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import eu.ebrains.kg.search.utils.MetaModelUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

class LabelsTest {
    ObjectMapper mapper = new ObjectMapper().configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

    @Test
    public void testLabels() {
        try {
            LabelsController labels = new LabelsController(new MetaModelUtils());
            Map<String, Object> labelsResult = labels.generateLabels();
            Path path = Paths.get(LabelsController.class.getClassLoader().getResource("labels_result.json").toURI());
            String json = Files.lines(path).collect(Collectors.joining("\n"));
            Map expected = mapper.readValue(json, Map.class);
            Assertions.assertEquals(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(expected), mapper.writerWithDefaultPrettyPrinter().writeValueAsString(labelsResult));
        } catch (URISyntaxException | IOException e) {
            Assertions.fail(e.getMessage());
        }


    }

}