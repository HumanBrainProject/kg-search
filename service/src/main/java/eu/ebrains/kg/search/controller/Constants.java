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

    public final static String OPENMINDS_CORE_NAMESPACE = "https://openminds.ebrains.eu/core/";

    public final static String SOURCE_MODEL_DATASET = OPENMINDS_CORE_NAMESPACE + "Dataset";
    public final static String SOURCE_MODEL_DATASET_VERSIONS = OPENMINDS_CORE_NAMESPACE + "DatasetVersion";
    public final static String SOURCE_MODEL_PERSON = OPENMINDS_CORE_NAMESPACE + "Person";
    public final static String SOURCE_MODEL_PROJECT = OPENMINDS_CORE_NAMESPACE + "Project";
    public final static String SOURCE_MODEL_SUBJECT = OPENMINDS_CORE_NAMESPACE + "Subject";
    public final static String SOURCE_MODEL_SAMPLE = OPENMINDS_CORE_NAMESPACE + "Sample";
    public final static String SOURCE_MODEL_MODEL = OPENMINDS_CORE_NAMESPACE + "Model";
    public final static String SOURCE_MODEL_MODEL_VERSION = OPENMINDS_CORE_NAMESPACE + "ModelVersion";
    public final static String SOURCE_MODEL_SOFTWARE = OPENMINDS_CORE_NAMESPACE + "Software";
    public final static String SOURCE_MODEL_SOFTWARE_VERSION = OPENMINDS_CORE_NAMESPACE + "SoftwareVersion";

    public final static List<String> SOURCE_MODELS = Arrays.asList(
            SOURCE_MODEL_DATASET,
            SOURCE_MODEL_DATASET_VERSIONS,
            SOURCE_MODEL_PERSON,
            SOURCE_MODEL_PROJECT,
            SOURCE_MODEL_SUBJECT,
            SOURCE_MODEL_SAMPLE,
            SOURCE_MODEL_MODEL,
            SOURCE_MODEL_MODEL_VERSION,
            SOURCE_MODEL_SOFTWARE,
            SOURCE_MODEL_SOFTWARE_VERSION
    );

    public final static List<Class<?>> TARGET_MODELS_ORDER = Arrays.asList(
            Project.class,
            DatasetVersion.class,
            Subject.class,
            Sample.class,
            ModelVersion.class,
            SoftwareVersion.class,
            Contributor.class,
            Dataset.class,
            Model.class,
            Software.class
    );

    public final static List<Map<String, String>> GROUPS = Arrays.asList(
            Map.of("name", "curated",
                    "label", "in progress"),
            Map.of("name", "public",
                    "label", "publicly released")
    );
}
