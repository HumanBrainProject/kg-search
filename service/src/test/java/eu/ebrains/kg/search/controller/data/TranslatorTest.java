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
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/modelReleasedSource.json"), StandardCharsets.UTF_8);
        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/modelReleasedTarget.json"), StandardCharsets.UTF_8);
        List<String> result = compareReleasedModel(sourceJson, expectedJson, DatabaseScope.RELEASED, false);
        if (!result.isEmpty()) {
            Assert.fail("\n\t" + String.join("\n\t", result));
        }
    }

    private List<String> compareReleasedModel(String sourceJson, String expectedJson, DatabaseScope databaseScope, boolean liveMode) {
        ModelTranslator translator = new ModelTranslator();

        ModelV2 source = jsonAdapter.fromJson(sourceJson, ModelV2.class);
        Map<String, Object> targetExpected = jsonAdapter.fromJson(expectedJson, Map.class);

        Model target = translator.translate(source, databaseScope, liveMode);
        String targetJson = jsonAdapter.toJson(target);
        Map<String, Object> targetResult = jsonAdapter.fromJson(targetJson, Map.class);

        return compareResults(targetExpected, targetResult);
    }

    @Test
    public void compareReleasedSoftware() throws IOException {
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/softwareReleasedSource.json"), StandardCharsets.UTF_8);
        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/softwareReleasedTarget.json"), StandardCharsets.UTF_8);
        List<String> result = compareReleasedModel(sourceJson, expectedJson, DatabaseScope.RELEASED, false);
        if (!result.isEmpty()) {
            Assert.fail("\n\t" + String.join("\n\t", result));
        }
    }

    public List<String> compareReleasedSoftware(String sourceJson, String expectedJson, DatabaseScope databaseScope, boolean liveMode) {
        SoftwareTranslator translator = new SoftwareTranslator();
        // Given
        SoftwareV2 source = jsonAdapter.fromJson(sourceJson, SoftwareV2.class);

        Map<String, Object> targetExpected = jsonAdapter.fromJson(expectedJson, Map.class);

        //When
        Software target = translator.translate(source, databaseScope, liveMode);
        String targetJson = jsonAdapter.toJson(target);
        Map<String, Object> targetResult = jsonAdapter.fromJson(targetJson, Map.class);

        //Then
        return compareResults(targetExpected, targetResult);
    }

    private List<String> compareResults(Map<String, Object> targetExpected, Map<String, Object> targetResult) {
        List<String> messages = new ArrayList<>();
        targetExpected.forEach((key, value) -> {
            try {
                assertEquals(targetExpected.get(key), targetResult.get(key));
            } catch (AssertionError assertFailed) {
                messages.add(key + ": " + assertFailed.getMessage());
            }
        });
        return messages;
    }

    @Test
    public void compareInferredSoftware() throws IOException {

    }

    @Test
    public void compareInferredLiveSoftware() throws IOException {

    }
}