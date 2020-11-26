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
    public void compareReleasedSoftwareAll() { compareSoftware(DatabaseScope.RELEASED); }

    @Test
    public void compareInferredSoftwareAll() {
        compareSoftware(DatabaseScope.INFERRED);
    }

    public void compareSoftware(DatabaseScope databaseScope) {
        List<String> result = new ArrayList<>();
        SoftwareV2Result queryResult = WebClientHelper.executeQuery("query/softwarecatalog/software/softwareproject/v1.0.0/search", DatabaseScope.RELEASED, SoftwareV2Result.class);
        queryResult.getResults().forEach(software -> {
            String id = software.getIdentifier();
            Map<String, Object> expected = WebClientHelper.getDocument(databaseScope.equals(DatabaseScope.RELEASED)?"public":"curated", "Software", id, ElasticSearchDocument.class).getSource();
            List<String> messages = TranslatorTestHelper.compareSoftware(software, expected, databaseScope, false);
            if (!messages.isEmpty()) {
                result.add("\n\n\tSoftware: " + id + "\n\t\t" + String.join("\n\t\t", messages));
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