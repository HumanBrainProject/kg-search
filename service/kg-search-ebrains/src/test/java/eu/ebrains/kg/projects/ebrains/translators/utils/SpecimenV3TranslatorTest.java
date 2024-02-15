/*
 *  Copyright 2018 - 2021 Swiss Federal Institute of Technology Lausanne (EPFL)
 *  Copyright 2021 - 2023 EBRAINS AISBL
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0.
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  This open source software code was developed in part or in whole in the
 *  Human Brain Project, funded from the European Union's Horizon 2020
 *  Framework Programme for Research and Innovation under
 *  Specific Grant Agreements No. 720270, No. 785907, and No. 945539
 *  (Human Brain Project SGA1, SGA2 and SGA3).
 */

package eu.ebrains.kg.projects.ebrains.translators.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.ebrains.kg.common.model.target.BasicHierarchyElement;
import eu.ebrains.kg.projects.ebrains.source.DatasetVersionV3;
import eu.ebrains.kg.projects.ebrains.target.DatasetVersion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SpecimenV3TranslatorTest {
    private final ObjectMapper objectMapper;

    public SpecimenV3TranslatorTest() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @BeforeEach
    void TestPayload() {

    }

    private void testSpecimenTranslation(String test, String id) {
        //given
        SpecimenTranslator translator = new SpecimenTranslator(String.format("https://kg.ebrains.eu/api/instances/%s", id), new ArrayList<>(), Collections.emptyMap());

        final List<DatasetVersionV3.StudiedSpecimen> payload = parseSource(test);
        final BasicHierarchyElement<?> expectation = parseTarget(test);
        clearKeysDueToDynamicity(expectation);

        //when
        final BasicHierarchyElement<DatasetVersion.DSVSpecimenOverview> result = translator.translateToHierarchy(payload);
        clearKeysDueToDynamicity(result);

        //then
        try {
            final String serializedResult = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
            final String serializedExpectation = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(expectation);
            Assertions.assertEquals(serializedExpectation, serializedResult);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Was not able to serialize result or expectation", e);
        }

    }


    @Test
    void tissueSamplesWithSingleStateAttachedToSubjectsWithSingleState() {
        testSpecimenTranslation("tissueSamplesWithSingleStateAttachedToSubjectsWithSingleState", "fe3da318-c8aa-4c83-ba64-1f0c74b00700");
    }

    @Test
    void tissueSampleCollectionFromOtherTissueSampleWithSubselection() {
        testSpecimenTranslation("tissueSampleCollectionFromOtherTissueSampleWithSubselection", "d71d369a-c401-4d7e-b97a-3fb78eed06c5");
    }

    @Test
    void nestedTissueSampleCollectionsWithSingleStatesAttachedToASubject() {
        testSpecimenTranslation("nestedTissueSampleCollectionsWithSingleStatesAttachedToASubject", "0bf058d2-6bf7-4e0f-8067-345e07109bf8");
    }

    @Test
    void tissueSampleFullyConnectedToASubjectState() {
        testSpecimenTranslation("tissueSampleFullyConnectedToASubjectState", "7f6e14f0-ab5c-4328-9e0e-01b260edd357");
    }

    @Test
    void subjectsWithTwoStatesAndAttachedTissueSampleCollections() {
        testSpecimenTranslation("subjectsWithTwoStatesAndAttachedTissueSampleCollections", "4660e79b-a731-40ac-905e-46d0d11c0dd5");
    }

    @Test
    void tissueSampleCollectionsWithServiceLinks(){
        testSpecimenTranslation("tissueSampleCollectionsWithServiceLinks", "77aa9e4c-fd9c-493c-9af4-0988284ffa95");
    }

    @Test
    void tissueSampleCollectionWithoutSubject(){
        testSpecimenTranslation("tissueSampleCollectionWithoutSubject", "89ddf976-e732-4eef-be48-08af62cfe40b");
    }

    @Test
    void tissueSampleInTissueSampleCollection(){
        testSpecimenTranslation("tissueSampleInTissueSampleCollection", "95f44822-6247-4d7a-a232-23a5247dd91d");
    }

    @Test
    void twoTissueSamplesInTissueSampleCollection(){
        testSpecimenTranslation("twoTissueSamplesInTissueSampleCollection", "103068ab-8993-4f0c-94a8-b55a6b99f109");
    }

    @Test
    void tissueSampleWithTwoStatesInTissueSampleCollection(){
        testSpecimenTranslation("tissueSampleWithTwoStatesInTissueSampleCollection", "b4a37f80-e231-4a27-92ca-f47de7b2208d");
    }

    @Test
    void tissueSampleWithoutSubjectAndCollectionButWithMultipleSexAndSpecies(){
        testSpecimenTranslation("tissueSampleWithoutSubjectAndCollectionButWithMultipleSexAndSpecies", "2d3757b5-afc8-470d-988e-f382884cf382");
    }

    @Test
    void subjectsWithMultipleStatesDependingOnEachOther(){
        testSpecimenTranslation("subjectsWithMultipleStatesDependingOnEachOther", "84180d1d-b639-4074-9bd2-768b69550656");
    }

    private List<DatasetVersionV3.StudiedSpecimen> parseSource(String test) {
        try {
            return objectMapper.readValue(new File(Objects.requireNonNull(getClass().getClassLoader().getResource(String.format("%s_source.json", test))).getFile()), new TypeReference<List<DatasetVersionV3.StudiedSpecimen>>() {});
        } catch (IOException e) {
            throw new RuntimeException(String.format("Was not able to parse source for %s", test), e);
        }
    }

    private BasicHierarchyElement<DatasetVersion.DSVSpecimenOverview> parseTarget(String test) {
        try {
            return objectMapper.readValue(new File(Objects.requireNonNull(getClass().getClassLoader().getResource(String.format("%s_target.json", test))).getFile()), new TypeReference<BasicHierarchyElement<DatasetVersion.DSVSpecimenOverview>>() {});
        } catch (IOException e) {
            throw new RuntimeException(String.format("Was not able to parse target for %s", test), e);
        }
    }

    private void clearKeysDueToDynamicity(BasicHierarchyElement<?> hierarchyElement){
        hierarchyElement.setKey(null);
        if(!CollectionUtils.isEmpty(hierarchyElement.getChildren())){
            hierarchyElement.getChildren().forEach(this::clearKeysDueToDynamicity);
        }
    }

}
