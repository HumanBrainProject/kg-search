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
import eu.ebrains.kg.search.model.source.openMINDSv3.ProjectV3;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Project;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.utils.IdUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class ProjectOfKGV3Translator  implements Translator<ProjectV3, Project>{

    public Project translate(ProjectV3 project, DataStage dataStage, boolean liveMode) {
        Project p = new Project();
        String uuid = IdUtils.getUUID(project.getId());
        p.setId(uuid);
        p.setIdentifier(project.getIdentifier());
        p.setDescription(project.getDescription());
        if(!CollectionUtils.isEmpty(project.getDatasets())) {
            p.setDataset(project.getDatasets().stream()
                    .map(dataset ->
                            new TargetInternalReference(
                                    IdUtils.getUUID(dataset.getId()),
                                    dataset.getFullName()))
                    .collect(Collectors.toList()));
        }
        p.setTitle(project.getTitle());
        if(!CollectionUtils.isEmpty(project.getPublications())) {
            p.setPublications(project.getPublications().stream()
                    .map(publication -> {
                        String doi;
                        if(StringUtils.isNotBlank(publication.getDoi())) {
                            String url = URLEncoder.encode(publication.getDoi(), StandardCharsets.UTF_8);
                            doi = String.format("[DOI: %s]\n[DOI: %s]: %s", publication.getDoi(), publication.getDoi(), url);
                        } else {
                            doi = "[DOI: null]\n[DOI: null]: https://doi.org/null";
                        }
                        if (StringUtils.isNotBlank(publication.getHowToCite())) {
                            return publication.getHowToCite() + "\n" + doi;
                        } else {
                            return doi;
                        }
                    }).collect(Collectors.toList()));
        }
        return p;
    }
}
