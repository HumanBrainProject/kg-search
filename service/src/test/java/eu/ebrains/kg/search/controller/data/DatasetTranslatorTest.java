package eu.ebrains.kg.search.controller.data;

import eu.ebrains.kg.search.controller.utils.TranslatorTestHelper;
import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.ResultOfKGv2;
import eu.ebrains.kg.search.model.source.openMINDSv1.DatasetV1;
import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchDocument;
import eu.ebrains.kg.search.services.KGServiceClient;
import eu.ebrains.kg.search.services.LegacySearchServiceClient;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DatasetTranslatorTest {
    private final KGServiceClient kgServiceClient;

    public DatasetTranslatorTest(KGServiceClient kgServiceClient) {
        this.kgServiceClient = kgServiceClient;
    }

    @Value("${test.token}")
    String token;

    private static class DatasetV1Result extends ResultOfKGv2<DatasetV1> {}

    @Test
    public void compareReleasedDatasets() {
        compareDatasets(DataStage.RELEASED, false);
    }

    @Test
    public void compareInferredDatasets() {
        compareDatasets(DataStage.IN_PROGRESS, false);
    }

    @Test
    public void compareInferredLiveDatasets() {
        compareDatasets(DataStage.IN_PROGRESS, true);
    }

    public void compareDatasets(DataStage dataStage, boolean liveMode) {
        List<String> result = new ArrayList<>();
        DatasetV1Result queryResult = kgServiceClient.executeQuery("query/minds/core/dataset/v1.0.0/search", dataStage, DatasetV1Result.class, token);
        queryResult.getResults().forEach(dataset -> {
            String id = liveMode?dataset.getEditorId():dataset.getIdentifier();
            ElasticSearchDocument doc;
            if (liveMode) {
                doc = LegacySearchServiceClient.getLiveDocument(id, ElasticSearchDocument.class);
            } else {
                doc = LegacySearchServiceClient.getDocument(dataStage, "Dataset", id, ElasticSearchDocument.class);
            }
            if (doc == null) {
                result.add("\n\n\tDataset: " + dataset.getIdentifier() + " (Fail to get expected document!)");
            } else {
                Map<String, Object> expected = doc.getSource();
                List<String> messages = TranslatorTestHelper.compareDataset(dataset, expected, dataStage, liveMode);
                if (!messages.isEmpty()) {
                    result.add("\n\n\tDataset: " + dataset.getIdentifier() + "\n\t\t" + String.join("\n\t\t", messages));
                }
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
        List<String> result = TranslatorTestHelper.compareDataset(sourceJson, expectedJson, DataStage.RELEASED, false);
        if (!result.isEmpty()) {
            Assert.fail("\n\t" + String.join("\n\t", result));
        }
    }

    @Test
    public void compareInferredDataset() throws IOException {
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v1/datasetInferredSource.json"), StandardCharsets.UTF_8);
        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/datasetInferredTarget.json"), StandardCharsets.UTF_8);
        List<String> result = TranslatorTestHelper.compareDataset(sourceJson, expectedJson, DataStage.IN_PROGRESS, false);
        if (!result.isEmpty()) {
            Assert.fail("\n\t" + String.join("\n\t", result));
        }
    }

    @Test
    public void compareInferredLiveDataset() throws IOException {
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v1/datasetInferredSource.json"), StandardCharsets.UTF_8);
        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/datasetInferredLiveTarget.json"), StandardCharsets.UTF_8);
        List<String> result = TranslatorTestHelper.compareDataset(sourceJson, expectedJson, DataStage.IN_PROGRESS, true);
        if (!result.isEmpty()) {
            Assert.fail("\n\t" + String.join("\n\t", result));
        }
    }
}
