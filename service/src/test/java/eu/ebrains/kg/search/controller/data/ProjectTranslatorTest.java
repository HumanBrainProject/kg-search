package eu.ebrains.kg.search.controller.data;

import eu.ebrains.kg.search.controller.utils.TranslatorTestHelper;
import eu.ebrains.kg.search.controller.utils.WebClientHelper;
import eu.ebrains.kg.search.model.DatabaseScope;
import eu.ebrains.kg.search.model.source.ResultOfKGv2;
import eu.ebrains.kg.search.model.source.openMINDSv1.ProjectV1;
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

public class ProjectTranslatorTest {
    private static class ProjectV1Result extends ResultOfKGv2<ProjectV1> { }

    @Test
    public void compareReleasedProjects() {
        compareProjects(DatabaseScope.RELEASED, false);
    }

    @Test
    public void compareInferredSubjects() {
        compareProjects(DatabaseScope.INFERRED, false);
    }

    @Test
    public void compareInferredLiveSubjects() {
        compareProjects(DatabaseScope.INFERRED, true);
    }

    private void compareProjects(DatabaseScope databaseScope, boolean liveMode) {
        List<String> result = new ArrayList<>();
        ProjectV1Result queryResult = WebClientHelper.executeQuery("query/minds/core/placomponent/v1.0.0/search", databaseScope, ProjectV1Result.class);
        queryResult.getResults().forEach(project -> {
            String id = liveMode?project.getEditorId():project.getIdentifier();
            ElasticSearchDocument doc = null;
            if (liveMode) {
                doc = WebClientHelper.getLiveDocument(id, ElasticSearchDocument.class);
            } else {
                doc = WebClientHelper.getDocument(databaseScope, "Project", id, ElasticSearchDocument.class);
            }
            if (doc == null) {
                result.add("\n\n\tContributor: " + id + " (Fail to get expected document!)");
            } else {
                Map<String, Object> expected = doc.getSource();
                List<String> messages = TranslatorTestHelper.compareProject(project, expected, databaseScope, false);
                if (!messages.isEmpty()) {
                    result.add("\n\n\tProject: " + id + "\n\t\t" + String.join("\n\t\t", messages));
                }
            }
        });
        if (!result.isEmpty()) {
            Assert.fail(String.join("", result));
        }
    }

    @Test
    public void compareReleasedProject() throws IOException {
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v1/projectReleasedSource.json"), StandardCharsets.UTF_8);
        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/projectReleasedTarget.json"), StandardCharsets.UTF_8);
        List<String> result = TranslatorTestHelper.compareProject(sourceJson, expectedJson, DatabaseScope.RELEASED, false);
        if (!result.isEmpty()) {
            Assert.fail("\n\t" + String.join("\n\t", result));
        }
    }

    @Test
    public void compareInferredProject() throws IOException {
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v1/projectInferredSource.json"), StandardCharsets.UTF_8);
        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/projectInferredTarget.json"), StandardCharsets.UTF_8);
        List<String> result = TranslatorTestHelper.compareProject(sourceJson, expectedJson, DatabaseScope.INFERRED, false);
        if (!result.isEmpty()) {
            Assert.fail("\n\t" + String.join("\n\t", result));
        }

    }

    @Test
    public void compareInferredLiveProject() throws IOException {
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v1/projectInferredSource.json"), StandardCharsets.UTF_8);
        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/projectInferredLiveTarget.json"), StandardCharsets.UTF_8);
        List<String> result = TranslatorTestHelper.compareProject(sourceJson, expectedJson, DatabaseScope.INFERRED, true);
        if (!result.isEmpty()) {
            Assert.fail("\n\t" + String.join("\n\t", result));
        }
    }
}
