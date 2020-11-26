package eu.ebrains.kg.search.controller.data;

import eu.ebrains.kg.search.controller.utils.TranslatorTestHelper;
import eu.ebrains.kg.search.controller.utils.WebClientHelper;
import eu.ebrains.kg.search.model.DatabaseScope;
import eu.ebrains.kg.search.model.source.ResultOfKGv2;
import eu.ebrains.kg.search.model.source.openMINDSv1.DatasetV1;
import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchDocument;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DatasetTranslatorTest {
    private static class DatasetV1Result extends ResultOfKGv2<DatasetV1> {}

    @Test
    public void compareReleasedDatasets() {
        compareDatasets(DatabaseScope.RELEASED);
    }

    @Test
    public void compareInferredDatasets() {
        compareDatasets(DatabaseScope.INFERRED);
    }

    public void compareDatasets(DatabaseScope databaseScope) {
        List<String> result = new ArrayList<>();
        DatasetV1Result queryResult = WebClientHelper.executeQuery("query/minds/core/dataset/v1.0.0/search", DatabaseScope.RELEASED, DatasetV1Result.class);
        queryResult.getResults().forEach(dataset -> {
            String id = dataset.getIdentifier();
            Map<String, Object> expected = WebClientHelper.getDocument(databaseScope.equals(DatabaseScope.RELEASED)?"public":"curated", "Dataset", id, ElasticSearchDocument.class).getSource();
            List<String> messages = TranslatorTestHelper.compareDataset(dataset, expected, databaseScope, false);
            if (!messages.isEmpty()) {
                result.add("\n\n\tDataset: " + id + "\n\t\t" + String.join("\n\t\t", messages));
            }
        });
        if (!result.isEmpty()) {
            Assert.fail(String.join("", result));
        }
    }


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
