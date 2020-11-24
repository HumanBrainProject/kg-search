package eu.ebrains.kg.search.controller.data;

import eu.ebrains.kg.search.controller.translators.SoftwareTranslator;
import eu.ebrains.kg.search.controller.utils.JsonAdapter;
import eu.ebrains.kg.search.model.DatabaseScope;
import eu.ebrains.kg.search.model.source.openMINDSv2.SoftwareV2;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Software;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.rules.ErrorCollector;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class SoftwareTranslatorTest {

    SoftwareTranslator translator = new SoftwareTranslator();
    JsonAdapter jsonAdapter = new JsonAdapter();

    @Rule
    public ErrorCollector collector = new ErrorCollector();

    @Test
    public void compareReleasedSoftware() throws IOException {

        //String jsonld = IOUtils.toString(this.getClass().getResourceAsStream("/v2/softwareReleasedSource.jsonld"), StandardCharsets.UTF_8);
        //System.out.println(jsonld);
        //JsonObject build = Json.createObjectBuilder(jsonObject).build();
        //JsonArray expanded = JsonLd.expand(JsonDocument.of(jsonObject)).get();
        //JsonObject json = JsonLd.compact(JsonDocument.of(expanded), EMPTY_DOCUMENT).get();
        //Object qualified = standardization.fullyQualify(json);
        //System.out.println(qualified);

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

    @Test
    public void compareInferredSoftware() throws IOException {

    }

    @Test
    public void compareInferredLiveSoftware() throws IOException {

    }
}