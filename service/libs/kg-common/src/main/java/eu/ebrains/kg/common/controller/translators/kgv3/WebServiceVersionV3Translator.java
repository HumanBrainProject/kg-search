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
import eu.ebrains.kg.common.model.source.ResultsOfKGv3;
import eu.ebrains.kg.common.model.source.openMINDSv3.WebServiceVersionV3;
import eu.ebrains.kg.common.model.source.openMINDSv3.commons.Version;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.WebServiceVersion;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.Children;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.TargetExternalReference;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.Value;
import eu.ebrains.kg.common.utils.IdUtils;
import eu.ebrains.kg.common.utils.TranslationException;
import eu.ebrains.kg.common.utils.TranslatorUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

public class WebServiceVersionV3Translator extends TranslatorV3<WebServiceVersionV3, WebServiceVersion, WebServiceVersionV3Translator.Result> {

    private Children<WebServiceVersion.FileFormat> translateFileFormat(WebServiceVersionV3.FileFormat i) {
        WebServiceVersion.FileFormat f = new WebServiceVersion.FileFormat();
        f.setName(ref(i));
        f.setRelatedMediaType(i.getRelatedMediaType() != null ? new Value<>(i.getRelatedMediaType()) : null);
        f.setFileExtensions(!CollectionUtils.isEmpty(i.getFileExtension()) ? i.getFileExtension().stream().map(Value::new).collect(Collectors.toList()) : null);
        return new Children<>(f);
    }

    public static class Result extends ResultsOfKGv3<WebServiceVersionV3> {
    }

    @Override
    public Class<WebServiceVersionV3> getSourceType() {
        return WebServiceVersionV3.class;
    }

    @Override
    public Class<WebServiceVersion> getTargetType() {
        return WebServiceVersion.class;
    }

    @Override
    public Class<Result> getResultType() {
        return Result.class;
    }

    @Override
    public List<String> getQueryIds() {
        return Collections.singletonList("7728c2dc-5782-4852-a24a-a15623b78d32");
    }

    @Override
    public List<String> semanticTypes() {
        return Collections.singletonList("https://openminds.ebrains.eu/core/WebServiceVersion");
    }

    public WebServiceVersion translate(WebServiceVersionV3 source, DataStage dataStage, boolean liveMode, TranslatorUtils translatorUtils) throws TranslationException {
        WebServiceVersion w = new WebServiceVersion();

        w.setCategory(new Value<>("Web service"));
        w.setDisclaimer(new Value<>("Please alert us at [curation-support@ebrains.eu](mailto:curation-support@ebrains.eu) for errors or quality concerns regarding the web service, so we can forward this information to the custodian responsible."));

        WebServiceVersionV3.WebServiceVersions parent = source.getWebservice();
        w.setIdentifier(IdUtils.getUUID(source.getIdentifier()).stream().distinct().collect(Collectors.toList()));
        w.setId(IdUtils.getUUID(source.getId()));
        final Date releaseDate = source.getReleaseDate() != null && source.getReleaseDate().before(new Date()) ? source.getReleaseDate() : source.getFirstReleasedAt();
        final String releaseDateForSorting = translatorUtils.getReleasedDateForSorting(null, releaseDate);
        translatorUtils.defineBadgesAndTrendingState(w, null, releaseDate, source.getLast30DaysViews());
        w.setFirstRelease(value(releaseDate));
        w.setLastRelease(value(source.getLastReleasedAt()));
        w.setReleasedDateForSorting(value(releaseDateForSorting));
        w.setAllIdentifiers(source.getIdentifier());
        List<Version> versions = parent == null ? null:parent.getVersions();
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
        if(StringUtils.isNotBlank(source.getFullName())){
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
            w.setDevelopers(source.getDeveloper().stream()
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        } else if (parent != null && !CollectionUtils.isEmpty(parent.getDeveloper())) {
            w.setDevelopers(parent.getDeveloper().stream()
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

        if(source.getRepository()!=null){
            w.setSourceCode(new TargetExternalReference(source.getRepository(), source.getRepository()));
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

        if(!CollectionUtils.isEmpty(source.getInputFormat())){
            w.setInputFormat(source.getInputFormat().stream().map(this::translateFileFormat).sorted(Comparator.comparing(e -> e.getChildren().getName())).collect(Collectors.toList()));
            w.setInputFormatsForFilter(source.getInputFormat().stream().map(f -> new Value<>(f.getFullName())).collect(Collectors.toList()));
        }

        if(!CollectionUtils.isEmpty(source.getOutputFormat())){
            w.setOutputFormats(source.getOutputFormat().stream().map(this::translateFileFormat).sorted(Comparator.comparing(e -> e.getChildren().getName())).collect(Collectors.toList()));
            w.setOutputFormatsForFilter(source.getOutputFormat().stream().map(f -> new Value<>(f.getFullName())).collect(Collectors.toList()));
        }

        if(!CollectionUtils.isEmpty(source.getComponents())){
            w.setComponents(source.getComponents().stream().map(c -> {
                String name = getName(c);
                return new TargetInternalReference(IdUtils.getUUID(c.getId()), String.format("%s %s", name, c.getVersionIdentifier()));
            }).collect(Collectors.toList()));
        }

        return w;
    }

    private String getName(WebServiceVersionV3.Component c) {
        if(StringUtils.isNotBlank(c.getFullName())) {
            return c.getFullName();
        }
        if(StringUtils.isNotBlank(c.getFallbackFullName())) {
            return c.getFallbackFullName();
        }
        return null;
    }
}
