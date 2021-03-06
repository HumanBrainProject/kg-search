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

import eu.ebrains.kg.search.model.source.openMINDSv3.commons.HasEmbargo;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.PersonOrOrganizationRef;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Versions;

import java.util.List;

public class ModelVersionV3 extends SourceInstanceV3 implements HasEmbargo {
    private String title;
    private String version;
    private String description;
    private List<PersonOrOrganizationRef> developer;
    private List<PersonOrOrganizationRef> custodian;
    private ModelVersions model;
    private List<String> applicationCategory;
    private List<String> operatingSystem;
    private List<String> format;
    private List<String> embargo;
    private List<String> fileBundle;
    private List<License> license;
    private List<String> publications;
    private String howToCite;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public ModelVersions getModel() {
        return model;
    }

    public void setModel(ModelVersions model) {
        this.model = model;
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

    public List<String> getFormat() {
        return format;
    }

    public void setFormat(List<String> format) {
        this.format = format;
    }

    public List<String> getEmbargo() {
        return embargo;
    }

    public void setEmbargo(List<String> embargo) {
        this.embargo = embargo;
    }

    public List<String> getFileBundle() {
        return fileBundle;
    }

    public void setFileBundle(List<String> fileBundle) {
        this.fileBundle = fileBundle;
    }

    public List<License> getLicense() {
        return license;
    }

    public void setLicense(List<License> license) {
        this.license = license;
    }

    public List<String> getPublications() {
        return publications;
    }

    public void setPublications(List<String> publications) {
        this.publications = publications;
    }

    public String getHowToCite() {
        return howToCite;
    }

    public void setHowToCite(String howToCite) {
        this.howToCite = howToCite;
    }

    public static class ModelVersions extends Versions {
        private List<String> studyTarget;
        private List<String> modelScope;
        private List<String> abstractionLevel;

        public List<String> getStudyTarget() {
            return studyTarget;
        }

        public void setStudyTarget(List<String> studyTarget) {
            this.studyTarget = studyTarget;
        }

        public List<String> getModelScope() {
            return modelScope;
        }

        public void setModelScope(List<String> modelScope) {
            this.modelScope = modelScope;
        }

        public List<String> getAbstractionLevel() {
            return abstractionLevel;
        }

        public void setAbstractionLevel(List<String> abstractionLevel) {
            this.abstractionLevel = abstractionLevel;
        }
    }

    public static class License {
        private String fullName;
        private String webpage;

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getWebpage() {
            return webpage;
        }

        public void setWebpage(String webpage) {
            this.webpage = webpage;
        }
    }

}
