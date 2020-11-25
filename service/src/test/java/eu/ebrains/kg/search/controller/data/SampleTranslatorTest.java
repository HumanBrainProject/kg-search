package eu.ebrains.kg.search.controller.data;

import eu.ebrains.kg.search.controller.utils.TranslatorTestHelper;
import eu.ebrains.kg.search.controller.utils.WebClientHelper;
import eu.ebrains.kg.search.model.DatabaseScope;
import eu.ebrains.kg.search.model.source.ResultOfKGv2;
import eu.ebrains.kg.search.model.source.openMINDSv1.SampleV1;
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

public class SampleTranslatorTest {
    private static class SampleV1Result extends ResultOfKGv2<SampleV1> {
    }

    @Test
    public void compareReleasedSamples() {
        compareSamples(DatabaseScope.RELEASED);
    }

    @Test
    public void compareInferredSamples() {
        compareSamples(DatabaseScope.INFERRED);
    }

    private void compareSamples(DatabaseScope databaseScope) {
        List<String> result = new ArrayList<>();
        SampleV1Result queryResult = WebClientHelper.executeQuery("query/minds/experiment/sample/v1.0.0/search", databaseScope, SampleV1Result.class);
        queryResult.getResults().forEach(sample -> {
            String id = sample.getIdentifier();
            Map<String, Object> expected = WebClientHelper.getDocument(databaseScope.equals(DatabaseScope.RELEASED)?"public":"curated", "Sample", id, ElasticSearchDocument.class).getSource();
            List<String> messages = TranslatorTestHelper.compareSample(sample, expected, databaseScope, false);
            if (!messages.isEmpty()) {
                result.add("\n\n\tSample: " + id + "\n\t\t" + String.join("\n\t\t", messages));
            }
        });
        if (!result.isEmpty()) {
            Assert.fail(String.join("", result));
        }
    }

    @Test
    public void compareReleasedSample() throws IOException {
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v1/sampleReleasedSource.json"), StandardCharsets.UTF_8);
        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/sampleReleasedTarget.json"), StandardCharsets.UTF_8);
        List<String> result = TranslatorTestHelper.compareSample(sourceJson, expectedJson, DatabaseScope.RELEASED, false);
        if (!result.isEmpty()) {
            Assert.fail("\n\t" + String.join("\n\t", result));
        }
    }

    @Test
    public void compareInferredSample() throws IOException {
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v1/sampleInferredSource.json"), StandardCharsets.UTF_8);
        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/sampleInferredTarget.json"), StandardCharsets.UTF_8);
        List<String> result = TranslatorTestHelper.compareSample(sourceJson, expectedJson, DatabaseScope.INFERRED, false);
        if (!result.isEmpty()) {
            Assert.fail("\n\t" + String.join("\n\t", result));
        }
    }

    @Test
    public void compareInferredLiveSample() throws IOException {
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v1/sampleInferredSource.json"), StandardCharsets.UTF_8);
        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/sampleInferredLiveTarget.json"), StandardCharsets.UTF_8);
        List<String> result = TranslatorTestHelper.compareSample(sourceJson, expectedJson, DatabaseScope.INFERRED, true);
        if (!result.isEmpty()) {
            Assert.fail("\n\t" + String.join("\n\t", result));
        }
    }
}
