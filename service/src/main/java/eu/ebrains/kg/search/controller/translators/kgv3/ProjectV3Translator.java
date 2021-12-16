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

import eu.ebrains.kg.search.controller.translators.Helpers;
import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.ResultsOfKGv3;
import eu.ebrains.kg.search.model.source.openMINDSv3.ProjectV3;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Project;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.services.DOICitationFormatter;
import eu.ebrains.kg.search.utils.IdUtils;
import eu.ebrains.kg.search.utils.TranslationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProjectV3Translator extends TranslatorV3<ProjectV3, Project, ProjectV3Translator.Result> {
    public static class Result extends ResultsOfKGv3<ProjectV3> {
    }

    @Override
    public Class<ProjectV3> getSourceType() {
        return ProjectV3.class;
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
        return Collections.singletonList("cc5324d5-eec8-4925-aa3e-221d44b8e965");
    }

    @Override
    public List<String> semanticTypes() {
        return Collections.singletonList("https://openminds.ebrains.eu/core/Project");
    }

    public Project translate(ProjectV3 project, DataStage dataStage, boolean liveMode, DOICitationFormatter doiCitationFormatter) throws TranslationException {
        Project p = new Project();
        String uuid = IdUtils.getUUID(project.getId());
        p.setId(uuid);
        p.setAllIdentifiers(project.getIdentifier());
        p.setIdentifier(IdUtils.getIdentifiersWithPrefix("Project", project.getIdentifier()));
        p.setDescription(value(project.getDescription()));
        p.setDataset(refExtendedVersion(project.getDatasets()));
        p.setModels(refExtendedVersion(project.getModels()));
        p.setSoftware(refExtendedVersion(project.getSoftware()));
        p.setTitle(value(project.getTitle()));
        if(!CollectionUtils.isEmpty(project.getPublications())) {
            p.setPublications(value(project.getPublications().stream()
                    .map(publication -> {
                        if (StringUtils.isNotBlank(publication)) {
                            final String doiWithoutPrefix = Helpers.stripDOIPrefix(publication);
                            return Helpers.getFormattedDOI(doiCitationFormatter, doiWithoutPrefix);
                        }
                        return null;
                    }).filter(Objects::nonNull).collect(Collectors.toList())));
        }
        return p;
    }
}
