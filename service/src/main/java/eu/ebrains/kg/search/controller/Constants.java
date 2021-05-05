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

package eu.ebrains.kg.search.controller;

import eu.ebrains.kg.search.model.target.elasticsearch.instances.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Constants {

    public final static Integer esQuerySize = 10000;

    public final static List<Class<?>> TARGET_MODELS_ORDER = Arrays.asList(Project.class, Dataset.class, Subject.class, Sample.class, Model.class, Software.class, Contributor.class);

    public final static Map<String, Class<?>> TARGET_MODELS_MAP = Map.of(
            "Project", Project.class,
            "Subject", Subject.class,
            "Sample", Sample.class,
            "Model", Model.class,
            "Contributor", Contributor.class,
            "Software", Software.class,
            "Dataset", Dataset.class
    );

    public final static List<Map<String, String>> GROUPS = Arrays.asList(
            Map.of("name", "curated",
                    "label", "in progress"),
            Map.of("name", "public",
                    "label", "publicly released")
    );

    public final static List<String> TYPES_FOR_LIVE = Arrays.asList(
            "https://openminds.ebrains.eu/core/Dataset",
            "https://openminds.ebrains.eu/core/DatasetVersion",
            "https://openminds.ebrains.eu/core/Person",
            "https://openminds.ebrains.eu/core/Project",
            "https://openminds.ebrains.eu/core/Model",
            "https://openminds.ebrains.eu/core/Software",
            "https://openminds.ebrains.eu/core/Subject",
            "https://openminds.ebrains.eu/core/Sample"
    );

}
