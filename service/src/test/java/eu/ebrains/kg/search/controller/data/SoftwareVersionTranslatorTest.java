package eu.ebrains.kg.search.controller.data;

import eu.ebrains.kg.search.controller.utils.TranslatorTestHelper;
import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.ResultsOfKGv2;
import eu.ebrains.kg.search.model.source.openMINDSv2.SoftwareV2;
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

public class SoftwareVersionTranslatorTest {
    private final KGServiceClient kgServiceClient;

    public SoftwareVersionTranslatorTest(KGServiceClient kgServiceClient) {
        this.kgServiceClient = kgServiceClient;
    }

    @Value("${test.token}")
    String token;

    private static class SoftwareV2Results extends ResultsOfKGv2<SoftwareV2> {}

    @Test
    public void compareReleasedSoftwares() { compareSoftware(DataStage.RELEASED, false); }

    @Test
    public void compareInferredSoftwares() {
        compareSoftware(DataStage.IN_PROGRESS, false);
    }

    @Test
    public void compareInferredLiveSoftwares() {
        compareSoftware(DataStage.IN_PROGRESS, true);
    }

    public void compareSoftware(DataStage dataStage, boolean liveMode) {
        List<String> result = new ArrayList<>();
        SoftwareV2Results queryResult = kgServiceClient.executeQueryForIndexing("query/softwarecatalog/software/softwareproject/v1.0.0/search", dataStage, SoftwareV2Results.class, token);
        queryResult.getResults().forEach(software -> {
            String id = liveMode?software.getEditorId():software.getIdentifier();
            ElasticSearchDocument doc;
            if (liveMode) {
                doc = LegacySearchServiceClient.getLiveDocument(id, ElasticSearchDocument.class);
            } else {
                doc = LegacySearchServiceClient.getDocument(dataStage, "Software", id, ElasticSearchDocument.class);
            }
            if (doc == null) {
                result.add("\n\n\tSoftware: " + software.getIdentifier() + " (Fail to get expected document!)");
            } else {
                Map<String, Object> expected = doc.getSource();
                List<String> messages = TranslatorTestHelper.compareSoftware(software, expected, dataStage, liveMode);
                if (!messages.isEmpty()) {
                    result.add("\n\n\tSoftware: " + software.getIdentifier() + "\n\t\t" + String.join("\n\t\t", messages));
                }
            }
        });
        if (!result.isEmpty()) {
            Assert.fail(String.join("", result));
        }
    }

    @Test
    public void compareReleasedSoftware() throws IOException {
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/softwareReleasedSource.json"), StandardCharsets.UTF_8);
        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/softwareReleasedTarget.json"), StandardCharsets.UTF_8);
        List<String> result = TranslatorTestHelper.compareSoftware(sourceJson, expectedJson, DataStage.RELEASED, false);
        if (!result.isEmpty()) {
            Assert.fail("\n\t" + String.join("\n\t", result));
        }
    }

    @Test
    public void compareInferredSoftware() throws IOException {
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/softwareInferredSource.json"), StandardCharsets.UTF_8);
        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/softwareInferredTarget.json"), StandardCharsets.UTF_8);
        List<String> result = TranslatorTestHelper.compareSoftware(sourceJson, expectedJson, DataStage.IN_PROGRESS, false);
        if (!result.isEmpty()) {
            Assert.fail("\n\t" + String.join("\n\t", result));
        }
    }

    @Test
    public void compareInferredLiveSoftware() throws IOException {
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/softwareInferredSource.json"), StandardCharsets.UTF_8);
        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/softwareInferredLiveTarget.json"), StandardCharsets.UTF_8);
        List<String> result = TranslatorTestHelper.compareSoftware(sourceJson, expectedJson, DataStage.IN_PROGRESS, true);
        if (!result.isEmpty()) {
            Assert.fail("\n\t" + String.join("\n\t", result));
        }
    }
}