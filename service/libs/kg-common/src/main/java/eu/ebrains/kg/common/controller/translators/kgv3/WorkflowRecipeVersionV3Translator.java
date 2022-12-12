
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

package eu.ebrains.kg.common.controller.translators.kgv3;

import eu.ebrains.kg.common.controller.translators.Helpers;
import eu.ebrains.kg.common.controller.translators.kgv3.commons.Accessibility;
import eu.ebrains.kg.common.controller.translators.kgv3.commons.Constants;
import eu.ebrains.kg.common.model.DataStage;
import eu.ebrains.kg.common.model.elasticsearch.Document;
import eu.ebrains.kg.common.model.source.ResultsOfKGv3;
import eu.ebrains.kg.common.model.source.openMINDSv3.WorkflowRecipeVersionV3;
import eu.ebrains.kg.common.model.source.openMINDSv3.commons.Version;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.WorkflowRecipeVersion;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.Value;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.ValueWithDetails;
import eu.ebrains.kg.common.utils.IdUtils;
import eu.ebrains.kg.common.utils.TranslationException;
import eu.ebrains.kg.common.utils.TranslatorUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WorkflowRecipeVersionV3Translator extends TranslatorV3<WorkflowRecipeVersionV3, WorkflowRecipeVersion, WorkflowRecipeVersionV3Translator.Result> {

    public static class Result extends ResultsOfKGv3<WorkflowRecipeVersionV3> {
    }

    @Override
    public Class<WorkflowRecipeVersionV3> getSourceType() {
        return WorkflowRecipeVersionV3.class;
    }

    @Override
    public Class<WorkflowRecipeVersion> getTargetType() {
        return WorkflowRecipeVersion.class;
    }

    @Override
    public Class<Result> getResultType() {
        return Result.class;
    }

    @Override
    public List<String> getQueryIds() {
        return Collections.singletonList("437e4fd8-0c04-43d7-9a99-a36a2b1548f8");
    }

    @Override
    public List<String> semanticTypes() {
        return Collections.singletonList("https://openminds.ebrains.eu/computation/WorkflowRecipeVersion");
    }


    public WorkflowRecipeVersion translate(WorkflowRecipeVersionV3 workflowRecipeVersionV3, DataStage dataStage, boolean liveMode, TranslatorUtils translatorUtils) throws TranslationException {
        WorkflowRecipeVersion w = new WorkflowRecipeVersion();

        w.setCategory(new Value<>("Workflow"));
        w.setDisclaimer(new Value<>("Please alert us at [curation-support@ebrains.eu](mailto:curation-support@ebrains.eu) for errors or quality concerns regarding the workflow, so we can forward this information to the Workflow Custodian responsible."));

        final WorkflowRecipeVersionV3.WorkflowReceipeVersions workflow = workflowRecipeVersionV3.getWorkflow();
        Accessibility accessibility = Accessibility.fromPayload(workflowRecipeVersionV3);
        final Date releaseDate = workflowRecipeVersionV3.getReleaseDate() != null && workflowRecipeVersionV3.getReleaseDate().before(new Date()) ? workflowRecipeVersionV3.getReleaseDate() : workflowRecipeVersionV3.getFirstReleasedAt();
        w.setId(IdUtils.getUUID(workflowRecipeVersionV3.getId()));
        w.setFirstRelease(value(releaseDate));
        w.setLastRelease(value(workflowRecipeVersionV3.getLastReleasedAt()));
        w.setAllIdentifiers(workflowRecipeVersionV3.getIdentifier());
        w.setIdentifier(IdUtils.getUUID(workflowRecipeVersionV3.getIdentifier()).stream().distinct().collect(Collectors.toList()));
        List<Version> versions = workflow == null ? null : workflow.getVersions();
        boolean hasMultipleVersions = !CollectionUtils.isEmpty(versions) && versions.size() > 1;
        if (hasMultipleVersions) {
            w.setVersion(workflowRecipeVersionV3.getVersionIdentifier());
            List<Version> sortedVersions = Helpers.sort(versions);
            List<TargetInternalReference> references = sortedVersions.stream().map(v -> new TargetInternalReference(IdUtils.getUUID(v.getId()), v.getVersionIdentifier())).collect(Collectors.toList());
            references.add(new TargetInternalReference(IdUtils.getUUID(workflow.getId()), "version overview"));
            w.setVersions(references);
            w.setAllVersionRef(new TargetInternalReference(IdUtils.getUUID(workflow.getId()), "version overview"));
            // if versions cannot be sorted (sortedVersions == versions) we flag it as searchable
            w.setSearchable(sortedVersions == versions || sortedVersions.get(0).getId().equals(workflowRecipeVersionV3.getId()));
        } else {
            w.setSearchable(true);
        }

        if (StringUtils.isNotBlank(workflowRecipeVersionV3.getDescription())) {
            w.setDescription(value(workflowRecipeVersionV3.getDescription()));
        } else if (workflow != null) {
            w.setDescription(value(workflow.getDescription()));
        }

        if (StringUtils.isNotBlank(workflowRecipeVersionV3.getVersionInnovation()) && !Constants.VERSION_INNOVATION_DEFAULTS.contains(StringUtils.trim(workflowRecipeVersionV3.getVersionInnovation()).toLowerCase())) {
            w.setNewInThisVersion(new Value<>(workflowRecipeVersionV3.getVersionInnovation()));
        }
        w.setHomepage(link(workflowRecipeVersionV3.getHomepage()));

        if (StringUtils.isNotBlank(workflowRecipeVersionV3.getFullName())) {
            if (hasMultipleVersions || StringUtils.isBlank(workflowRecipeVersionV3.getVersionIdentifier())) {
                w.setTitle(value(workflowRecipeVersionV3.getFullName()));
            } else {
                w.setTitle(value(String.format("%s (%s)", workflowRecipeVersionV3.getFullName(), workflowRecipeVersionV3.getVersionIdentifier())));
            }
        } else if (workflow != null && StringUtils.isNotBlank(workflow.getFullName())) {
            if (hasMultipleVersions || StringUtils.isBlank(workflowRecipeVersionV3.getVersionIdentifier())) {
                w.setTitle(value(workflow.getFullName()));
            } else {
                w.setTitle(value(String.format("%s (%s)", workflow.getFullName(), workflowRecipeVersionV3.getVersionIdentifier())));
            }
        }

        if (!CollectionUtils.isEmpty(workflowRecipeVersionV3.getDeveloper())) {
            w.setContributors(workflowRecipeVersionV3.getDeveloper().stream()
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        } else if (workflow != null && !CollectionUtils.isEmpty(workflow.getDeveloper())) {
            w.setContributors(workflow.getDeveloper().stream()
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        }
        final Document diagram = translatorUtils.getResource(String.format("%s-mermaid", workflowRecipeVersionV3.getUUID()));
        if(diagram!=null && diagram.getSource()!=null){
            final Object mermaid = diagram.getSource().get("mermaid");
            if(mermaid instanceof String){
                final Object details = diagram.getSource().get("details");
                w.setWorkflow(new ValueWithDetails<>((String)mermaid, details instanceof Map ? (Map<String, Object>)details : null));
            }
        }
        return w;
    }
}
