package eu.ebrains.kg.search.controller.data;

import eu.ebrains.kg.search.controller.translators.ModelTranslator;
import eu.ebrains.kg.search.controller.translators.SoftwareTranslator;
import eu.ebrains.kg.search.controller.utils.JsonAdapter;
import eu.ebrains.kg.search.model.DatabaseScope;
import eu.ebrains.kg.search.model.source.openMINDSv2.ModelV2;
import eu.ebrains.kg.search.model.source.openMINDSv2.SoftwareV2;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Model;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Software;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TranslatorTest {

    JsonAdapter jsonAdapter = new JsonAdapter();

    @Test
    public void compareReleasedModel() throws IOException {
        ModelTranslator translator = new ModelTranslator();
        // Given
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/modelReleasedSource.json"), StandardCharsets.UTF_8);
        ModelV2 source = jsonAdapter.fromJson(sourceJson, ModelV2.class);

        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/modelReleasedTarget.json"), StandardCharsets.UTF_8);
        Map<String, Object> targetExpected = jsonAdapter.fromJson(expectedJson, Map.class);

        //When
        Model target = translator.translate(source, DatabaseScope.RELEASED, false);
        String targetJson = jsonAdapter.toJson(target);
        Map<String, Object> targetResult = jsonAdapter.fromJson(targetJson, Map.class);

        //Then
        compareResults(targetExpected, targetResult);
    }

    @Test
    public void compareReleasedSoftware() throws IOException {
        SoftwareTranslator translator = new SoftwareTranslator();
        // Given
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/softwareReleasedSource.json"), StandardCharsets.UTF_8);
        SoftwareV2 source = jsonAdapter.fromJson(sourceJson, SoftwareV2.class);

        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/softwareReleasedTarget.json"), StandardCharsets.UTF_8);
        Map<String, Object> targetExpected = jsonAdapter.fromJson(expectedJson, Map.class);

        //When
        Software target = translator.translate(source, DatabaseScope.RELEASED, false);
        String targetJson = jsonAdapter.toJson(target);
        Map<String, Object> targetResult = jsonAdapter.fromJson(targetJson, Map.class);

        //Then
        compareResults(targetExpected, targetResult);
    }

    private void compareResults(Map<String, Object> targetExpected, Map<String, Object> targetResult) {
        List<String> messages = new ArrayList<>();
        targetExpected.forEach((key, value) -> {
            try {
                assertEquals(targetExpected.get(key), targetResult.get(key));
            } catch (AssertionError assertFailed) {
                messages.add(key + ": " + assertFailed.getMessage());
            }
        });
        if (!messages.isEmpty()) {
            Assert.fail("\n\t" + String.join("\n\t", messages));
        }
    }

}