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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import eu.ebrains.kg.common.model.source.HasMetrics;
import eu.ebrains.kg.common.model.source.IsCiteable;
import eu.ebrains.kg.common.model.source.openMINDSv3.commons.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class WebServiceVersionV3 extends SourceInstanceV3 implements HasMetrics {
    private String fullName;
    private String versionIdentifier;
    private List<PersonOrOrganizationRef> developer;
    private String howToCite;
    private Copyright copyright;
    private List<FullNameRef> projects;
    private List<PersonOrOrganizationRef> custodian;
    private String description;
    private String versionInnovation;
    private Date releaseDate;
    private Date firstReleasedAt;
    private Date lastReleasedAt;
    private List<RelatedPublication> publications;
    private String homepage;
    private String repository;
    private String documentationDOI;
    private String documentationURL;
    private String documentationFile;
    private String accessibility;

    @JsonDeserialize(using=ListOrSingleStringAsListDeserializer.class)
    private List<String> supportChannel;

    private List<FileFormat> inputFormat;
    private List<FileFormat> outputFormat;

    private List<Component> components;
    private WebServiceVersions webservice;
    private String version;
    private Integer last30DaysViews;

    @Getter
    @Setter
    public static class WebServiceVersions extends Versions {

        @JsonProperty("webserviceCustodian")
        private List<PersonOrOrganizationRef> custodian;

        @JsonProperty("webserviceDeveloper")
        private List<PersonOrOrganizationRef> developer;

        @JsonProperty("webserviceProjects")
        private List<FullNameRef> projects;

        @JsonProperty("webserviceHomepage")
        private String homepage;

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

}
