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

package eu.ebrains.kg.indexing.controller.settings;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static eu.ebrains.kg.indexing.controller.mapping.MappingController.TEXT_ANALYZER;

@Component
public class SettingsController {

    public Map<String, Object> generateSearchIndexSettings() {
        return Map.of(
                "analysis", Map.of(
                        "analyzer", Map.of(
                                "default", Map.of(
                                        "type", "standard"
                                ),
                                TEXT_ANALYZER, Map.of(
                                        "tokenizer", "custom_text_tokenizer",
                                        "filter", List.of("custom_word_delimiter_graph", "flatten_graph", "custom_length", "lowercase", "asciifolding", "stop")
                                )
                        ),
                        "tokenizer", Map.of(
                                "custom_text_tokenizer", Map.of(
                                        "type", "char_group",
                                        "tokenize_on_chars", List.of("whitespace")
                                )
                        ),
                        "filter", Map.of(
                                "custom_word_delimiter_graph", Map.of(
                                        "type", "word_delimiter_graph",
                                        "preserve_original", true
                                ),
                                "custom_length", Map.of(
                                        "type", "length",
                                        "min", 2
                                )
                        )

                )
        );
    }
}
