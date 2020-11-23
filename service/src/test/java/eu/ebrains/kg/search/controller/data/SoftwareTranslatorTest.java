package eu.ebrains.kg.search.controller.data;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.api.JsonLdError;
import com.apicatalog.jsonld.document.JsonDocument;
import eu.ebrains.kg.search.controller.translators.SoftwareTranslator;
import eu.ebrains.kg.search.controller.utils.JsonAdapter;
import eu.ebrains.kg.search.model.DatabaseScope;
import eu.ebrains.kg.search.model.source.openMINDSv2.SoftwareV2;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Software;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import com.github.jsonldjava.utils.JsonUtils;
import org.junit.rules.ErrorCollector;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;

public class SoftwareTranslatorTest {

    SoftwareTranslator translator = new SoftwareTranslator();
    JsonAdapter jsonAdapter = new JsonAdapter();
    private final static JsonDocument EMPTY_DOCUMENT = JsonDocument.of(Json.createObjectBuilder().build());

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

        String json = IOUtils.toString(this.getClass().getResourceAsStream("/v2/softwareReleasedSource.json"), StandardCharsets.UTF_8);
        //System.out.println(json);

        SoftwareV2 source = jsonAdapter.fromJson(json, SoftwareV2.class);

        Software target = translator.translate(source, DatabaseScope.RELEASED, false);
        String targetJson = jsonAdapter.toJson(target);
        //System.out.println(targetJson);
        Map<String, Object> targetResult = jsonAdapter.fromJson(targetJson, Map.class);

        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/softwareReleasedTarget.json"), StandardCharsets.UTF_8);
        Map<String, Object> targetExpected = jsonAdapter.fromJson(expectedJson, Map.class);

        List<String> messages = new ArrayList<String>();
        targetExpected.entrySet().stream().forEach(i -> {
            String key = i.getKey();
            try {
                assertEquals(targetExpected.get(key), targetResult.get(key));
            } catch (AssertionError assertFaild) {
                messages.add(key + ": " + assertFaild.getMessage());
            }
        });
        if (!messages.isEmpty()) {
            Assert.fail("\n\t" + messages.stream().collect(Collectors.joining("\n\t")));
        }
    }

}