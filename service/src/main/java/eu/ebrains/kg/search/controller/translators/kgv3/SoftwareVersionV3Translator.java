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
import eu.ebrains.kg.search.model.source.openMINDSv3.SoftwareVersionV3;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Version;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.SoftwareVersion;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Children;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetExternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Value;
import eu.ebrains.kg.search.services.DOICitationFormatter;
import eu.ebrains.kg.search.utils.IdUtils;
import eu.ebrains.kg.search.utils.TranslationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

public class SoftwareVersionV3Translator extends TranslatorV3<SoftwareVersionV3, SoftwareVersion, SoftwareVersionV3Translator.Result> {

    private Children<SoftwareVersion.FileFormat> translateFileFormat(SoftwareVersionV3.FileFormat i) {
        SoftwareVersion.FileFormat f = new SoftwareVersion.FileFormat();
        f.setName(ref(i));
        f.setRelatedMediaType(i.getRelatedMediaType() != null ? new Value<>(i.getRelatedMediaType()) : null);
        f.setFileExtensions(!CollectionUtils.isEmpty(i.getFileExtension()) ? i.getFileExtension().stream().map(Value::new).collect(Collectors.toList()) : null);
        return new Children<>(f);
    }

    public static class Result extends ResultsOfKGv3<SoftwareVersionV3> {
    }

    @Override
    public Class<SoftwareVersionV3> getSourceType() {
        return SoftwareVersionV3.class;
    }

    @Override
    public Class<SoftwareVersion> getTargetType() {
        return SoftwareVersion.class;
    }

    @Override
    public Class<Result> getResultType() {
        return Result.class;
    }

    @Override
    public List<String> getQueryIds() {
        return Collections.singletonList("e1cd5cd9-f4e1-467b-82de-b5f093c2a0cf");
    }

    @Override
    public List<String> semanticTypes() {
        return Collections.singletonList("https://openminds.ebrains.eu/core/SoftwareVersion");
    }

