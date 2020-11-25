package eu.ebrains.kg.search.controller.data;

import eu.ebrains.kg.search.controller.utils.TranslatorTestHelper;
import eu.ebrains.kg.search.controller.utils.WebClientHelper;
import eu.ebrains.kg.search.model.DatabaseScope;
import eu.ebrains.kg.search.model.source.ResultOfKGv2;
import eu.ebrains.kg.search.model.source.openMINDSv1.SubjectV1;
import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchDocument;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SubjectTranslatorTest {
    private static class SubjectV1Result extends ResultOfKGv2<SubjectV1> {
    }

    @Test
    public void compareReleasedSubjects() {
        compareSubjects(DatabaseScope.RELEASED);
    }

    @Test
    public void compareInferredSubjects() {
        compareSubjects(DatabaseScope.INFERRED);
    }

    private void compareSubjects(DatabaseScope databaseScope) {
        List<String> result = new ArrayList<>();
        SubjectV1Result queryResult = WebClientHelper.executeQuery("query/minds/experiment/subject/v1.0.0/search", databaseScope, SubjectV1Result.class);
        queryResult.getResults().forEach(subject -> {
            String id = subject.getIdentifier();
            Map<String, Object> expected = WebClientHelper.getDocument(databaseScope.equals(DatabaseScope.RELEASED)?"public":"curated", "Subject", id, ElasticSearchDocument.class).getSource();
            List<String> messages = TranslatorTestHelper.compareSubject(subject, expected, databaseScope, false);
            if (!messages.isEmpty()) {
                result.add("\n\n\tSubject: " + id + "\n\t\t" + String.join("\n\t\t", messages));
            }
        });
        if (!result.isEmpty()) {
            Assert.fail(String.join("", result));
        }
    }

    @Test
    public void compareReleasedSubject() throws IOException {
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v1/subjectReleasedSource.json"), StandardCharsets.UTF_8);
        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/subjectReleasedTarget.json"), StandardCharsets.UTF_8);
        List<String> result = TranslatorTestHelper.compareSubject(sourceJson, expectedJson, DatabaseScope.RELEASED, false);
        if (!result.isEmpty()) {
            Assert.fail("\n\t" + String.join("\n\t", result));
        }
    }

    @Test
    public void compareInferredSubject() throws IOException {
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v1/subjectInferredSource.json"), StandardCharsets.UTF_8);
        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/subjectInferredTarget.json"), StandardCharsets.UTF_8);
        List<String> result = TranslatorTestHelper.compareSubject(sourceJson, expectedJson, DatabaseScope.INFERRED, false);
        if (!result.isEmpty()) {
            Assert.fail("\n\t" + String.join("\n\t", result));
        }

    }

    @Test
    public void compareInferredLiveSubject() throws IOException {
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v1/subjectInferredSource.json"), StandardCharsets.UTF_8);
        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/subjectInferredLiveTarget.json"), StandardCharsets.UTF_8);
        List<String> result = TranslatorTestHelper.compareSubject(sourceJson, expectedJson, DatabaseScope.INFERRED, true);
        if (!result.isEmpty()) {
            Assert.fail("\n\t" + String.join("\n\t", result));
        }
    }
}
