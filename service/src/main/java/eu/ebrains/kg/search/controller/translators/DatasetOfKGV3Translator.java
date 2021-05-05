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

package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.openMINDSv3.DatasetV3;
import eu.ebrains.kg.search.model.source.openMINDSv3.DatasetVersionV3;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Dataset;
import eu.ebrains.kg.search.utils.IdUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public class DatasetOfKGV3Translator implements  VersionedTranslator<DatasetV3, Dataset>{

    public Dataset translate(DatasetV3 datasetV3, DataStage dataStage, boolean liveMode, String versionIdentifier) {
        Dataset d = new Dataset();
        DatasetVersionV3 datasetVersion = getDatasetVersion(datasetV3.getDatasetVersions(), versionIdentifier);
        if(datasetVersion != null) {
            d.setVersion(versionIdentifier);
            d.setId(IdUtils.getUUID(datasetVersion.getId()));
            d.setIdentifier(IdUtils.getUUID(datasetVersion.getIdentifier()));
            d.setVersions(datasetV3.getDatasetVersions());
            d.addDatasetToVersions(datasetV3);
            if (StringUtils.isBlank(datasetVersion.getDescription())) {
                d.setDescription(datasetV3.getDescription());
            } else {
                d.setDescription(datasetVersion.getDescription());
            }
//            if (StringUtils.isBlank(datasetVersion.getFullName())) {
//                d.setTitle(datasetV3.getFullName());
//            } else {
//                d.setTitle(datasetVersion.getFullName());
//            }
            // For the UI we don't need the version number in the title as it is set in de dropdown
            d.setTitle(datasetV3.getFullName());
        } else {
            d.setId(IdUtils.getUUID(datasetV3.getId()));
            d.setIdentifier(IdUtils.getUUID(datasetV3.getIdentifier()));
            d.setDescription(datasetV3.getDescription());
            d.setTitle(datasetV3.getFullName());
            d.setVersions(datasetV3.getDatasetVersions());
            d.addDatasetToVersions(datasetV3);
        }
        return d;
    }

    private DatasetVersionV3 getDatasetVersion(List<DatasetVersionV3> datasetVersions, String versionIdentifier) {
        if (datasetVersions != null && versionIdentifier != null) {
            return datasetVersions.stream().filter(d -> d.getVersionIdentifier().equals(versionIdentifier)).collect(Collectors.toList()).stream().findFirst().orElse(null);
        }
        return null;
    }

}