    public SoftwareVersion translate(SoftwareVersionV3 softwareVersion, DataStage dataStage, boolean liveMode, DOICitationFormatter doiCitationFormatter) throws TranslationException {
        SoftwareVersion s = new SoftwareVersion();
        SoftwareVersionV3.SoftwareVersions software = softwareVersion.getSoftware();

        s.setId(IdUtils.getUUID(softwareVersion.getId()));
        s.setAllIdentifiers(softwareVersion.getIdentifier());
        s.setIdentifier(IdUtils.getIdentifiersWithPrefix("Software", softwareVersion.getIdentifier()));

        List<Version> versions = software == null ? null:software.getVersions();
        if (!CollectionUtils.isEmpty(versions) && versions.size()>1) {
            s.setVersion(softwareVersion.getVersion());
            List<Version> sortedVersions = Helpers.sort(versions);
            List<TargetInternalReference> references = sortedVersions.stream().map(v -> new TargetInternalReference(IdUtils.getUUID(v.getId()), v.getVersionIdentifier())).collect(Collectors.toList());
            references.add(new TargetInternalReference(IdUtils.getUUID(software.getId()), "All versions"));
            s.setVersions(references);
            // if versions cannot be sorted (sortedVersions == versions) we flag it as searchable
            s.setSearchable(sortedVersions == versions || sortedVersions.get(0).getId().equals(softwareVersion.getId()));
        } else {
            s.setSearchable(true);
        }

        // title
        if(StringUtils.isNotBlank(softwareVersion.getFullName())){
            s.setTitle(softwareVersion.getFullName());
        }
        else if(software!=null && StringUtils.isNotBlank(software.getFullName())){
            s.setTitle(software.getFullName());
        }

        // developers
        if (!CollectionUtils.isEmpty(softwareVersion.getDeveloper())) {
            s.setDevelopers(softwareVersion.getDeveloper().stream()
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        } else if (software != null && !CollectionUtils.isEmpty(software.getDeveloper())) {
            s.setDevelopers(software.getDeveloper().stream()
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        }
        String citation = softwareVersion.getHowToCite();
        String doi = softwareVersion.getDoi();
        if (StringUtils.isNotBlank(doi)) {
            final String doiWithoutPrefix = Helpers.stripDOIPrefix(doi);
            //TODO do we want to keep this one? It's actually redundant with what we have in "cite dataset"
            s.setDoi(value(doiWithoutPrefix));
            if (StringUtils.isNotBlank(citation)) {
                s.setCitation(value(String.format("%s [DOI: %s](%s)", citation, doiWithoutPrefix, doi)));
            } else {
                s.setCitation(value(Helpers.getFormattedDOI(doiCitationFormatter, doi)));
            }
        } else if (StringUtils.isNotBlank(citation)) {
            s.setCitation(value(citation));
        }
        if(s.getCitation() == null && StringUtils.isNotBlank(softwareVersion.getSwhid())){
            //TODO resolve SWHID with citation formatter
            s.setCitation(new Value<>(softwareVersion.getSwhid()));
        }


        if(!CollectionUtils.isEmpty(softwareVersion.getLicense())){
            s.setLicense(softwareVersion.getLicense().stream().map(l -> new TargetExternalReference(l.getUrl(), l.getLabel())).collect(Collectors.toList()));
            s.setLicenseForFilter(softwareVersion.getLicense().stream().map(l -> new Value<>(l.getShortName())).collect(Collectors.toList()));
        }

        if(softwareVersion.getCopyright()!=null){
            final String copyrightHolders = softwareVersion.getCopyright().getHolder().stream().map(h -> Helpers.getFullName(h.getFullName(), h.getFamilyName(), h.getGivenName())).filter(Objects::nonNull).collect(Collectors.joining(", "));
            s.setCopyright(new Value<>(String.format("%s %s", softwareVersion.getCopyright().getYear(), copyrightHolders)));
        }

        List<TargetInternalReference> projects = new ArrayList<>();
        if(!CollectionUtils.isEmpty(softwareVersion.getProjects())){
            projects.addAll(softwareVersion.getProjects().stream().map(p -> new TargetInternalReference(IdUtils.getUUID(p.getId()), p.getFullName())).collect(Collectors.toList()));
        }
        if(software!=null && !CollectionUtils.isEmpty(software.getProjects())){
            projects.addAll(software.getProjects().stream().map(p -> new TargetInternalReference(IdUtils.getUUID(p.getId()), p.getFullName())).filter(p-> !projects.contains(p)).collect(Collectors.toList()));
        }
        if(!CollectionUtils.isEmpty(projects)){
            s.setProjects(projects);
        }

        if (!CollectionUtils.isEmpty(softwareVersion.getCustodian())) {
            s.setCustodians(softwareVersion.getCustodian().stream()
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        } else if (software != null && !CollectionUtils.isEmpty(software.getCustodian())) {
            s.setCustodians(software.getCustodian().stream()
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        }


        if (StringUtils.isNotBlank(softwareVersion.getDescription())) {
            s.setDescription(softwareVersion.getDescription());
        } else if (software != null) {
            s.setDescription(software.getDescription());
        }

        if(!CollectionUtils.isEmpty(softwareVersion.getPublications())){
            s.setPublications(softwareVersion.getPublications().stream().map(p -> Helpers.getFormattedDOI(doiCitationFormatter, p)).filter(Objects::nonNull).map(Value::new).collect(Collectors.toList()));
        }

        if(!CollectionUtils.isEmpty(softwareVersion.getApplicationCategory())){
            s.setAppCategory(softwareVersion.getApplicationCategory().stream().map(v -> new TargetInternalReference(IdUtils.getUUID(v.getId()), v.getFullName())).collect(Collectors.toList()));
        }

        if(!CollectionUtils.isEmpty(softwareVersion.getOperatingSystem())){
            s.setOperatingSystem(softwareVersion.getOperatingSystem().stream().map(v -> new TargetInternalReference(IdUtils.getUUID(v.getId()), v.getFullName())).collect(Collectors.toList()));
        }

        if(!CollectionUtils.isEmpty(softwareVersion.getDevice())){
            s.setDevices(softwareVersion.getDevice().stream().map(v -> new TargetInternalReference(IdUtils.getUUID(v.getId()), v.getFullName())).collect(Collectors.toList()));
        }

        if(!CollectionUtils.isEmpty(softwareVersion.getProgrammingLanguage())){
            s.setProgrammingLanguages(softwareVersion.getProgrammingLanguage().stream().map(v -> new TargetInternalReference(IdUtils.getUUID(v.getId()), v.getFullName())).collect(Collectors.toList()));
        }

        if(!CollectionUtils.isEmpty(softwareVersion.getRequirement())){
            s.setRequirements(softwareVersion.getRequirement().stream().map(Value::new).collect(Collectors.toList()));
        }

        if(!CollectionUtils.isEmpty(softwareVersion.getFeature())){
            s.setFeatures(softwareVersion.getFeature().stream().map(v -> new TargetInternalReference(IdUtils.getUUID(v.getId()), v.getFullName())).collect(Collectors.toList()));
        }

        if(!CollectionUtils.isEmpty(softwareVersion.getLanguage())){
            s.setLanguages(softwareVersion.getLanguage().stream().map(v -> new TargetInternalReference(IdUtils.getUUID(v.getId()), v.getFullName())).collect(Collectors.toList()));
        }

        if(softwareVersion.getHomepage()!=null){
            s.setHomepage(new TargetExternalReference(softwareVersion.getHomepage(), softwareVersion.getHomepage()));
        }

        if(softwareVersion.getRepository()!=null){
            s.setSourceCode(new TargetExternalReference(softwareVersion.getRepository(), softwareVersion.getRepository()));
        }

        List<TargetExternalReference> documentationElements = new ArrayList<>();
        if(softwareVersion.getDocumentationDOI()!=null){
            documentationElements.add(new TargetExternalReference(softwareVersion.getDocumentationDOI(), softwareVersion.getDocumentationDOI()));
        }
        if(softwareVersion.getDocumentationURL()!=null){
            documentationElements.add(new TargetExternalReference(softwareVersion.getDocumentationURL(), softwareVersion.getDocumentationURL()));
        }
        if(softwareVersion.getDocumentationFile()!=null){
            //TODO make this a little bit prettier (maybe just show the relative file name or similar)
            documentationElements.add(new TargetExternalReference(softwareVersion.getDocumentationFile(), softwareVersion.getDocumentationFile()));
        }
        if(!documentationElements.isEmpty()){
            s.setDocumentation(documentationElements);
        }
        if(!CollectionUtils.isEmpty(softwareVersion.getSupportChannel())){
            final List<TargetExternalReference> links = softwareVersion.getSupportChannel().stream().filter(channel -> channel.startsWith("http")).
                    map(url -> new TargetExternalReference(url, url)).collect(Collectors.toList());
            if(links.isEmpty()){
                //Decision from Oct 2th 2021: we only show e-mail addresses if there are no links available
                final List<TargetExternalReference> emailAddresses = softwareVersion.getSupportChannel().stream().filter(channel -> channel.contains("@")).map(email -> new TargetExternalReference(String.format("mailto:%s", email), email)).collect(Collectors.toList());
                if(!emailAddresses.isEmpty()){
                    s.setSupport(emailAddresses);
                }
            }
            else{
                s.setSupport(links);
            }
        }

        if(!CollectionUtils.isEmpty(softwareVersion.getInputFormat())){
            s.setInputFormat(softwareVersion.getInputFormat().stream().map(this::translateFileFormat).collect(Collectors.toList()));
            s.setInputFormatsForFilter(softwareVersion.getInputFormat().stream().map(f -> new Value<>(f.getFullName())).collect(Collectors.toList()));
        }

        if(!CollectionUtils.isEmpty(softwareVersion.getOutputFormat())){
            s.setOutputFormats(softwareVersion.getOutputFormat().stream().map(this::translateFileFormat).collect(Collectors.toList()));
            s.setOutputFormatsForFilter(softwareVersion.getOutputFormat().stream().map(f -> new Value<>(f.getFullName())).collect(Collectors.toList()));
        }

        if(!CollectionUtils.isEmpty(softwareVersion.getComponents())){
            s.setComponents(softwareVersion.getComponents().stream().map(c -> {
                String name = StringUtils.isNotBlank(c.getFullName()) ? c.getFullName() : StringUtils.isNotBlank(c.getFallbackFullName()) ? c.getFallbackFullName() : null;
                return new TargetInternalReference(IdUtils.getUUID(c.getId()), String.format("%s %s", name, c.getVersionIdentifier()));
            }).collect(Collectors.toList()));
        }
        return s;
    }
}
