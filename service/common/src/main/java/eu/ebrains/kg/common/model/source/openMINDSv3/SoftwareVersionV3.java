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

package eu.ebrains.kg.common.model.source.openMINDSv3;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import eu.ebrains.kg.common.model.source.HasMetaBadges;
import eu.ebrains.kg.common.model.source.HasMetrics;
import eu.ebrains.kg.common.model.source.IsCiteable;
import eu.ebrains.kg.common.model.source.openMINDSv3.commons.ListOrSingleStringAsListDeserializer;
import eu.ebrains.kg.common.model.source.openMINDSv3.commons.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class SoftwareVersionV3 extends SourceInstanceV3 implements IsCiteable, HasMetrics, HasMetaBadges {
    private String fullName;
    private String versionIdentifier;
    private List<PersonOrOrganizationRef> developer;
    private String howToCite;
    private String doi;
    private String swhid;
    private List<License> license;
    private Copyright copyright;
    private List<FullNameRefForResearchProductVersion> projects;
    private List<PersonOrOrganizationRef> custodian;
    private String description;
    private String versionInnovation;
    private Date releaseDate;
    private Date firstReleasedAt;
    private Date lastReleasedAt;
    private List<RelatedPublication> publications;
    private List<FullNameRef> applicationCategory;
    private List<FullNameRef> operatingSystem;
    private List<FullNameRef> device;
    private List<FullNameRef> programmingLanguage;

    @JsonDeserialize(using= ListOrSingleStringAsListDeserializer.class)
    private List<String> requirement;
    private List<FullNameRef> feature;
    private List<FullNameRef> language;
    private String homepage;
    private String repository;

    private String documentationDOI;
    private String documentationURL;
    private String documentationFile;
    private String documentationWebResource;

    @JsonDeserialize(using=ListOrSingleStringAsListDeserializer.class)
    private List<String> supportChannel;

    private List<FileFormat> inputFormat;
    private List<FileFormat> outputFormat;

    private List<Component> components;
    private SoftwareVersions software;
    private String version;
    private Integer last30DaysViews;
    private String issueDate;

    private List<LearningResource> learningResource;
    private List<ExternalRef> livePapers;

    @Override
    @JsonIgnore
    public SoftwareVersions getParentOfVersion() {
        return getSoftware();
    }

    @Getter
    @Setter
    public static class SoftwareVersions extends Versions {

        @JsonProperty("softwareCustodian")
        private List<PersonOrOrganizationRef> custodian;

        @JsonProperty("softwareDeveloper")
        private List<PersonOrOrganizationRef> developer;

        @JsonProperty("softwareProjects")
        private List<FullNameRefForResearchProductVersion> projects;

        @JsonProperty("softwareHomepage")
        private String homepage;

    }

    @Getter
    @Setter
    public static class License extends ExternalRef{
        private String shortName;
    }

    @Getter
    @Setter
    public static class FileFormat extends FullNameRef{
        @JsonDeserialize(using = ListOrSingleStringAsListDeserializer.class)
        private List<String> fileExtension;
        private String relatedMediaType;
    }

    @Getter
    @Setter
    public static class Component{
        private String id;
        private String fullName;
        private String versionIdentifier;
        private String fallbackFullName;
    }

    @JsonIgnore
    @Override
    public List<ServiceLink> getAllServiceLinks() {
        return Collections.emptyList();
    }

    @Override
    @JsonIgnore
    public List<String> getAllContentTypes() {
        return Collections.emptyList();
    }

    @Override
    @JsonIgnore
    public boolean hasProtocolExecutions() {
        return false;
    }
}
