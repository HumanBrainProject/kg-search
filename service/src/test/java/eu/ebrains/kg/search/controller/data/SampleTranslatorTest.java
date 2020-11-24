package eu.ebrains.kg.search.controller.data;

import eu.ebrains.kg.search.controller.utils.TranslatorTestHelper;
import eu.ebrains.kg.search.model.DatabaseScope;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SampleTranslatorTest {
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
