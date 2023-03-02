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

    public WebServiceVersion translate(WebServiceVersionV3 webServiceVersion, DataStage dataStage, boolean liveMode, TranslatorUtils translatorUtils) throws TranslationException {
        WebServiceVersion w = new WebServiceVersion();

        w.setCategory(new Value<>("Web service"));
        w.setDisclaimer(new Value<>("Please alert us at [curation-support@ebrains.eu](mailto:curation-support@ebrains.eu) for errors or quality concerns regarding the web service, so we can forward this information to the custodian responsible."));

        WebServiceVersionV3.WebServiceVersions webservice = webServiceVersion.getWebservice();
        w.setId(IdUtils.getUUID(webServiceVersion.getId()));
        final Date releaseDate = webServiceVersion.getReleaseDate() != null && webServiceVersion.getReleaseDate().before(new Date()) ? webServiceVersion.getReleaseDate() : webServiceVersion.getFirstReleasedAt();
        translatorUtils.defineBadgesAndTrendingState(w, releaseDate, webServiceVersion.getLast30DaysViews());
        w.setFirstRelease(value(releaseDate));
        w.setLastRelease(value(webServiceVersion.getLastReleasedAt()));
        w.setAllIdentifiers(webServiceVersion.getIdentifier());
        List<Version> versions = webservice == null ? null:webservice.getVersions();
        boolean hasMultipleVersions = !CollectionUtils.isEmpty(versions) && versions.size() > 1;
        if (!CollectionUtils.isEmpty(versions) && versions.size()>1) {
            w.setVersion(webServiceVersion.getVersion());
            List<Version> sortedVersions = Helpers.sort(versions, translatorUtils.getErrors());
            List<TargetInternalReference> references = sortedVersions.stream().map(v -> new TargetInternalReference(IdUtils.getUUID(v.getId()), v.getVersionIdentifier())).collect(Collectors.toList());
            references.add(new TargetInternalReference(IdUtils.getUUID(webservice.getId()), "version overview"));
            w.setVersions(references);
            w.setSearchable(sortedVersions.get(sortedVersions.size()-1).getId().equals(webServiceVersion.getId()));
        } else {
            w.setSearchable(true);
        }

        // title
        if(StringUtils.isNotBlank(webServiceVersion.getFullName())){
            if (hasMultipleVersions || StringUtils.isBlank(webServiceVersion.getVersion())) {
                w.setTitle(value(webServiceVersion.getFullName()));
            } else {
                w.setTitle(value(String.format("%s (%s)", webServiceVersion.getFullName(), webServiceVersion.getVersion())));
            }
        }
        else if(webservice!=null && StringUtils.isNotBlank(webservice.getFullName())){
            if (hasMultipleVersions || StringUtils.isBlank(webServiceVersion.getVersion())) {
                w.setTitle(value(webservice.getFullName()));
            } else {
                w.setTitle(value(String.format("%s (%s)", webservice.getFullName(), webServiceVersion.getVersion())));
            }
        }

        // developers
        if (!CollectionUtils.isEmpty(webServiceVersion.getDeveloper())) {
            w.setDevelopers(webServiceVersion.getDeveloper().stream()
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        } else if (webservice != null && !CollectionUtils.isEmpty(webservice.getDeveloper())) {
            w.setDevelopers(webservice.getDeveloper().stream()
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        }
        String howToCite = webServiceVersion.getHowToCite();
        if(howToCite != null){
            w.setCitation(value(howToCite));
        }
        if(webServiceVersion.getCopyright()!=null){
            final String copyrightHolders = webServiceVersion.getCopyright().getHolder().stream().map(h -> Helpers.getFullName(h.getFullName(), h.getFamilyName(), h.getGivenName())).filter(Objects::nonNull).collect(Collectors.joining(", "));
            w.setCopyright(new Value<>(String.format("%s %s", webServiceVersion.getCopyright().getYear(), copyrightHolders)));
        }

        List<TargetInternalReference> projects = new ArrayList<>();
        if(!CollectionUtils.isEmpty(webServiceVersion.getProjects())){
            projects.addAll(webServiceVersion.getProjects().stream().map(p -> new TargetInternalReference(IdUtils.getUUID(p.getId()), p.getFullName())).collect(Collectors.toList()));
        }
        if(webservice!=null && !CollectionUtils.isEmpty(webservice.getProjects())){
            projects.addAll(webservice.getProjects().stream().map(p -> new TargetInternalReference(IdUtils.getUUID(p.getId()), p.getFullName())).filter(p-> !projects.contains(p)).collect(Collectors.toList()));
        }
        if(!CollectionUtils.isEmpty(projects)){
            w.setProjects(projects);
        }

        if (!CollectionUtils.isEmpty(webServiceVersion.getCustodian())) {
            w.setCustodians(webServiceVersion.getCustodian().stream()
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        } else if (webservice != null && !CollectionUtils.isEmpty(webservice.getCustodian())) {
            w.setCustodians(webservice.getCustodian().stream()
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        }


        if (StringUtils.isNotBlank(webServiceVersion.getDescription())) {
            w.setDescription(value(webServiceVersion.getDescription()));
        } else if (webservice != null) {
            w.setDescription(value(webservice.getDescription()));
        }

        if(!CollectionUtils.isEmpty(webServiceVersion.getPublications())){
            w.setPublications(webServiceVersion.getPublications().stream().map(p -> Helpers.getFormattedDigitalIdentifier(translatorUtils.getDoiCitationFormatter(), p.getIdentifier(), p.resolvedType())).filter(Objects::nonNull).map(Value::new).collect(Collectors.toList()));
        }

        w.setAccessibility(value(webServiceVersion.getAccessibility()));

        if(webServiceVersion.getHomepage()!=null){
            w.setHomepage(new TargetExternalReference(webServiceVersion.getHomepage(), webServiceVersion.getHomepage()));
        }
        else if(webservice!=null && webservice.getHomepage()!=null){
            w.setHomepage(new TargetExternalReference(webservice.getHomepage(), webservice.getHomepage()));
        }

        if(webServiceVersion.getRepository()!=null){
            w.setSourceCode(new TargetExternalReference(webServiceVersion.getRepository(), webServiceVersion.getRepository()));
        }

        List<TargetExternalReference> documentationElements = new ArrayList<>();
        if(webServiceVersion.getDocumentationDOI()!=null){
            documentationElements.add(new TargetExternalReference(webServiceVersion.getDocumentationDOI(), webServiceVersion.getDocumentationDOI()));
        }
        if(webServiceVersion.getDocumentationURL()!=null){
            documentationElements.add(new TargetExternalReference(webServiceVersion.getDocumentationURL(), webServiceVersion.getDocumentationURL()));
        }
        if(webServiceVersion.getDocumentationFile()!=null){
            //TODO make this a little bit prettier (maybe just show the relative file name or similar)
            documentationElements.add(new TargetExternalReference(webServiceVersion.getDocumentationFile(), webServiceVersion.getDocumentationFile()));
        }
        if(!documentationElements.isEmpty()){
            w.setDocumentation(documentationElements);
        }
        if(!CollectionUtils.isEmpty(webServiceVersion.getSupportChannel())){
            final List<TargetExternalReference> links = webServiceVersion.getSupportChannel().stream().filter(channel -> channel.startsWith("http")).
                    map(url -> new TargetExternalReference(url, url)).collect(Collectors.toList());
            if(links.isEmpty()){
                //Decision from Oct 2th 2021: we only show e-mail addresses if there are no links available
                final List<TargetExternalReference> emailAddresses = webServiceVersion.getSupportChannel().stream().filter(channel -> channel.contains("@")).map(email -> new TargetExternalReference(String.format("mailto:%s", email), email)).collect(Collectors.toList());
                if(!emailAddresses.isEmpty()){
                    w.setSupport(emailAddresses);
                }
            }
            else{
                w.setSupport(links);
            }
        }

        if(!CollectionUtils.isEmpty(webServiceVersion.getInputFormat())){
            w.setInputFormat(webServiceVersion.getInputFormat().stream().map(this::translateFileFormat).sorted(Comparator.comparing(e -> e.getChildren().getName())).collect(Collectors.toList()));
            w.setInputFormatsForFilter(webServiceVersion.getInputFormat().stream().map(f -> new Value<>(f.getFullName())).collect(Collectors.toList()));
        }

        if(!CollectionUtils.isEmpty(webServiceVersion.getOutputFormat())){
            w.setOutputFormats(webServiceVersion.getOutputFormat().stream().map(this::translateFileFormat).sorted(Comparator.comparing(e -> e.getChildren().getName())).collect(Collectors.toList()));
            w.setOutputFormatsForFilter(webServiceVersion.getOutputFormat().stream().map(f -> new Value<>(f.getFullName())).collect(Collectors.toList()));
        }

        if(!CollectionUtils.isEmpty(webServiceVersion.getComponents())){
            w.setComponents(webServiceVersion.getComponents().stream().map(c -> {
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
