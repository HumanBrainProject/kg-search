package eu.ebrains.kg.search.controller.data;

import eu.ebrains.kg.search.controller.utils.TranslatorTestHelper;
import eu.ebrains.kg.search.controller.utils.WebClientHelper;
import eu.ebrains.kg.search.model.DatabaseScope;
import eu.ebrains.kg.search.model.source.ResultOfKGv2;
import eu.ebrains.kg.search.model.source.openMINDSv2.SoftwareV2;
import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchDocument;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SoftwareTranslatorTest {
    private static class SoftwareV2Result extends ResultOfKGv2<SoftwareV2> {}

    @Test
    public void compareReleasedSoftwares() { compareSoftware(DatabaseScope.RELEASED, false); }

    @Test
    public void compareInferredSoftwares() {
        compareSoftware(DatabaseScope.INFERRED, false);
    }

    @Test
    public void compareInferredLiveSoftwares() {
        compareSoftware(DatabaseScope.INFERRED, true);
    }

    public void compareSoftware(DatabaseScope databaseScope, boolean liveMode) {
        List<String> result = new ArrayList<>();
        SoftwareV2Result queryResult = WebClientHelper.executeQuery("query/softwarecatalog/software/softwareproject/v1.0.0/search", databaseScope, SoftwareV2Result.class);
        queryResult.getResults().forEach(software -> {
            String id = liveMode?software.getEditorId():software.getIdentifier();
            ElasticSearchDocument doc;
            if (liveMode) {
                doc = WebClientHelper.getLiveDocument(id, ElasticSearchDocument.class);
            } else {
                doc = WebClientHelper.getDocument(databaseScope, "Software", id, ElasticSearchDocument.class);
            }
            if (doc == null) {
                result.add("\n\n\tSoftware: " + software.getIdentifier() + " (Fail to get expected document!)");
            } else {
                Map<String, Object> expected = doc.getSource();
                List<String> messages = TranslatorTestHelper.compareSoftware(software, expected, databaseScope, false);
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
        List<String> result = TranslatorTestHelper.compareSoftware(sourceJson, expectedJson, DatabaseScope.RELEASED, false);
        if (!result.isEmpty()) {
            Assert.fail("\n\t" + String.join("\n\t", result));
        }
    }

    @Test
    public void compareInferredSoftware() throws IOException {
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/softwareInferredSource.json"), StandardCharsets.UTF_8);
        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/softwareInferredTarget.json"), StandardCharsets.UTF_8);
        List<String> result = TranslatorTestHelper.compareSoftware(sourceJson, expectedJson, DatabaseScope.INFERRED, false);
        if (!result.isEmpty()) {
            Assert.fail("\n\t" + String.join("\n\t", result));
        }
    }

    @Test
    public void compareInferredLiveSoftware() throws IOException {
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/softwareInferredSource.json"), StandardCharsets.UTF_8);
        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/softwareInferredLiveTarget.json"), StandardCharsets.UTF_8);
        List<String> result = TranslatorTestHelper.compareSoftware(sourceJson, expectedJson, DatabaseScope.INFERRED, true);
        if (!result.isEmpty()) {
            Assert.fail("\n\t" + String.join("\n\t", result));
        }
    }
}