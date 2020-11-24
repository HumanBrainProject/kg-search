package eu.ebrains.kg.search.controller.data;

import eu.ebrains.kg.search.controller.utils.TranslatorTestHelper;
import eu.ebrains.kg.search.model.DatabaseScope;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class DatasetTranslatorTest {

    @Test
    public void compareReleasedDataset() throws IOException {
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v1/datasetReleasedSource.json"), StandardCharsets.UTF_8);
        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/datasetReleasedTarget.json"), StandardCharsets.UTF_8);
        List<String> result = TranslatorTestHelper.compareDataset(sourceJson, expectedJson, DatabaseScope.RELEASED, false);
        if (!result.isEmpty()) {
            Assert.fail("\n\t" + String.join("\n\t", result));
        }
    }

    @Test
    public void compareInferredDataset() throws IOException {
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v1/datasetInferredSource.json"), StandardCharsets.UTF_8);
        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/datasetInferredTarget.json"), StandardCharsets.UTF_8);
        List<String> result = TranslatorTestHelper.compareDataset(sourceJson, expectedJson, DatabaseScope.INFERRED, false);
        if (!result.isEmpty()) {
            Assert.fail("\n\t" + String.join("\n\t", result));
        }
    }

    @Test
    public void compareInferredLiveDataset() throws IOException {
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v1/datasetInferredSource.json"), StandardCharsets.UTF_8);
        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/datasetInferredLiveTarget.json"), StandardCharsets.UTF_8);
        List<String> result = TranslatorTestHelper.compareDataset(sourceJson, expectedJson, DatabaseScope.INFERRED, true);
        if (!result.isEmpty()) {
            Assert.fail("\n\t" + String.join("\n\t", result));
        }
    }
}
