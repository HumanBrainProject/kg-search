/*
 *  Copyright 2018 - 2021 Swiss Federal Institute of Technology Lausanne (EPFL)
 *  Copyright 2021 - 2023 EBRAINS AISBL
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0.
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  This open source software code was developed in part or in whole in the
 *  Human Brain Project, funded from the European Union's Horizon 2020
 *  Framework Programme for Research and Innovation under
 *  Specific Grant Agreements No. 720270, No. 785907, and No. 945539
 *  (Human Brain Project SGA1, SGA2 and SGA3).
 */

package eu.ebrains.kg.projects.ebrains.translators;

import eu.ebrains.kg.common.model.DataStage;
import eu.ebrains.kg.common.model.source.ResultsOfKG;
import eu.ebrains.kg.common.model.target.Value;
import eu.ebrains.kg.common.utils.IdUtils;
import eu.ebrains.kg.common.utils.TranslationException;
import eu.ebrains.kg.common.utils.TranslatorUtils;
import eu.ebrains.kg.projects.ebrains.EBRAINSTranslatorUtils;
import eu.ebrains.kg.projects.ebrains.source.ProjectV3;
import eu.ebrains.kg.projects.ebrains.target.Project;
import eu.ebrains.kg.projects.ebrains.translators.commons.EBRAINSTranslator;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProjectTranslator extends EBRAINSTranslator<ProjectV3, Project, ProjectTranslator.Result> {
    public static class Result extends ResultsOfKG<ProjectV3> {
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

    public Project translate(ProjectV3 project, DataStage dataStage, boolean liveMode, TranslatorUtils translatorUtils) throws TranslationException {
        Project p = new Project();

        p.setCategory(new Value<>("Project"));
        p.setDisclaimer(new Value<>("Please alert us at [curation-support@ebrains.eu](mailto:curation-support@ebrains.eu) for errors or quality concerns regarding the dataset, so we can forward this information to the Data Custodian responsible."));

        String uuid = IdUtils.getUUID(project.getId());
        p.setId(uuid);
        p.setAllIdentifiers(project.getIdentifier());
        p.setIdentifier(IdUtils.getIdentifiersWithPrefix("Project", project.getIdentifier()).stream().distinct().collect(Collectors.toList()));
        p.setDescription(value(project.getDescription()));
        p.setDataset(refExtendedVersion(project.getDatasets(), true));
        p.setModels(refExtendedVersion(project.getModels(), true));
        p.setSoftware(refExtendedVersion(project.getSoftware(), true));
        p.setMetaDataModels(refExtendedVersion(project.getMetaDataModels(), true));
        p.setTitle(value(project.getTitle()));
        if(!CollectionUtils.isEmpty(project.getPublications())) {
            p.setPublications(value(project.getPublications().stream()
                    .map(rp -> EBRAINSTranslatorUtils.getFormattedDigitalIdentifier(translatorUtils.getDoiCitationFormatter(), rp.getIdentifier(), rp.resolvedType())).filter(Objects::nonNull).collect(Collectors.toList())));
        }
        p.setQueryBuilderText(value(TranslatorUtils.createQueryBuilderText(project.getPrimaryType(), p.getId())));
        return p;
    }
}
