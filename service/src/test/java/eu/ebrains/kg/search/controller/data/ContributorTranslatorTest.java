package eu.ebrains.kg.search.controller.data;

import eu.ebrains.kg.search.controller.utils.TranslatorTestHelper;
import eu.ebrains.kg.search.model.DatabaseScope;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ContributorTranslatorTest {
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
