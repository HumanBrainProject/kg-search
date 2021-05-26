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
import eu.ebrains.kg.search.model.source.openMINDSv3.ModelVersionV3;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Version;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.ModelVersion;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetExternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.utils.IdUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static eu.ebrains.kg.search.controller.translators.TranslatorCommons.*;
import static eu.ebrains.kg.search.controller.translators.TranslatorOfKGV3Commons.*;

public class ModelVersionOfKGV3Translator implements Translator<ModelVersionV3, ModelVersion> {

    public ModelVersion translate(ModelVersionV3 modelVersion, DataStage dataStage, boolean liveMode) {
        ModelVersion m = new ModelVersion();
        ModelVersionV3.ModelVersions model = modelVersion.getModel();
        m.setVersion(modelVersion.getVersion());
        m.setId(IdUtils.getUUID(modelVersion.getId()));
        m.setIdentifier(IdUtils.getUUID(modelVersion.getIdentifier()));

        List<Version> versions = model == null?null:model.getVersions();
        if (!CollectionUtils.isEmpty(versions)) {
            List<Version> sortedVersions = Helpers.sort(versions);
            List<TargetInternalReference> references = sortedVersions.stream().map(v -> new TargetInternalReference(IdUtils.getUUID(v.getId()), v.getVersionIdentifier())).collect(Collectors.toList());
            references.add(new TargetInternalReference(IdUtils.getUUID(model.getId()), "All versions"));
            m.setVersions(references);
            // if versions cannot be sorted (sortedVersions == versions) we flag it as searchable
            m.setSearchable(sortedVersions == versions || sortedVersions.get(0).getId().equals(modelVersion.getId()));
        } else {
            m.setSearchable(true);
        }
        if (!StringUtils.isBlank(modelVersion.getDescription())) {
            m.setDescription(modelVersion.getDescription());
        } else if (model != null) {
            m.setDescription(model.getDescription());
        }
//        if (!StringUtils.isBlank(modelVersion.getFullName())) {
//            m.setTitle(modelVersion.getFullName());
//        } else if (model != null {
//            m.setTitle(model.getFullName());
//        }
        // For the UI we don't need the version number in the title as it is set in de dropdown
        if (model != null) {
            m.setTitle(model.getFullName());
            m.setModel(new TargetInternalReference(IdUtils.getUUID(model.getId()), model.getFullName()));
        }

        if (hasEmbargoStatus(modelVersion, UNDER_EMBARGO)) {
            if (dataStage == DataStage.RELEASED) {
                m.setEmbargo("This model is temporarily under embargo. The data will become available for download after the embargo period.");
            } else {
                String fileBundle = firstItemOrNull(modelVersion.getFileBundle());
                if (fileBundle != null) {
                    if (StringUtils.isNotBlank(fileBundle) && fileBundle.startsWith("https://object.cscs.ch")) {
                        m.setEmbargo(String.format("This model is temporarily under embargo. The data will become available for download after the embargo period.<br/><br/>If you are an authenticated user, <a href=\"https://kg.ebrains.eu/files/cscs/list?url=%s\" target=\"_blank\"> you should be able to access the data here</a>", fileBundle));
                    } else {
                        m.setEmbargo("This model is temporarily under embargo. The data will become available for download after the embargo period.");
                    }
                }
            }
        }
        if (!hasEmbargoStatus(modelVersion, UNDER_EMBARGO) && !CollectionUtils.isEmpty(modelVersion.getFileBundle())) {
            m.setAllFiles(modelVersion.getFileBundle().stream()
                    .map(fb -> {
                        if (fb.startsWith("https://object.cscs.ch")) {
                            return new TargetExternalReference(
                                    String.format("https://kg.ebrains.eu/proxy/export?container=%s", fb),
                                    "download all related data as ZIP" // TODO: Capitalize the value
                            );
                        } else {
                            return new TargetExternalReference(
                                    fb,
                                    "Go to the data"
                            );
                        }
                    }).collect(Collectors.toList()));
        }
        m.setModelFormat(emptyToNull(modelVersion.getFormat()));
        m.setDescription(modelVersion.getDescription());

        ModelVersionV3.License license = firstItemOrNull(modelVersion.getLicense());
        if (license != null) {
            m.setLicenseInfo(new TargetExternalReference(license.getWebpage(), license.getFullName()));
        }

        if (!CollectionUtils.isEmpty(modelVersion.getCustodian())) {
            m.setOwners(modelVersion.getCustodian().stream()
                    .map(o -> new TargetInternalReference(
                            IdUtils.getUUID(o.getId()),
                            Helpers.getFullName(o.getFullName(), o.getFamilyName(), o.getGivenName())
                    )).collect(Collectors.toList()));
        }

        if (model != null) {
            m.setAbstractionLevel(emptyToNull(model.getAbstractionLevel()));
            m.setStudyTarget(emptyToNull(model.getStudyTarget()));
            m.setModelScope(emptyToNull(model.getModelScope()));
        }

        String citation = modelVersion.getHowToCite();
        if (!CollectionUtils.isEmpty(modelVersion.getPublications())) {
            m.setPublications(emptyToNull(modelVersion.getPublications().stream()
                    .map(p -> {
                        if (StringUtils.isNotBlank(citation)) {
                            String url = URLEncoder.encode(p, StandardCharsets.UTF_8);
                            return citation + "\n" + String.format("[DOI: %s]\\n[DOI: %s]: https://doi.org/%s\"", p, p, url);
                        } else {
                            return p;
                        }
                    }).collect(Collectors.toList())));
        }
        if (!CollectionUtils.isEmpty(modelVersion.getDeveloper())) {
            m.setContributors(modelVersion.getDeveloper().stream()
                    .map(c -> new TargetInternalReference(
                            IdUtils.getUUID(c.getId()),
                            Helpers.getFullName(c.getFullName(), c.getFamilyName(), c.getGivenName())
                    )).collect(Collectors.toList()));
        }
      return m;
    }
}
