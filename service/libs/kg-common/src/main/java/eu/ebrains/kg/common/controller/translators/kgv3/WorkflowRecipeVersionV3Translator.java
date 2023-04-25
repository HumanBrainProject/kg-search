
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

package eu.ebrains.kg.common.controller.translators.kgv3;

import eu.ebrains.kg.common.controller.translators.Helpers;
import eu.ebrains.kg.common.controller.translators.kgv3.commons.Constants;
import eu.ebrains.kg.common.model.DataStage;
import eu.ebrains.kg.common.model.elasticsearch.Document;
import eu.ebrains.kg.common.model.source.ResultsOfKGv3;
import eu.ebrains.kg.common.model.source.openMINDSv3.WorkflowRecipeVersionV3;
import eu.ebrains.kg.common.model.source.openMINDSv3.commons.Version;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.WorkflowRecipeVersion;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.TargetExternalReference;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.Value;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.ValueWithDetails;
import eu.ebrains.kg.common.utils.IdUtils;
import eu.ebrains.kg.common.utils.TranslationException;
import eu.ebrains.kg.common.utils.TranslatorUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;
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


    public WorkflowRecipeVersion translate(WorkflowRecipeVersionV3 source, DataStage dataStage, boolean liveMode, TranslatorUtils translatorUtils) throws TranslationException {
        WorkflowRecipeVersion w = new WorkflowRecipeVersion();
        w.setCategory(new Value<>("Workflow"));
        w.setDisclaimer(new Value<>("Please alert us at [curation-support@ebrains.eu](mailto:curation-support@ebrains.eu) for errors or quality concerns regarding the workflow, so we can forward this information to the custodian responsible."));

        final WorkflowRecipeVersionV3.WorkflowReceipeVersions parent = source.getWorkflow();
        w.setId(IdUtils.getUUID(source.getId()));
        final Date releaseDate = source.getReleaseDate() != null && source.getReleaseDate().before(new Date()) ? source.getReleaseDate() : source.getFirstReleasedAt();
        translatorUtils.defineBadgesAndTrendingState(w, releaseDate, source.getLast30DaysViews());
        w.setFirstRelease(value(releaseDate));
        w.setLastRelease(value(source.getLastReleasedAt()));
        w.setAllIdentifiers(source.getIdentifier());
        List<Version> versions = parent == null ? null : parent.getVersions();
        boolean hasMultipleVersions = !CollectionUtils.isEmpty(versions) && versions.size() > 1;
        if (!CollectionUtils.isEmpty(versions) && versions.size()>1) {
            w.setVersion(source.getVersion());
            List<Version> sortedVersions = Helpers.sort(versions, translatorUtils.getErrors());
            List<TargetInternalReference> references = sortedVersions.stream().map(v -> new TargetInternalReference(IdUtils.getUUID(v.getId()), v.getVersionIdentifier())).collect(Collectors.toList());
            references.add(new TargetInternalReference(IdUtils.getUUID(parent.getId()), "version overview"));
            w.setVersions(references);
            w.setSearchable(sortedVersions.get(sortedVersions.size()-1).getId().equals(source.getId()));
        } else {
            w.setSearchable(true);
        }

        // title
        if (StringUtils.isNotBlank(source.getFullName())) {
            if (hasMultipleVersions || StringUtils.isBlank(source.getVersion())) {
                w.setTitle(value(source.getFullName()));
            } else {
                w.setTitle(value(String.format("%s (%s)", source.getFullName(), source.getVersion())));
            }
        }
        else if(parent!=null && StringUtils.isNotBlank(parent.getFullName())){
            if (hasMultipleVersions || StringUtils.isBlank(source.getVersion())) {
                w.setTitle(value(parent.getFullName()));
            } else {
                w.setTitle(value(String.format("%s (%s)", parent.getFullName(), source.getVersion())));
            }
        }

        // developers
        if (!CollectionUtils.isEmpty(source.getDeveloper())) {
            w.setContributors(source.getDeveloper().stream()
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        } else if (parent != null && !CollectionUtils.isEmpty(parent.getDeveloper())) {
            w.setContributors(parent.getDeveloper().stream()
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        }
        String howToCite = source.getHowToCite();
        if(howToCite != null){
            w.setCitation(value(howToCite));
        }
        if(source.getCopyright()!=null){
            final String copyrightHolders = source.getCopyright().getHolder().stream().map(h -> Helpers.getFullName(h.getFullName(), h.getFamilyName(), h.getGivenName())).filter(Objects::nonNull).collect(Collectors.joining(", "));
            w.setCopyright(new Value<>(String.format("%s %s", source.getCopyright().getYear(), copyrightHolders)));
        }

        List<TargetInternalReference> projects = new ArrayList<>();
        if(!CollectionUtils.isEmpty(source.getProjects())){
            projects.addAll(source.getProjects().stream().map(p -> new TargetInternalReference(IdUtils.getUUID(p.getId()), p.getFullName())).collect(Collectors.toList()));
        }
        if(parent!=null && !CollectionUtils.isEmpty(parent.getProjects())){
            projects.addAll(parent.getProjects().stream().map(p -> new TargetInternalReference(IdUtils.getUUID(p.getId()), p.getFullName())).filter(p-> !projects.contains(p)).collect(Collectors.toList()));
        }
        if(!CollectionUtils.isEmpty(projects)){
            w.setProjects(projects);
        }

        if (!CollectionUtils.isEmpty(source.getCustodian())) {
            w.setCustodians(source.getCustodian().stream()
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        } else if (parent != null && !CollectionUtils.isEmpty(parent.getCustodian())) {
            w.setCustodians(parent.getCustodian().stream()
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        }


        if (StringUtils.isNotBlank(source.getDescription())) {
            w.setDescription(value(source.getDescription()));
        } else if (parent != null) {
            w.setDescription(value(parent.getDescription()));
        }

        if (StringUtils.isNotBlank(source.getVersionInnovation()) && !Constants.VERSION_INNOVATION_DEFAULTS.contains(StringUtils.trim(source.getVersionInnovation()).toLowerCase())) {
            w.setNewInThisVersion(new Value<>(source.getVersionInnovation()));
        }

        if(!CollectionUtils.isEmpty(source.getPublications())){
            w.setPublications(source.getPublications().stream().map(p -> Helpers.getFormattedDigitalIdentifier(translatorUtils.getDoiCitationFormatter(), p.getIdentifier(), p.resolvedType())).filter(Objects::nonNull).map(Value::new).collect(Collectors.toList()));
        }

        w.setAccessibility(value(source.getAccessibility()));

        if(source.getHomepage()!=null){
            w.setHomepage(new TargetExternalReference(source.getHomepage(), source.getHomepage()));
        }
        else if(parent!=null && parent.getHomepage()!=null){
            w.setHomepage(new TargetExternalReference(parent.getHomepage(), parent.getHomepage()));
        }

        List<TargetExternalReference> documentationElements = new ArrayList<>();
        if(source.getDocumentationDOI()!=null){
            documentationElements.add(new TargetExternalReference(source.getDocumentationDOI(), source.getDocumentationDOI()));
        }
        if(source.getDocumentationURL()!=null){
            documentationElements.add(new TargetExternalReference(source.getDocumentationURL(), source.getDocumentationURL()));
        }
        if(source.getDocumentationFile()!=null){
            //TODO make this a little bit prettier (maybe just show the relative file name or similar)
            documentationElements.add(new TargetExternalReference(source.getDocumentationFile(), source.getDocumentationFile()));
        }
        if(!documentationElements.isEmpty()){
            w.setDocumentation(documentationElements);
        }
        if(!CollectionUtils.isEmpty(source.getSupportChannel())){
            final List<TargetExternalReference> links = source.getSupportChannel().stream().filter(channel -> channel.startsWith("http")).
                    map(url -> new TargetExternalReference(url, url)).collect(Collectors.toList());
            if(links.isEmpty()){
                //Decision from Oct 2th 2021: we only show e-mail addresses if there are no links available
                final List<TargetExternalReference> emailAddresses = source.getSupportChannel().stream().filter(channel -> channel.contains("@")).map(email -> new TargetExternalReference(String.format("mailto:%s", email), email)).collect(Collectors.toList());
                if(!emailAddresses.isEmpty()){
                    w.setSupport(emailAddresses);
                }
            }
            else{
                w.setSupport(links);
            }
        }

        final Document diagram = translatorUtils.getResource(String.format("%s-mermaid", source.getUUID()));
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
