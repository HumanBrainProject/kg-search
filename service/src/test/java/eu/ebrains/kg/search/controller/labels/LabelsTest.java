package eu.ebrains.kg.search.controller.labels;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Contributor;
import eu.ebrains.kg.search.utils.MetaModelUtils;
import org.junit.jupiter.api.Test;

import java.util.Map;

class LabelsTest {

    @Test
    public void testLabels() throws JsonProcessingException {
        LabelsController labels = new LabelsController(new MetaModelUtils());
        Map<String, Object> result = labels.generateLabels();
        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(result));

    }

}