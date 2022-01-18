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

package eu.ebrains.kg.search.model.source.openMINDSv3;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import eu.ebrains.kg.search.model.source.commonsV1andV2.ListOrSingleStringAsListDeserializer;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.*;

import java.util.List;

public class SoftwareVersionV3 extends SourceInstanceV3 {
    private String fullName;
    private String versionIdentifier;
    private List<PersonOrOrganizationRef> developer;
    private String howToCite;
    private String doi;
    private String swhid;
    private List<License> license;
    private Copyright copyright;
    private List<FullNameRef> projects;
    private List<PersonOrOrganizationRef> custodian;
    private String description;
    private String versionInnovation;
    private List<String> publications;
    private List<FullNameRef> applicationCategory;
    private List<FullNameRef> operatingSystem;
    private List<FullNameRef> device;
    private List<FullNameRef> programmingLanguage;

    @JsonDeserialize(using=ListOrSingleStringAsListDeserializer.class)
    private List<String> requirement;
    private List<FullNameRef> feature;
    private List<FullNameRef> language;
    private String homepage;
    private String repository;

    private String documentationDOI;
    private String documentationURL;
    private String documentationFile;

    @JsonDeserialize(using=ListOrSingleStringAsListDeserializer.class)
    private List<String> supportChannel;

    private List<FileFormat> inputFormat;
    private List<FileFormat> outputFormat;

    private List<Component> components;
    private SoftwareVersions software;
    private String version;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getVersionIdentifier() {
        return versionIdentifier;
    }

    public void setVersionIdentifier(String versionIdentifier) {
        this.versionIdentifier = versionIdentifier;
    }

    public List<PersonOrOrganizationRef> getDeveloper() {
        return developer;
    }

    public void setDeveloper(List<PersonOrOrganizationRef> developer) {
        this.developer = developer;
    }

    public String getHowToCite() {
        return howToCite;
    }

    public void setHowToCite(String howToCite) {
        this.howToCite = howToCite;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public String getSwhid() {
        return swhid;
    }

    public void setSwhid(String swhid) {
        this.swhid = swhid;
    }

    public List<License> getLicense() {
        return license;
    }

    public void setLicense(List<License> license) {
        this.license = license;
    }

    public Copyright getCopyright() {
        return copyright;
    }

    public void setCopyright(Copyright copyright) {
        this.copyright = copyright;
    }

    public List<FullNameRef> getProjects() {
        return projects;
    }

    public void setProjects(List<FullNameRef> projects) {
        this.projects = projects;
    }

    public List<PersonOrOrganizationRef> getCustodian() {
        return custodian;
    }

    public void setCustodian(List<PersonOrOrganizationRef> custodian) {
        this.custodian = custodian;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersionInnovation() {
        return versionInnovation;
    }

    public void setVersionInnovation(String versionInnovation) {
        this.versionInnovation = versionInnovation;
    }

    public List<String> getPublications() {
        return publications;
    }

    public void setPublications(List<String> publications) {
        this.publications = publications;
    }

    public List<FullNameRef> getApplicationCategory() {
        return applicationCategory;
    }

    public void setApplicationCategory(List<FullNameRef> applicationCategory) {
        this.applicationCategory = applicationCategory;
    }

    public List<FullNameRef> getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(List<FullNameRef> operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public List<FullNameRef> getDevice() {
        return device;
    }

    public void setDevice(List<FullNameRef> device) {
        this.device = device;
    }

    public List<FullNameRef> getProgrammingLanguage() {
        return programmingLanguage;
    }

    public void setProgrammingLanguage(List<FullNameRef> programmingLanguage) {
        this.programmingLanguage = programmingLanguage;
    }

    public List<String> getRequirement() {
        return requirement;
    }

    public void setRequirement(List<String> requirement) {
        this.requirement = requirement;
    }

    public List<FullNameRef> getFeature() {
        return feature;
    }

    public void setFeature(List<FullNameRef> feature) {
        this.feature = feature;
    }

    public List<FullNameRef> getLanguage() {
        return language;
    }

    public void setLanguage(List<FullNameRef> language) {
        this.language = language;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getDocumentationDOI() {
        return documentationDOI;
    }

    public void setDocumentationDOI(String documentationDOI) {
        this.documentationDOI = documentationDOI;
    }

    public String getDocumentationURL() {
        return documentationURL;
    }

    public void setDocumentationURL(String documentationURL) {
        this.documentationURL = documentationURL;
    }

    public String getDocumentationFile() {
        return documentationFile;
    }

    public void setDocumentationFile(String documentationFile) {
        this.documentationFile = documentationFile;
    }

    public List<String> getSupportChannel() {
        return supportChannel;
    }

    public void setSupportChannel(List<String> supportChannel) {
        this.supportChannel = supportChannel;
    }

    public List<FileFormat> getInputFormat() {
        return inputFormat;
    }

    public void setInputFormat(List<FileFormat> inputFormat) {
        this.inputFormat = inputFormat;
    }

    public List<FileFormat> getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(List<FileFormat> outputFormat) {
        this.outputFormat = outputFormat;
    }

    public List<Component> getComponents() {
        return components;
    }

    public void setComponents(List<Component> components) {
        this.components = components;
    }

    public SoftwareVersionV3.SoftwareVersions getSoftware() {
        return software;
    }

    public void setSoftware(SoftwareVersionV3.SoftwareVersions software) {
        this.software = software;
    }

    public static class SoftwareVersions extends Versions {

        @JsonProperty("softwareCustodian")
        private List<PersonOrOrganizationRef> custodian;

        @JsonProperty("softwareDeveloper")
        private List<PersonOrOrganizationRef> developer;

        @JsonProperty("softwareProjects")
        private List<FullNameRef> projects;

        public List<FullNameRef> getProjects() {
            return projects;
        }

        public void setProjects(List<FullNameRef> projects) {
            this.projects = projects;
        }

        public List<PersonOrOrganizationRef> getCustodian() {
            return custodian;
        }

        public void setCustodian(List<PersonOrOrganizationRef> custodian) {
            this.custodian = custodian;
        }

        public List<PersonOrOrganizationRef> getDeveloper() {
            return developer;
        }

        public void setDeveloper(List<PersonOrOrganizationRef> developer) {
            this.developer = developer;
        }
    }

    public static class License extends ExternalRef{
        private String shortName;

        public String getShortName() {
            return shortName;
        }

        public void setShortName(String shortName) {
            this.shortName = shortName;
        }
    }

    public static class FileFormat extends FullNameRef{
        private List<String> fileExtension;
        private String relatedMediaType;

        public List<String> getFileExtension() {
            return fileExtension;
        }

        public void setFileExtension(List<String> fileExtension) {
            this.fileExtension = fileExtension;
        }

        public String getRelatedMediaType() {
            return relatedMediaType;
        }

        public void setRelatedMediaType(String relatedMediaType) {
            this.relatedMediaType = relatedMediaType;
        }
    }

    public static class Component{
        private String id;
        private String fullName;
        private String versionIdentifier;
        private String fallbackFullName;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getVersionIdentifier() {
            return versionIdentifier;
        }

        public void setVersionIdentifier(String versionIdentifier) {
            this.versionIdentifier = versionIdentifier;
        }

        public String getFallbackFullName() {
            return fallbackFullName;
        }

        public void setFallbackFullName(String fallbackFullName) {
            this.fallbackFullName = fallbackFullName;
        }
    }

}
