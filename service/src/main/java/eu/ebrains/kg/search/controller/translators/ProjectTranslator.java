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
import eu.ebrains.kg.search.model.source.openMINDSv1.ProjectV1;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Project;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.utils.IdUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectTranslator implements Translator<ProjectV1, Project> {

    public Project translate(ProjectV1 projectSource, DataStage dataStage, boolean liveMode) {
        Project p = new Project();
        String uuid = IdUtils.getUUID(projectSource.getId());
        p.setId(uuid);
        List<String> identifiers = Arrays.asList(uuid, String.format("Project/%s", projectSource.getIdentifier()));
        p.setIdentifier(identifiers);
        p.setFirstRelease(projectSource.getFirstReleaseAt());
        p.setDescription(projectSource.getDescription());
        p.setLastRelease(projectSource.getLastReleaseAt());
        if(!CollectionUtils.isEmpty(projectSource.getDatasets())) {
            p.setDataset(projectSource.getDatasets().stream()
                    .map(dataset ->
                            new TargetInternalReference(
                                    liveMode ? dataset.getRelativeUrl() : String.format("Dataset/%s", dataset.getIdentifier()),
                                    dataset.getName(), null))
                    .collect(Collectors.toList()));
        }
        p.setTitle(projectSource.getTitle());
        if(!CollectionUtils.isEmpty(projectSource.getPublications())) {
            p.setPublications(projectSource.getPublications().stream()
                    .map(publication -> {
                        String doi;
                        if(StringUtils.isNotBlank(publication.getDoi())) {
                            String url = URLEncoder.encode(publication.getDoi(), StandardCharsets.UTF_8);
                            doi = String.format("[DOI: %s]\n[DOI: %s]: https://doi.org/%s", publication.getDoi(), publication.getDoi(), url);
                        } else {
                            doi = "[DOI: null]\n[DOI: null]: https://doi.org/null";
                        }
                        if (StringUtils.isNotBlank(publication.getCitation())) {
                            return publication.getCitation() + "\n" + doi;
                        } else {
                            return doi;
                        }
                    }).collect(Collectors.toList()));
        }
        if (dataStage == DataStage.IN_PROGRESS) {
            p.setEditorId(projectSource.getEditorId());
        }
        return p;
    }
}
