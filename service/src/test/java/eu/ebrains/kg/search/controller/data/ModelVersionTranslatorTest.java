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
import eu.ebrains.kg.search.model.source.openMINDSv2.ModelV2;
import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchDocument;
import eu.ebrains.kg.search.services.KGV2SearchServiceClient;
import eu.ebrains.kg.search.services.KGV2ServiceClient;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModelVersionTranslatorTest {
    private final KGV2ServiceClient KGV2ServiceClient;

    public ModelVersionTranslatorTest(KGV2ServiceClient KGV2ServiceClient) {
        this.KGV2ServiceClient = KGV2ServiceClient;
    }

    private static class ModelV2Results extends ResultsOfKGv2<ModelV2> { }

    @Test
    public void compareReleasedModels() {
        compareModels(DataStage.RELEASED, false);
    }

    @Test
    public void compareInferredModels() {
        compareModels(DataStage.IN_PROGRESS, false);
    }

    @Test
    public void compareInferredLiveModels() {
        compareModels(DataStage.IN_PROGRESS, true);
    }

    private void compareModels(DataStage dataStage, boolean liveMode) {
        List<String> result = new ArrayList<>();
        ModelV2Results queryResult = KGV2ServiceClient.executeQuery("query/uniminds/core/modelinstance/v1.0.0/search", dataStage, ModelV2Results.class);
        queryResult.getResults().forEach(project -> {
            String id = liveMode?project.getEditorId():project.getIdentifier();
            ElasticSearchDocument doc;
            if (liveMode) {
                doc = KGV2SearchServiceClient.getLiveDocument(id, ElasticSearchDocument.class);
            } else {
                doc = KGV2SearchServiceClient.getDocument(dataStage, "Model", id, ElasticSearchDocument.class);
            }
            if (doc == null) {
                result.add("\n\n\tModel: " + project.getIdentifier() + " (Fail to get expected document!)");
            } else {
                Map<String, Object> expected = doc.getSource();
                List<String> messages = TranslatorTestHelper.compareModel(project, expected, dataStage, liveMode);
                if (!messages.isEmpty()) {
                    result.add("\n\n\tModel: " + project.getIdentifier() + "\n\t\t" + String.join("\n\t\t", messages));
                }
            }
        });
        if (!result.isEmpty()) {
            Assert.fail(String.join("", result));
        }
    }

    @Test
    public void compareReleasedModel() throws IOException {
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/modelReleasedSource.json"), StandardCharsets.UTF_8);
        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/modelReleasedTarget.json"), StandardCharsets.UTF_8);
        List<String> result = TranslatorTestHelper.compareModel(sourceJson, expectedJson, DataStage.RELEASED, false);
        if (!result.isEmpty()) {
            Assert.fail("\n\t" + String.join("\n\t", result));
        }
    }

    @Test
    public void compareInferredSoftware() throws IOException {
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/modelInferredSource.json"), StandardCharsets.UTF_8);
        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/modelInferredTarget.json"), StandardCharsets.UTF_8);
        List<String> result = TranslatorTestHelper.compareModel(sourceJson, expectedJson, DataStage.IN_PROGRESS, false);
        if (!result.isEmpty()) {
            Assert.fail("\n\t" + String.join("\n\t", result));
        }

    }

    @Test
    public void compareInferredLiveSoftware() throws IOException {
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/modelInferredSource.json"), StandardCharsets.UTF_8);
        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/modelInferredLiveTarget.json"), StandardCharsets.UTF_8);
        List<String> result = TranslatorTestHelper.compareModel(sourceJson, expectedJson, DataStage.IN_PROGRESS, true);
        if (!result.isEmpty()) {
            Assert.fail("\n\t" + String.join("\n\t", result));
        }
    }
}