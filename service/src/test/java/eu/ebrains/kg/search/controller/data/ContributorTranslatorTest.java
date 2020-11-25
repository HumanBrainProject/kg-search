package eu.ebrains.kg.search.controller.data;

import eu.ebrains.kg.search.controller.utils.TranslatorTestHelper;
import eu.ebrains.kg.search.controller.utils.WebClientHelper;
import eu.ebrains.kg.search.model.DatabaseScope;
import eu.ebrains.kg.search.model.source.PersonSources;
import eu.ebrains.kg.search.model.source.PersonV1andV2;
import eu.ebrains.kg.search.model.source.ResultOfKGv2;
import eu.ebrains.kg.search.model.source.openMINDSv1.PersonV1;
import eu.ebrains.kg.search.model.source.openMINDSv1.SubjectV1;
import eu.ebrains.kg.search.model.source.openMINDSv2.PersonV2;
import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchDocument;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Contributor;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ContributorTranslatorTest {

    private static class PersonV1Result extends ResultOfKGv2<PersonV1> {
    }

    private static class PersonV2Result extends ResultOfKGv2<PersonV2> {
    }

    @Test
    public void compareReleasedContributors() {
        compareContributors(DatabaseScope.RELEASED);
    }

    @Test
    public void compareInferredContributors() {
        compareContributors(DatabaseScope.INFERRED);
    }

    private void compareContributors(DatabaseScope databaseScope) {
        List<String> result = new ArrayList<>();
        ContributorTranslatorTest.PersonV1Result personV1Result = WebClientHelper.executeQuery("query/minds/core/person/v1.0.0/search", databaseScope, ContributorTranslatorTest.PersonV1Result.class);
        ContributorTranslatorTest.PersonV1Result personV2Result = WebClientHelper.executeQuery("query/minds/core/person/v1.0.0/search", databaseScope, ContributorTranslatorTest.PersonV1Result.class);

        //PersonSources sources = new PersonSources();
        List<PersonSources> sources =  new ArrayList<>();
        sources.forEach(personSources -> {
            PersonV1andV2 person = personSources.getPersonV2() != null ? personSources.getPersonV2() : personSources.getPersonV1();
            String id = person.getIdentifier();
            Map<String, Object> expected = WebClientHelper.getDocument(databaseScope.equals(DatabaseScope.RELEASED)?"public":"curated", "Contributor", id, ElasticSearchDocument.class).getSource();
            List<String> messages = TranslatorTestHelper.compareContributor(personSources, expected, databaseScope, false);
            if (!messages.isEmpty()) {
                result.add("\n\n\tContributor: " + id + "\n\t\t" + String.join("\n\t\t", messages));
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
