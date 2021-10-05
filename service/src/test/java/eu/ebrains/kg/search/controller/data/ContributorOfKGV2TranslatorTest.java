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

import eu.ebrains.kg.search.model.source.ResultsOfKGv2;
import eu.ebrains.kg.search.model.source.openMINDSv1.PersonV1;
import eu.ebrains.kg.search.model.source.openMINDSv2.PersonV2;
import eu.ebrains.kg.search.services.KGV2ServiceClient;

public class ContributorOfKGV2TranslatorTest {

    private final KGV2ServiceClient KGV2ServiceClient;

    public ContributorOfKGV2TranslatorTest(KGV2ServiceClient KGV2ServiceClient) {
        this.KGV2ServiceClient = KGV2ServiceClient;
    }

    private static class PersonV1Results extends ResultsOfKGv2<PersonV1> {}

    private static class PersonV2Results extends ResultsOfKGv2<PersonV2> { }
//
//    @Test
//    public void compareReleasedContributors() {
//        compareContributors(DataStage.RELEASED, false);
//    }
//
//    @Test
//    public void compareInferredContributors() {
//        compareContributors(DataStage.IN_PROGRESS, false);
//    }
//
//    @Test
//    public void compareInferredLiveContributors() {
//        compareContributors(DataStage.IN_PROGRESS, true);
//    }

//    private void compareContributors(DataStage dataStage, boolean liveMode) {
//        Map<String, PersonSources> sourcesMap = new HashMap<>();
//
//        List<String> result = new ArrayList<>();
//        PersonV1Results personV1Result = KGV2ServiceClient.executeQueryForIndexing("query/minds/core/person/v1.0.0/search", dataStage, PersonV1Results.class);
//        PersonV2Results personV2Result = KGV2ServiceClient.executeQueryForIndexing("query/uniminds/core/person/v1.0.0/search", dataStage, PersonV2Results.class);
//
//        if (!CollectionUtils.isEmpty(personV1Result.getResults())) {
//            personV1Result.getResults().forEach(person -> {
//                String id = person.getIdentifier();
//                if (sourcesMap.containsKey(id)) {
//                    PersonSources sources = sourcesMap.get(id);
//                    sources.setPersonV1(person);
//                } else {
//                    PersonSources sources = new PersonSources();
//                    sourcesMap.put(id, sources);
//                    sources.setPersonV1(person);
//                }
//            });
//        }
//
//        if (!CollectionUtils.isEmpty(personV2Result.getResults())) {
//            personV2Result.getResults().forEach(person -> {
//                String id = person.getIdentifier();
//                if (sourcesMap.containsKey(id)) {
//                    PersonSources sources = sourcesMap.get(id);
//                    sources.setPersonV2(person);
//                } else {
//                    PersonSources sources = new PersonSources();
//                    sourcesMap.put(id, sources);
//                    sources.setPersonV2(person);
//                }
//            });
//        }
//        sourcesMap.forEach((key, personSources) -> {
//            PersonV1andV2 person = personSources.getPersonV2() != null ? personSources.getPersonV2() : personSources.getPersonV1();
//            String id = liveMode?person.getEditorId():person.getIdentifier();
//            ElasticSearchDocument doc;
//            if (liveMode) {
//                doc = KGV2SearchServiceClient.getLiveDocument(id, ElasticSearchDocument.class);
//            } else {
//                doc = KGV2SearchServiceClient.getDocument(dataStage, "Contributor", id, ElasticSearchDocument.class);
//            }
//            if (doc == null) {
//                result.add("\n\n\tContributor: " + person.getIdentifier() + " (Fail to get expected document!)");
//            } else {
//                Map<String, Object> expected = doc.getSource();
//                List<String> messages = TranslatorTestHelper.compareContributor(personSources, expected, dataStage, liveMode);
//                if (!messages.isEmpty()) {
//                    result.add("\n\n\tContributor: " + person.getIdentifier() + "\n\t\t" + String.join("\n\t\t", messages));
//                }
//            }
//        });
//        if (!result.isEmpty()) {
//            fail(String.join("", result));
//        }
//    }
//
//    @Test
//    public void compareReleasedContributorV1() throws IOException {
//        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v1/personReleasedSource.json"), StandardCharsets.UTF_8);
//        List<String> sourcesJson = new ArrayList<>(Arrays.asList(sourceJson));
//        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/contributorV1ReleasedTarget.json"), StandardCharsets.UTF_8);
//        List<String> result = TranslatorTestHelper.compareContributor(sourcesJson, expectedJson, DataStage.RELEASED, false);
//        if (!result.isEmpty()) {
//            fail("\n\t" + String.join("\n\t", result));
//        }
//    }
//    @Test
//    public void compareReleasedContributorV2() throws IOException {
//        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/personReleasedSource.json"), StandardCharsets.UTF_8);
//        List<String> sourcesJson = new ArrayList<>(Arrays.asList(null, sourceJson));
//        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/contributorV2ReleasedTarget.json"), StandardCharsets.UTF_8);
//        List<String> result = TranslatorTestHelper.compareContributor(sourcesJson, expectedJson, DataStage.RELEASED, false);
//        if (!result.isEmpty()) {
//            fail("\n\t" + String.join("\n\t", result));
//        }
//    }
//
//    @Test
//    public void compareInferredContributorV1() throws IOException {
//        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v1/personInferredSource.json"), StandardCharsets.UTF_8);
//        List<String> sourcesJson = new ArrayList<>(Arrays.asList(sourceJson));
//        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/contributorV1InferredTarget.json"), StandardCharsets.UTF_8);
//        List<String> result = TranslatorTestHelper.compareContributor(sourcesJson, expectedJson, DataStage.IN_PROGRESS, false);
//        if (!result.isEmpty()) {
//            fail("\n\t" + String.join("\n\t", result));
//        }
//    }
//    @Test
//    public void compareInferredContributorV2() throws IOException {
//        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/personInferredSource.json"), StandardCharsets.UTF_8);
//        List<String> sourcesJson = Arrays.asList(null, sourceJson);
//        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/contributorV2InferredTarget.json"), StandardCharsets.UTF_8);
//        List<String> result = TranslatorTestHelper.compareContributor(sourcesJson, expectedJson, DataStage.IN_PROGRESS, false);
//        if (!result.isEmpty()) {
//            fail("\n\t" + String.join("\n\t", result));
//        }
//    }
//
//    @Test
//    public void compareInferredLiveContributorV1() throws IOException {
//        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v1/personInferredSource.json"), StandardCharsets.UTF_8);
//        List<String> sourcesJson = Collections.singletonList(sourceJson);
//        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/contributorV1InferredLiveTarget.json"), StandardCharsets.UTF_8);
//        List<String> result = TranslatorTestHelper.compareContributor(sourcesJson, expectedJson, DataStage.IN_PROGRESS, true);
//        if (!result.isEmpty()) {
//            fail("\n\t" + String.join("\n\t", result));
//        }
//    }
//    @Test
//    public void compareInferredLiveContributorV2() throws IOException {
//        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/personInferredSource.json"), StandardCharsets.UTF_8);
//        List<String> sourcesJson = Arrays.asList(null, sourceJson);
//        String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/v2/contributorV2InferredLiveTarget.json"), StandardCharsets.UTF_8);
//        List<String> result = TranslatorTestHelper.compareContributor(sourcesJson, expectedJson, DataStage.IN_PROGRESS, true);
//        if (!result.isEmpty()) {
//            fail("\n\t" + String.join("\n\t", result));
//        }
//    }
}
