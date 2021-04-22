package eu.ebrains.kg.search.controller.utils;

import eu.ebrains.kg.search.controller.translators.*;
import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.PersonSources;
import eu.ebrains.kg.search.model.source.openMINDSv1.*;
import eu.ebrains.kg.search.model.source.openMINDSv2.ModelV2;
import eu.ebrains.kg.search.model.source.openMINDSv2.PersonV2;
import eu.ebrains.kg.search.model.source.openMINDSv2.SoftwareV2;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TranslatorTestHelper {
    private static final JsonAdapter jsonAdapter = new JsonAdapter();

    private static final List<String> keysToIgnore = new ArrayList<>(Arrays.asList("@timestamp"));

    public static List<String> compareContributor(List<String> sourcesJson, String expectedJson, DataStage dataStage, boolean liveMode) {

        PersonSources sources = new PersonSources();

        if (sourcesJson.size() >= 1) {
            String sourceJsonV1 = sourcesJson.get(0);
            if (sourceJsonV1 != null) {
                PersonV1 source = jsonAdapter.fromJson(sourceJsonV1, PersonV1.class);
                sources.setPersonV1(source);
            }
        }

        if (sourcesJson.size() >= 2) {
            String sourceJsonV2 = sourcesJson.get(1);
            if (sourceJsonV2 != null) {
                PersonV2 source = jsonAdapter.fromJson(sourceJsonV2, PersonV2.class);
                sources.setPersonV2(source);
            }
        }

        Map<String, Object> targetExpected = jsonAdapter.fromJson(expectedJson, Map.class);

        return compareContributor(sources, targetExpected, dataStage, liveMode);
    }

    public static List<String> compareContributor(PersonSources sources, Map<String, Object> targetExpected, DataStage dataStage, boolean liveMode) {
        ContributorOfKGV2Translator translator = new ContributorOfKGV2Translator();

        Contributor target = translator.translate(sources, dataStage, liveMode);
        String targetJson = jsonAdapter.toJson(target);
        Map<String, Object> targetResult = jsonAdapter.fromJson(targetJson, Map.class);

        return compareResults(targetExpected, targetResult);
    }

    public static List<String> compareModel(String sourceJson, String expectedJson, DataStage dataStage, boolean liveMode) {
        ModelV2 source = jsonAdapter.fromJson(sourceJson, ModelV2.class);
        Map<String, Object> targetExpected = jsonAdapter.fromJson(expectedJson, Map.class);
        return compareModel(source, targetExpected, dataStage, liveMode);
    }

    public static List<String> compareModel(ModelV2 source, Map<String, Object> targetExpected, DataStage dataStage, boolean liveMode) {
        ModelOfKGV2Translator translator = new ModelOfKGV2Translator();
        Model target = translator.translate(source, dataStage, liveMode);
        String targetJson = jsonAdapter.toJson(target);
        Map<String, Object> targetResult = jsonAdapter.fromJson(targetJson, Map.class);

        return compareResults(targetExpected, targetResult);
    }

    public static List<String> compareSubject(String sourceJson, String expectedJson, DataStage dataStage, boolean liveMode) {
        SubjectV1 source = jsonAdapter.fromJson(sourceJson, SubjectV1.class);
        Map<String, Object> targetExpected = jsonAdapter.fromJson(expectedJson, Map.class);
        return compareSubject(source, targetExpected, dataStage, liveMode);
    }

    public static List<String> compareSubject(SubjectV1 source, Map<String, Object> targetExpected, DataStage dataStage, boolean liveMode) {
        SubjectTranslator translator = new SubjectTranslator();
        Subject target = translator.translate(source, dataStage, liveMode);
        String targetJson = jsonAdapter.toJson(target);
        Map<String, Object> targetResult = jsonAdapter.fromJson(targetJson, Map.class);

        return compareResults(targetExpected, targetResult);
    }

    public static List<String> compareSoftware(String sourceJson, String expectedJson, DataStage dataStage, boolean liveMode) {
        SoftwareV2 source = jsonAdapter.fromJson(sourceJson, SoftwareV2.class);
        Map<String, Object> targetExpected = jsonAdapter.fromJson(expectedJson, Map.class);
        return compareSoftware(source, targetExpected, dataStage, liveMode);
    }


    public static List<String> compareSoftware(SoftwareV2 source, Map<String, Object> targetExpected, DataStage dataStage, boolean liveMode) {
        SoftwareOfKGV2Translator translator = new SoftwareOfKGV2Translator();
        Software target = translator.translate(source, dataStage, liveMode);
        String targetJson = jsonAdapter.toJson(target);
        Map<String, Object> targetResult = jsonAdapter.fromJson(targetJson, Map.class);

        return compareResults(targetExpected, targetResult);
    }

    public static List<String> compareSample(String sourceJson, String expectedJson, DataStage dataStage, boolean liveMode) {
        SampleV1 source = jsonAdapter.fromJson(sourceJson, SampleV1.class);
        Map<String, Object> targetExpected = jsonAdapter.fromJson(expectedJson, Map.class);
        return compareSample(source, targetExpected, dataStage, liveMode);
    }

    public static List<String> compareSample(SampleV1 source, Map<String, Object> targetExpected, DataStage dataStage, boolean liveMode) {
        SampleTranslator translator = new SampleTranslator();
        Sample target = translator.translate(source, dataStage, liveMode);
        String targetJson = jsonAdapter.toJson(target);
        Map<String, Object> targetResult = jsonAdapter.fromJson(targetJson, Map.class);

        return compareResults(targetExpected, targetResult);
    }

    public static List<String> compareProject(String sourceJson, String expectedJson, DataStage dataStage, boolean liveMode) {
        ProjectV1 source = jsonAdapter.fromJson(sourceJson, ProjectV1.class);
        Map<String, Object> targetExpected = jsonAdapter.fromJson(expectedJson, Map.class);
        return compareProject(source, targetExpected, dataStage, liveMode);
    }


    public static List<String> compareProject(ProjectV1 source, Map<String, Object> targetExpected, DataStage dataStage, boolean liveMode) {
        ProjectTranslator translator = new ProjectTranslator();
        Project target = translator.translate(source, dataStage, liveMode);
        String targetJson = jsonAdapter.toJson(target);
        Map<String, Object> targetResult = jsonAdapter.fromJson(targetJson, Map.class);
        return compareResults(targetExpected, targetResult);
    }

    public static List<String> compareDataset(String sourceJson, String expectedJson, DataStage dataStage, boolean liveMode) {
        DatasetV1 source = jsonAdapter.fromJson(sourceJson, DatasetV1.class);
        Map<String, Object> targetExpected = jsonAdapter.fromJson(expectedJson, Map.class);
        return compareDataset(source, targetExpected, dataStage, liveMode);
    }

    public static List<String> compareDataset(DatasetV1 source, Map<String, Object> targetExpected, DataStage dataStage, boolean liveMode) {
        DatasetOfKGV2Translator translator = new DatasetOfKGV2Translator();
        Dataset target = translator.translate(source, dataStage, liveMode);
        String targetJson = jsonAdapter.toJson(target);
        Map<String, Object> targetResult = jsonAdapter.fromJson(targetJson, Map.class);
        return compareResults(targetExpected, targetResult);
    }

    private static List<String> compareResults(Map<String, Object> targetExpected, Map<String, Object> targetResult) {
        List<String> messages = new ArrayList<>();
        targetExpected.forEach((key, value) -> {
            if (!keysToIgnore.contains(key)) {
                try {
                    assertEquals(targetExpected.get(key), targetResult.get(key));
                } catch (AssertionError assertFailed) {
                    messages.add(key + ": " + assertFailed.getMessage());
                }
            }
        });
        return messages;
    }
}
