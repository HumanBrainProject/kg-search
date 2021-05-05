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

package eu.ebrains.kg.search.controller.mapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import eu.ebrains.kg.search.controller.Constants;
import eu.ebrains.kg.search.utils.MetaModelUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

class MappingControllerTest {

    @Test
    void generateMapping() {
        //Given
        ObjectMapper mapper = new ObjectMapper().configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        MappingController mappingController = new MappingController(new MetaModelUtils());

        //When
        Constants.TARGET_MODELS_MAP.forEach((type, clazz) -> {
            System.out.printf("Now handling type: %s%n", type);
            Path path;
            String json;
            try {
                path = Paths.get(MappingControllerTest.class.getClassLoader().getResource(String.format("mappings/%sMapping.json", type.toLowerCase())).toURI());
                json = Files.lines(path).collect(Collectors.joining("\n"));
                Map expected = mapper.readValue(json, Map.class);
                Map<String, Object> mapping = mappingController.generateMapping(clazz);
                //Then
                Assertions.assertEquals(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(expected), mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapping));
            } catch (URISyntaxException | IOException e) {
                Assert.fail(e.getMessage());
            }
        });
    }
}