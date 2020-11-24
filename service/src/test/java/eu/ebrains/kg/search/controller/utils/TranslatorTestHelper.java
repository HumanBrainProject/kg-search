package eu.ebrains.kg.search.controller.utils;

import eu.ebrains.kg.search.controller.translators.DatasetTranslator;
import eu.ebrains.kg.search.controller.translators.ModelTranslator;
import eu.ebrains.kg.search.controller.translators.SoftwareTranslator;
import eu.ebrains.kg.search.model.DatabaseScope;
import eu.ebrains.kg.search.model.source.openMINDSv1.DatasetV1;
import eu.ebrains.kg.search.model.source.openMINDSv2.ModelV2;
import eu.ebrains.kg.search.model.source.openMINDSv2.SoftwareV2;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Dataset;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Model;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Software;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TranslatorTestHelper {
    private static final JsonAdapter jsonAdapter = new JsonAdapter();

    public static List<String> compareModel(String sourceJson, String expectedJson, DatabaseScope databaseScope, boolean liveMode) {
        ModelTranslator translator = new ModelTranslator();

        ModelV2 source = jsonAdapter.fromJson(sourceJson, ModelV2.class);
        Map<String, Object> targetExpected = jsonAdapter.fromJson(expectedJson, Map.class);

        Model target = translator.translate(source, databaseScope, liveMode);
        String targetJson = jsonAdapter.toJson(target);
        Map<String, Object> targetResult = jsonAdapter.fromJson(targetJson, Map.class);

        return compareResults(targetExpected, targetResult);
    }

    public static List<String> compareSoftware(String sourceJson, String expectedJson, DatabaseScope databaseScope, boolean liveMode) {
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

    public static List<String> compareDataset(String sourceJson, String expectedJson, DatabaseScope databaseScope, boolean liveMode) {
        DatasetTranslator translator = new DatasetTranslator();
        // Given
        DatasetV1 source = jsonAdapter.fromJson(sourceJson, DatasetV1.class);

        Map<String, Object> targetExpected = jsonAdapter.fromJson(expectedJson, Map.class);

        //When
        Dataset target = translator.translate(source, databaseScope, liveMode);
        String targetJson = jsonAdapter.toJson(target);
        Map<String, Object> targetResult = jsonAdapter.fromJson(targetJson, Map.class);

        //Then
        return compareResults(targetExpected, targetResult);
    }

    private static List<String> compareResults(Map<String, Object> targetExpected, Map<String, Object> targetResult) {
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
}
