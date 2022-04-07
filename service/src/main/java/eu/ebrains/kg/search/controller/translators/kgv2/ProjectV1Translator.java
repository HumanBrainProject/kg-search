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

import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.ResultsOfKGv2;
import eu.ebrains.kg.search.model.source.openMINDSv1.ProjectV1;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Project;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Value;
import eu.ebrains.kg.search.services.DOICitationFormatter;
import eu.ebrains.kg.search.utils.TranslationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectV1Translator extends TranslatorV2<ProjectV1, Project, ProjectV1Translator.Result> {

    public static class Result extends ResultsOfKGv2<ProjectV1> {
    }

    @Override
    public Class<ProjectV1> getSourceType() {
        return ProjectV1.class;
    }

    @Override
    public Class<Project> getTargetType() {
        return Project.class;
    }

    @Override
    public Class<Result> getResultType() {
        return Result.class;
    }

    @Override
    public List<String> getQueryIds() {
        return Collections.singletonList("minds/core/placomponent/v1.0.0");
    }

    public Project translate(ProjectV1 projectSource, DataStage dataStage, boolean liveMode, DOICitationFormatter doiCitationFormatter) throws TranslationException {
        Project p = new Project();

        p.setCategory(new Value<>("Project"));
        p.setDisclaimer(new Value<>("Please alert us at [curation-support@ebrains.eu](mailto:curation-support@ebrains.eu) for errors or quality concerns regarding the dataset, so we can forward this information to the Data Custodian responsible."));

        p.setAllIdentifiers(createList(projectSource.getIdentifier()));
        p.setId(projectSource.getIdentifier());
        p.setIdentifier(createList(projectSource.getIdentifier(), String.format("Project/%s", projectSource.getIdentifier())).stream().distinct().collect(Collectors.toList()));
        p.setFirstRelease(value(projectSource.getFirstReleaseAt()));
        p.setDescription(value(projectSource.getDescription()));
        p.setLastRelease(value(projectSource.getLastReleaseAt()));
        if(!CollectionUtils.isEmpty(projectSource.getDatasets())) {
            p.setDataset(projectSource.getDatasets().stream()
                    .map(dataset ->
                            new TargetInternalReference(
                                    liveMode ? dataset.getRelativeUrl() : String.format("Dataset/%s", dataset.getIdentifier()),
                                    dataset.getName(), null))
                    .collect(Collectors.toList()));
        }
        p.setTitle(value(projectSource.getTitle()));
        if(!CollectionUtils.isEmpty(projectSource.getPublications())) {
            p.setPublications(value(projectSource.getPublications().stream()
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
                    }).collect(Collectors.toList())));
        }
        if (dataStage == DataStage.IN_PROGRESS) {
            p.setEditorId(value(projectSource.getEditorId()));
        }
        return p;
    }
}
