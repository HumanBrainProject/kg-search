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

package eu.ebrains.kg.search.controller.translators.kgv2;

import eu.ebrains.kg.search.controller.translators.Translator;
import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.ResultsOfKGv2;
import eu.ebrains.kg.search.model.source.commonsV1andV2.SourceExternalReference;
import eu.ebrains.kg.search.model.source.openMINDSv2.ModelV2;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.ModelVersion;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetExternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.utils.IdUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static eu.ebrains.kg.search.controller.translators.TranslatorCommons.*;
import static eu.ebrains.kg.search.controller.translators.kgv2.TranslatorOfKGV2Commons.*;

public class ModelV2Translator extends TranslatorV2<ModelV2, ModelVersion, ModelV2Translator.Result> {

    public static class Result extends ResultsOfKGv2<ModelV2> {
    }

    @Override
    public Class<ModelV2> getSourceType() {
        return ModelV2.class;
    }

    @Override
    public Class<ModelVersion> getTargetType() {
        return ModelVersion.class;
    }

    @Override
    public Class<Result> getResultType() {
        return Result.class;
    }

    @Override
    public List<String> getQueryIds() {
        return Collections.singletonList("uniminds/core/modelinstance/v1.0.0");
    }

    public ModelVersion translate(ModelV2 modelV2, DataStage dataStage, boolean liveMode) {
        ModelVersion m = new ModelVersion();
        m.setId(modelV2.getIdentifier());
        List<String> identifiers = Arrays.asList(modelV2.getIdentifier(), String.format("Model/%s", modelV2.getIdentifier()));
        m.setIdentifier(identifiers);
        if (dataStage == DataStage.IN_PROGRESS) {
            m.setEditorId(modelV2.getEditorId());
        }
        if (hasEmbargoStatus(modelV2, EMBARGOED)) {
            if (dataStage == DataStage.RELEASED) {
                m.setEmbargo("This model is temporarily under embargo. The data will become available for download after the embargo period.");
            } else {
                SourceExternalReference fileBundle = firstItemOrNull(modelV2.getFileBundle());
                if (fileBundle != null) {
                    String fileUrl = fileBundle.getUrl();
                    if (StringUtils.isNotBlank(fileUrl) && fileUrl.startsWith("https://object.cscs.ch")) {
                        m.setEmbargo(String.format("This model is temporarily under embargo. The data will become available for download after the embargo period.<br/><br/>If you are an authenticated user, <a href=\"https://kg.ebrains.eu/files/cscs/list?url=%s\" target=\"_blank\"> you should be able to access the data here</a>", fileUrl));
                    } else {
                        m.setEmbargo("This model is temporarily under embargo. The data will become available for download after the embargo period.");
                    }
                }
            }
        }
        if (!CollectionUtils.isEmpty(modelV2.getProducedDataset())) {
            m.setProducedDataset(modelV2.getProducedDataset().stream()
                    .map(pd -> new TargetInternalReference(
                            liveMode ? pd.getRelativeUrl() : String.format("Dataset/%s", pd.getIdentifier()),
                            pd.getName()
                    )).collect(Collectors.toList()));
        }
        if (!hasEmbargoStatus(modelV2, EMBARGOED) && !CollectionUtils.isEmpty(modelV2.getFileBundle())) {
            m.setAllFiles(modelV2.getFileBundle().stream()
                    .map(fb -> {
                        if (fb.getUrl().startsWith("https://object.cscs.ch")) {
                            return new TargetExternalReference(
                                    String.format("https://kg.ebrains.eu/proxy/export?container=%s", fb.getUrl()),
                                    "download all related data as ZIP" // TODO: Capitalize the value
                            );
                        } else {
                            return new TargetExternalReference(
                                    fb.getUrl(),
                                    "Go to the data"
                            );
                        }
                    }).collect(Collectors.toList()));
        }
        m.setModelFormat(emptyToNull(modelV2.getModelFormat()));
        m.setDescription(modelV2.getDescription());

        SourceExternalReference license = firstItemOrNull(modelV2.getLicense());
        if (license != null) {
            m.setLicenseInfo(new TargetExternalReference(license.getUrl(), license.getName()));
        }

        if (!CollectionUtils.isEmpty(modelV2.getCustodian())) {
            m.setOwners(modelV2.getCustodian().stream()
                    .map(o -> new TargetInternalReference(
                            liveMode ? o.getRelativeUrl() : String.format("Contributor/%s", o.getIdentifier()),
                            o.getName()
                    )).collect(Collectors.toList()));
        }

        m.setAbstractionLevel(emptyToNull(modelV2.getAbstractionLevel()));

        if (!CollectionUtils.isEmpty(modelV2.getMainContact())) {
            m.setMainContact(modelV2.getMainContact().stream()
                    .map(mc -> new TargetInternalReference(
                            liveMode ? mc.getRelativeUrl() : String.format("Contributor/%s", mc.getIdentifier()),
                            mc.getName()
                    )).collect(Collectors.toList()));
        }
        m.setBrainStructures(emptyToNull(modelV2.getBrainStructure()));

        if (!CollectionUtils.isEmpty(modelV2.getUsedDataset())) {
            m.setUsedDataset(modelV2.getUsedDataset().stream()
                    .map(ud -> new TargetInternalReference(
                            liveMode ? ud.getRelativeUrl() : String.format("Dataset/%s", ud.getIdentifier()),
                            ud.getName()
                    )).collect(Collectors.toList()));
        }
        m.setVersion(modelV2.getVersion());
        if (!CollectionUtils.isEmpty(modelV2.getPublications())) {
            m.setPublications(emptyToNull(modelV2.getPublications().stream()
                    .filter(p -> StringUtils.isNotBlank(p.getDoi()))
                    .map(p -> {
                        if (StringUtils.isNotBlank(p.getCitation())) {
                            String url = URLEncoder.encode(p.getDoi(), StandardCharsets.UTF_8);
                            return p.getCitation() + "\n" + String.format("[DOI: %s]\\n[DOI: %s]: https://doi.org/%s\"", p.getDoi(), p.getDoi(), url);
                        } else {
                            return p.getDoi();
                        }
                    }).collect(Collectors.toList())));
        }
        m.setStudyTarget(emptyToNull(modelV2.getStudyTarget()));
        m.setModelScope(emptyToNull(modelV2.getModelScope()));
        m.setTitle(modelV2.getTitle());
        if (!CollectionUtils.isEmpty(modelV2.getContributors())) {
            m.setContributors(modelV2.getContributors().stream()
                    .map(c -> new TargetInternalReference(
                            liveMode ? c.getRelativeUrl() : String.format("Contributor/%s", c.getIdentifier()),
                            c.getName()
                    )).collect(Collectors.toList()));
        }
        m.setCellularTarget(emptyToNull(modelV2.getCellularTarget()));
        m.setFirstRelease(modelV2.getFirstReleaseAt());
        m.setLastRelease(modelV2.getLastReleaseAt());
        m.setSearchable(true);
        return m;
    }
}
