package eu.ebrains.kg.search.controller.data;

import eu.ebrains.kg.search.controller.utils.TranslatorTestHelper;
import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.PersonSources;
import eu.ebrains.kg.search.model.source.PersonV1andV2;
import eu.ebrains.kg.search.model.source.ResultOfKGv2;
import eu.ebrains.kg.search.model.source.openMINDSv1.PersonV1;
import eu.ebrains.kg.search.model.source.openMINDSv2.PersonV2;
import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchDocument;
import eu.ebrains.kg.search.services.KGServiceClient;
import eu.ebrains.kg.search.services.LegacySearchServiceClient;
import org.apache.commons.io.IOUtils;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ContributorOfKGV2TranslatorTest {

    private final KGServiceClient kgServiceClient;

    public ContributorOfKGV2TranslatorTest(KGServiceClient kgServiceClient) {
        this.kgServiceClient = kgServiceClient;
    }

    @Value("${test.token}")
    String token;

    private static class PersonV1Result extends ResultOfKGv2<PersonV1> {}

    private static class PersonV2Result extends ResultOfKGv2<PersonV2> { }

    @Test
    public void compareReleasedContributors() {
        compareContributors(DataStage.RELEASED, false);
    }

    @Test
    public void compareInferredContributors() {
        compareContributors(DataStage.IN_PROGRESS, false);
    }

    @Test
    public void compareInferredLiveContributors() {
        compareContributors(DataStage.IN_PROGRESS, true);
    }

    private void compareContributors(DataStage dataStage, boolean liveMode) {
        Map<String, PersonSources> sourcesMap = new HashMap<>();

        List<String> result = new ArrayList<>();
        ContributorOfKGV2TranslatorTest.PersonV1Result personV1Result = kgServiceClient.executeQueryForIndexing("query/minds/core/person/v1.0.0/search", dataStage, ContributorOfKGV2TranslatorTest.PersonV1Result.class, token);
        ContributorOfKGV2TranslatorTest.PersonV2Result personV2Result = kgServiceClient.executeQueryForIndexing("query/uniminds/core/person/v1.0.0/search", dataStage, ContributorOfKGV2TranslatorTest.PersonV2Result.class, token);

        if (!CollectionUtils.isEmpty(personV1Result.getResults())) {
            personV1Result.getResults().forEach(person -> {
                String id = person.getIdentifier();
                if (sourcesMap.containsKey(id)) {
                    PersonSources sources = sourcesMap.get(id);
                    sources.setPersonV1(person);
                } else {
                    PersonSources sources = new PersonSources();
                    sourcesMap.put(id, sources);
                    sources.setPersonV1(person);
                }
            });
        }

        if (!CollectionUtils.isEmpty(personV2Result.getResults())) {
            personV2Result.getResults().forEach(person -> {
                String id = person.getIdentifier();
                if (sourcesMap.containsKey(id)) {
                    PersonSources sources = sourcesMap.get(id);
                    sources.setPersonV2(person);
                } else {
                    PersonSources sources = new PersonSources();
                    sourcesMap.put(id, sources);
                    sources.setPersonV2(person);
                }
            });
        }
        sourcesMap.forEach((key, personSources) -> {
            PersonV1andV2 person = personSources.getPersonV2() != null ? personSources.getPersonV2() : personSources.getPersonV1();
            String id = liveMode?person.getEditorId():person.getIdentifier();
            ElasticSearchDocument doc;
            if (liveMode) {
                doc = LegacySearchServiceClient.getLiveDocument(id, ElasticSearchDocument.class);
            } else {
                doc = LegacySearchServiceClient.getDocument(dataStage, "Contributor", id, ElasticSearchDocument.class);
            }
            if (doc == null) {
                result.add("\n\n\tContributor: " + person.getIdentifier() + " (Fail to get expected document!)");
            } else {
                Map<String, Object> expected = doc.getSource();
                List<String> messages = TranslatorTestHelper.compareContributor(personSources, expected, dataStage, liveMode);
                if (!messages.isEmpty()) {
                    result.add("\n\n\tContributor: " + person.getIdentifier() + "\n\t\t" + String.join("\n\t\t", messages));
                }
            }
        });
        if (!result.isEmpty()) {
            fail(String.join("", result));
        }
    }

    @Test
    public void compareReleasedContributorV1() throws IOException {
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v1/personReleasedSource.json"), StandardCharsets.UTF_8);
        List<String> sourcesJson = new ArrayList<>(Arrays.asList(sourceJson));
        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/contributorV1ReleasedTarget.json"), StandardCharsets.UTF_8);
        List<String> result = TranslatorTestHelper.compareContributor(sourcesJson, expectedJson, DataStage.RELEASED, false);
        if (!result.isEmpty()) {
            fail("\n\t" + String.join("\n\t", result));
        }
    }
    @Test
    public void compareReleasedContributorV2() throws IOException {
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/personReleasedSource.json"), StandardCharsets.UTF_8);
        List<String> sourcesJson = new ArrayList<>(Arrays.asList(null, sourceJson));
        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/contributorV2ReleasedTarget.json"), StandardCharsets.UTF_8);
        List<String> result = TranslatorTestHelper.compareContributor(sourcesJson, expectedJson, DataStage.RELEASED, false);
        if (!result.isEmpty()) {
            fail("\n\t" + String.join("\n\t", result));
        }
    }

    @Test
    public void compareInferredContributorV1() throws IOException {
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v1/personInferredSource.json"), StandardCharsets.UTF_8);
        List<String> sourcesJson = new ArrayList<>(Arrays.asList(sourceJson));
        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/contributorV1InferredTarget.json"), StandardCharsets.UTF_8);
        List<String> result = TranslatorTestHelper.compareContributor(sourcesJson, expectedJson, DataStage.IN_PROGRESS, false);
        if (!result.isEmpty()) {
            fail("\n\t" + String.join("\n\t", result));
        }
    }
    @Test
    public void compareInferredContributorV2() throws IOException {
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/personInferredSource.json"), StandardCharsets.UTF_8);
        List<String> sourcesJson = Arrays.asList(null, sourceJson);
        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/contributorV2InferredTarget.json"), StandardCharsets.UTF_8);
        List<String> result = TranslatorTestHelper.compareContributor(sourcesJson, expectedJson, DataStage.IN_PROGRESS, false);
        if (!result.isEmpty()) {
            fail("\n\t" + String.join("\n\t", result));
        }
    }

    @Test
    public void compareInferredLiveContributorV1() throws IOException {
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v1/personInferredSource.json"), StandardCharsets.UTF_8);
        List<String> sourcesJson = Collections.singletonList(sourceJson);
        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/contributorV1InferredLiveTarget.json"), StandardCharsets.UTF_8);
        List<String> result = TranslatorTestHelper.compareContributor(sourcesJson, expectedJson, DataStage.IN_PROGRESS, true);
        if (!result.isEmpty()) {
            fail("\n\t" + String.join("\n\t", result));
        }
    }
    @Test
    public void compareInferredLiveContributorV2() throws IOException {
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/personInferredSource.json"), StandardCharsets.UTF_8);
        List<String> sourcesJson = Arrays.asList(null, sourceJson);
        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/contributorV2InferredLiveTarget.json"), StandardCharsets.UTF_8);
        List<String> result = TranslatorTestHelper.compareContributor(sourcesJson, expectedJson, DataStage.IN_PROGRESS, true);
        if (!result.isEmpty()) {
            fail("\n\t" + String.join("\n\t", result));
        }
    }
}
