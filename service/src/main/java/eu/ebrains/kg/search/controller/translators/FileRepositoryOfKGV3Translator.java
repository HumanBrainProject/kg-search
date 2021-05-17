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
import eu.ebrains.kg.search.model.source.openMINDSv3.FileRepositoryV3;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.FileRepository;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.utils.IdUtils;

public class FileRepositoryOfKGV3Translator implements Translator<FileRepositoryV3, FileRepository> {

    public FileRepository translate(FileRepositoryV3 fileRepository, DataStage dataStage, boolean liveMode) {
        FileRepository f = new FileRepository();
        f.setId(IdUtils.getUUID(fileRepository.getId()));
        f.setIdentifier(IdUtils.getUUID(fileRepository.getIdentifier()));
        f.setIRI(fileRepository.getIRI());
        if (fileRepository.getDatasetVersion() != null) {
            f.setDatasetVersion(new TargetInternalReference(IdUtils.getUUID(fileRepository.getDatasetVersion().getId()), fileRepository.getDatasetVersion().getFullName()));
        }
        if (fileRepository.getMetaDataModelVersion() != null) {
            f.setMetaDataModelVersion(new TargetInternalReference(IdUtils.getUUID(fileRepository.getMetaDataModelVersion().getId()), fileRepository.getMetaDataModelVersion().getFullName()));
        }
        if (fileRepository.getModelVersion() != null) {
            f.setModelVersion(new TargetInternalReference(IdUtils.getUUID(fileRepository.getModelVersion().getId()), fileRepository.getModelVersion().getFullName()));
        }
        if (fileRepository.getSoftwareVersion() != null) {
            f.setSoftwareVersion(new TargetInternalReference(IdUtils.getUUID(fileRepository.getSoftwareVersion().getId()), fileRepository.getSoftwareVersion().getFullName()));
        }
        return f;
    }
}
