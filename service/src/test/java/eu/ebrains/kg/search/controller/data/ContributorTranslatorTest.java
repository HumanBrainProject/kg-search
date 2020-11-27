package eu.ebrains.kg.search.controller.data;

import eu.ebrains.kg.search.controller.utils.TranslatorTestHelper;
import eu.ebrains.kg.search.controller.utils.WebClientHelper;
import eu.ebrains.kg.search.model.DatabaseScope;
import eu.ebrains.kg.search.model.source.PersonSources;
import eu.ebrains.kg.search.model.source.PersonV1andV2;
import eu.ebrains.kg.search.model.source.ResultOfKGv2;
import eu.ebrains.kg.search.model.source.openMINDSv1.PersonV1;
import eu.ebrains.kg.search.model.source.openMINDSv2.PersonV2;
import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchDocument;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ContributorTranslatorTest {

    private static class PersonV1Result extends ResultOfKGv2<PersonV1> {}

    private static class PersonV2Result extends ResultOfKGv2<PersonV2> { }

    @Test
    public void compareReleasedContributors() {
        compareContributors(DatabaseScope.RELEASED, false);
    }

    @Test
    public void compareInferredContributors() {
        compareContributors(DatabaseScope.INFERRED, false);
    }

    @Test
    public void compareInferredLiveContributors() {
        compareContributors(DatabaseScope.INFERRED, true);
    }

    private void compareContributors(DatabaseScope databaseScope, boolean liveMode) {
        Map<String, PersonSources> sourcesMap = new HashMap<>();

        List<String> result = new ArrayList<>();
        ContributorTranslatorTest.PersonV1Result personV1Result = WebClientHelper.executeQuery("query/minds/core/person/v1.0.0/search", databaseScope, ContributorTranslatorTest.PersonV1Result.class);
        ContributorTranslatorTest.PersonV2Result personV2Result = WebClientHelper.executeQuery("query/uniminds/core/person/v1.0.0/search", databaseScope, ContributorTranslatorTest.PersonV2Result.class);

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
                doc = WebClientHelper.getLiveDocument(id, ElasticSearchDocument.class);
            } else {
                doc = WebClientHelper.getDocument(databaseScope, "Contributor", id, ElasticSearchDocument.class);
            }
            if (doc == null) {
                result.add("\n\n\tContributor: " + person.getIdentifier() + " (Fail to get expected document!)");
            } else {
                Map<String, Object> expected = doc.getSource();
                List<String> messages = TranslatorTestHelper.compareContributor(personSources, expected, databaseScope, false);
                if (!messages.isEmpty()) {
                    result.add("\n\n\tContributor: " + person.getIdentifier() + "\n\t\t" + String.join("\n\t\t", messages));
                }
            }
        });
        if (!result.isEmpty()) {
            Assert.fail(String.join("", result));
        }
    }

    @Test
    public void compareReleasedContributorV1() throws IOException {
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v1/personReleasedSource.json"), StandardCharsets.UTF_8);
        List<String> sourcesJson = new ArrayList<>(Arrays.asList(sourceJson));
        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/contributorV1ReleasedTarget.json"), StandardCharsets.UTF_8);
        List<String> result = TranslatorTestHelper.compareContributor(sourcesJson, expectedJson, DatabaseScope.RELEASED, false);
        if (!result.isEmpty()) {
            Assert.fail("\n\t" + String.join("\n\t", result));
        }
    }
    @Test
    public void compareReleasedContributorV2() throws IOException {
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/personReleasedSource.json"), StandardCharsets.UTF_8);
        List<String> sourcesJson = new ArrayList<>(Arrays.asList(null, sourceJson));
        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/contributorV2ReleasedTarget.json"), StandardCharsets.UTF_8);
        List<String> result = TranslatorTestHelper.compareContributor(sourcesJson, expectedJson, DatabaseScope.RELEASED, false);
        if (!result.isEmpty()) {
            Assert.fail("\n\t" + String.join("\n\t", result));
        }
    }

    @Test
    public void compareInferredContributorV1() throws IOException {
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v1/personInferredSource.json"), StandardCharsets.UTF_8);
        List<String> sourcesJson = new ArrayList<>(Arrays.asList(sourceJson));
        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/contributorV1InferredTarget.json"), StandardCharsets.UTF_8);
        List<String> result = TranslatorTestHelper.compareContributor(sourcesJson, expectedJson, DatabaseScope.INFERRED, false);
        if (!result.isEmpty()) {
            Assert.fail("\n\t" + String.join("\n\t", result));
        }
    }
    @Test
    public void compareInferredContributorV2() throws IOException {
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/personInferredSource.json"), StandardCharsets.UTF_8);
        List<String> sourcesJson = new ArrayList<>(Arrays.asList(null, sourceJson));
        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/contributorV2InferredTarget.json"), StandardCharsets.UTF_8);
        List<String> result = TranslatorTestHelper.compareContributor(sourcesJson, expectedJson, DatabaseScope.INFERRED, false);
        if (!result.isEmpty()) {
            Assert.fail("\n\t" + String.join("\n\t", result));
        }
    }

    @Test
    public void compareInferredLiveContributorV1() throws IOException {
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v1/personInferredSource.json"), StandardCharsets.UTF_8);
        List<String> sourcesJson = new ArrayList<>(Arrays.asList(sourceJson));
        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/contributorV1InferredLiveTarget.json"), StandardCharsets.UTF_8);
        List<String> result = TranslatorTestHelper.compareContributor(sourcesJson, expectedJson, DatabaseScope.INFERRED, true);
        if (!result.isEmpty()) {
            Assert.fail("\n\t" + String.join("\n\t", result));
        }
    }
    @Test
    public void compareInferredLiveContributorV2() throws IOException {
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/personInferredSource.json"), StandardCharsets.UTF_8);
        List<String> sourcesJson = new ArrayList<>(Arrays.asList(null, sourceJson));
        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/contributorV2InferredLiveTarget.json"), StandardCharsets.UTF_8);
        List<String> result = TranslatorTestHelper.compareContributor(sourcesJson, expectedJson, DatabaseScope.INFERRED, true);
        if (!result.isEmpty()) {
            Assert.fail("\n\t" + String.join("\n\t", result));
        }
    }
}
