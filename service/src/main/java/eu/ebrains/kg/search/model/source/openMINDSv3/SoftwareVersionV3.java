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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import eu.ebrains.kg.search.model.source.commonsV1andV2.ListOrSingleStringAsStringDeserializer;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.PersonOrOrganizationRef;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Versions;

import java.util.List;

public class SoftwareVersionV3 extends SourceInstanceV3 {
    private String version;
    @JsonDeserialize(using = ListOrSingleStringAsStringDeserializer.class)
    private String doi;
    private String howToCite;
    private String description;
    private String title;
    private SoftwareVersions software;
    private List<String> applicationCategory;
    private List<String> operatingSystem;
    @JsonDeserialize(using = ListOrSingleStringAsStringDeserializer.class)
    private String homepage;
    private List<PersonOrOrganizationRef> developer;
    private List<PersonOrOrganizationRef> custodian;
    private List<String> sourceCode;
    private List<String> documentation;
    private List<String> features;
    private List<String> license;

    public String getVersion() { return version; }

    public void setVersion(String version) { this.version = version; }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public String getHowToCite() {
        return howToCite;
    }

    public void setHowToCite(String howToCite) {
        this.howToCite = howToCite;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public SoftwareVersions getSoftware() {
        return software;
    }

    public void setSoftware(SoftwareVersions software) {
        this.software = software;
    }

    public List<String> getApplicationCategory() {
        return applicationCategory;
    }

    public void setApplicationCategory(List<String> applicationCategory) {
        this.applicationCategory = applicationCategory;
    }

    public List<String> getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(List<String> operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public List<PersonOrOrganizationRef> getDeveloper() {
        return developer;
    }

    public void setDeveloper(List<PersonOrOrganizationRef> developer) {
        this.developer = developer;
    }

    public List<PersonOrOrganizationRef> getCustodian() {
        return custodian;
    }

    public void setCustodian(List<PersonOrOrganizationRef> custodian) {
        this.custodian = custodian;
    }

    public List<String> getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(List<String> sourceCode) {
        this.sourceCode = sourceCode;
    }

    public List<String> getDocumentation() {
        return documentation;
    }

    public void setDocumentation(List<String> documentation) {
        this.documentation = documentation;
    }

    public List<String> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }

    public List<String> getLicense() {
        return license;
    }

    public void setLicense(List<String> license) {
        this.license = license;
    }

    public static class SoftwareVersions extends Versions {

        private List<PersonOrOrganizationRef> custodian;
        private List<PersonOrOrganizationRef> developer;

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
}
