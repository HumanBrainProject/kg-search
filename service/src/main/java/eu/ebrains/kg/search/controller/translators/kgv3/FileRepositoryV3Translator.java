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

package eu.ebrains.kg.search.controller.translators.kgv3;

import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.TranslatorModel;
import eu.ebrains.kg.search.model.source.ResultsOfKGv3;
import eu.ebrains.kg.search.model.source.openMINDSv3.FileRepositoryV3;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.*;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.utils.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

import static eu.ebrains.kg.search.controller.translators.kgv3.TranslatorOfKGV3Commons.*;

public class FileRepositoryV3Translator extends TranslatorV3<FileRepositoryV3, FileRepository, FileRepositoryV3Translator.Result> {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    public static class Result extends ResultsOfKGv3<FileRepositoryV3> {
    }

    @Override
    public Class<FileRepositoryV3> getSourceType() {
        return FileRepositoryV3.class;
    }

    @Override
    public Class<FileRepository> getTargetType() {
        return FileRepository.class;
    }

    @Override
    public Class<Result> getResultType() {
        return Result.class;
    }

    @Override
    public List<String> getQueryIds() {
        return Collections.singletonList("536c37ed-f8cd-40b1-91ab-d193509e348c");
    }

    @Override
    public List<String> semanticTypes() {
        return Collections.singletonList("https://openminds.ebrains.eu/core/FileRepository");
    }

    public FileRepository translate(FileRepositoryV3 fileRepository, DataStage dataStage, boolean liveMode) {
        FileRepository f = new FileRepository();
        f.setId(IdUtils.getUUID(fileRepository.getId()));
        f.setIdentifier(IdUtils.getUUID(fileRepository.getIdentifier()));
        f.setIRI(fileRepository.getIRI());
        FileRepositoryV3.FileRepositoryOfReference fileRepositoryOf = fileRepository.getFileRepositoryOf();
        if (fileRepositoryOf != null) {
            List<String> types = fileRepositoryOf.getType();
            if (!CollectionUtils.isEmpty(types)) {
                String type = types.get(0);
                if (type != null) {
                    TargetInternalReference reference = new TargetInternalReference(IdUtils.getUUID(fileRepositoryOf.getId()), fileRepositoryOf.getFullName());
                    final TranslatorModel<?, ?, ?, ?> modelByType = TranslatorModel.getModelByType(type);
                    if(modelByType==null){
                        logger.error(String.format("No model can be found for type %s - we skip", type));
                    }
                    else if(modelByType.getTargetClass() == DatasetVersion.class){
                        f.setDatasetVersion(reference);
                    }
                    else if(modelByType.getTargetClass() == ModelVersion.class){
                        f.setDatasetVersion(reference);
                    }
                    else if(modelByType.getTargetClass() == SoftwareVersion.class){
                        f.setSoftwareVersion(reference);
                    }
                    //else if(modelByType.getTargetClass() == MetadataModelVersion.class){
                    // f.setMetaDataModelVersion(reference);
                    // }
                }
            }

            if (liveMode) {
                f.setFilesAsyncUrl(String.format("/api/repositories/%s/files/live", IdUtils.getUUID(fileRepository.getId())));
            } else {
                if (hasEmbargoStatus(fileRepositoryOf, RESTRICTED_ACCESS)) {
                    String hdgMessage = String.format("This data requires you to explicitly **[request access](https://data-proxy.ebrains.eu/datasets/%s)** with your EBRAINS account. If you don't have such an account yet, please **[register](https://ebrains.eu/register/)**.", IdUtils.getUUID(fileRepositoryOf.getId()));
                    String hdgMessageHTML = String.format("This data requires you to explicitly <b><a href=\"https://data-proxy.ebrains.eu/datasets/%s\" target=\"_blank\">request access</a></b> with your EBRAINS account. If you don't have such an account yet, please <b><a href=\"https://ebrains.eu/register/\" target=\"_blank\">register</a></b>.", IdUtils.getUUID(fileRepositoryOf.getId()));
                    f.setUseHDG(hdgMessage);
                    f.setEmbargo(hdgMessageHTML);
                } else {
                    if (dataStage == DataStage.RELEASED) {
                        if (hasEmbargoStatus(fileRepositoryOf, UNDER_EMBARGO)) {
                            f.setEmbargo("Those files are temporarily under embargo. The data will become available for download after the embargo period.");
                        } else if (hasEmbargoStatus(fileRepositoryOf, CONTROLLED_ACCESS)) {
                            f.setEmbargo("Those files are currently reviewed by the Data Protection Office regarding GDPR compliance. The data will be available after this review.");
                        }
                    }
                    if ((dataStage == DataStage.IN_PROGRESS || (dataStage == DataStage.RELEASED && hasEmbargoStatus(fileRepositoryOf, FREE_ACCESS)))) {
                        f.setFilesAsyncUrl(String.format("/api/groups/%s/repositories/%s/files", dataStage.equals(DataStage.IN_PROGRESS) ? "curated" : "public", IdUtils.getUUID(fileRepository.getId())));
                    }
                }
            }
        }
      return f;
    }
}
