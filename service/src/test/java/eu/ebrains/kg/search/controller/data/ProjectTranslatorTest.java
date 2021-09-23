/*
 * Copyright 2018 - 2021 Swiss Federal Institute of Technology Lausanne (EPFL)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This open source software code was developed in part or in whole in the
 * Human Brain Project, funded from the European Union's Horizon 2020
 * Framework Programme for Research and Innovation under
 * Specific Grant Agreements No. 720270, No. 785907, and No. 945539
 * (Human Brain Project SGA1, SGA2 and SGA3).
 *
 */

package eu.ebrains.kg.search.controller.data;

import eu.ebrains.kg.search.controller.utils.TranslatorTestHelper;
import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.ResultsOfKGv2;
import eu.ebrains.kg.search.model.source.openMINDSv1.ProjectV1;
import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchDocument;
import eu.ebrains.kg.search.services.KGV2ServiceClient;
import eu.ebrains.kg.search.services.KGV2SearchServiceClient;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProjectTranslatorTest {
    private final KGV2ServiceClient KGV2ServiceClient;

    public ProjectTranslatorTest(KGV2ServiceClient KGV2ServiceClient) {
        this.KGV2ServiceClient = KGV2ServiceClient;
    }

    private static class ProjectV1Results extends ResultsOfKGv2<ProjectV1> { }

    @Test
    public void compareReleasedProjects() {
        compareProjects(DataStage.RELEASED, false);
    }

    @Test
    public void compareInferredProjects() {
        compareProjects(DataStage.IN_PROGRESS, false);
    }

    @Test
    public void compareInferredLiveSubjects() {
        compareProjects(DataStage.IN_PROGRESS, true);
    }

    private void compareProjects(DataStage dataStage, boolean liveMode) {
        List<String> result = new ArrayList<>();
        ProjectV1Results queryResult = KGV2ServiceClient.executeQuery("query/minds/core/placomponent/v1.0.0/search", dataStage, ProjectV1Results.class);
        queryResult.getResults().forEach(project -> {
            String id = liveMode?project.getEditorId():project.getIdentifier();
            ElasticSearchDocument doc;
            if (liveMode) {
                doc = KGV2SearchServiceClient.getLiveDocument(id, ElasticSearchDocument.class);
            } else {
                doc = KGV2SearchServiceClient.getDocument(dataStage, "Project", id, ElasticSearchDocument.class);
            }
            if (doc == null) {
                result.add("\n\n\tProject: " + project.getIdentifier() + " (Fail to get expected document!)");
            } else {
                Map<String, Object> expected = doc.getSource();
                List<String> messages = TranslatorTestHelper.compareProject(project, expected, dataStage, liveMode);
                if (!messages.isEmpty()) {
                    result.add("\n\n\tProject: " + project.getIdentifier() + "\n\t\t" + String.join("\n\t\t", messages));
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
        List<String> result = TranslatorTestHelper.compareProject(sourceJson, expectedJson, DataStage.RELEASED, false);
        if (!result.isEmpty()) {
            Assert.fail("\n\t" + String.join("\n\t", result));
        }
    }

    @Test
    public void compareInferredProject() throws IOException {
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v1/projectInferredSource.json"), StandardCharsets.UTF_8);
        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/projectInferredTarget.json"), StandardCharsets.UTF_8);
        List<String> result = TranslatorTestHelper.compareProject(sourceJson, expectedJson, DataStage.IN_PROGRESS, false);
        if (!result.isEmpty()) {
            Assert.fail("\n\t" + String.join("\n\t", result));
        }

    }

    @Test
    public void compareInferredLiveProject() throws IOException {
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v1/projectInferredSource.json"), StandardCharsets.UTF_8);
        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/projectInferredLiveTarget.json"), StandardCharsets.UTF_8);
        List<String> result = TranslatorTestHelper.compareProject(sourceJson, expectedJson, DataStage.IN_PROGRESS, true);
        if (!result.isEmpty()) {
            Assert.fail("\n\t" + String.join("\n\t", result));
        }
    }
}
