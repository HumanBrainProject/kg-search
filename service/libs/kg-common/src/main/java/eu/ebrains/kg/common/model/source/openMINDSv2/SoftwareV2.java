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

package eu.ebrains.kg.common.model.source.openMINDSv2;

import eu.ebrains.kg.common.model.source.SourceInstanceV1andV2;

import java.util.List;

public class SoftwareV2 extends SourceInstanceV1andV2 {
    private String description;
    private List<Version> versions;
    private String title;

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public List<Version> getVersions() { return versions; }

    public void setVersions(List<Version> versions) { this.versions = versions; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public static class Version {
        private List<String> applicationCategory;
        private List<String> documentation;
        private String description;
        private List<String> license;
        private List<String> features;
        private List<String> operatingSystem;
        private List<Contributor> contributors;
        private List<String> homepage;
        private List<String> sourceCode;
        private String version;

        public List<String> getApplicationCategory() { return applicationCategory; }

        public void setApplicationCategory(List<String> applicationCategory) { this.applicationCategory = applicationCategory; }

        public List<String> getDocumentation() { return documentation; }

        public void setDocumentation(List<String> documentation) { this.documentation = documentation; }

        public String getDescription() { return description; }

        public void setDescription(String description) { this.description = description; }

        public List<String> getLicense() { return license; }

        public void setLicense(List<String> license) { this.license = license; }

        public List<String> getFeatures() { return features; }

        public void setFeatures(List<String> features) { this.features = features; }

        public List<String> getOperatingSystem() { return operatingSystem; }

        public void setOperatingSystem(List<String> operatingSystem) { this.operatingSystem = operatingSystem; }

        public List<Contributor> getContributors() { return contributors; }

        public void setContributors(List<Contributor> contributors) { this.contributors = contributors; }

        public List<String> getHomepage() { return homepage; }

        public void setHomepage(List<String> homepage) { this.homepage = homepage; }

        public List<String> getSourceCode() { return sourceCode; }

        public void setSourceCode(List<String> sourceCode) { this.sourceCode = sourceCode; }

        public String getVersion() { return version; }

        public void setVersion(String version) { this.version = version; }
    }

    public static class Contributor {
        private String givenName;
        private String familyName;

        public String getGivenName() { return givenName; }

        public void setGivenName(String givenName) { this.givenName = givenName; }

        public String getFamilyName() { return familyName; }

        public void setFamilyName(String familyName) { this.familyName = familyName; }

    }
}
