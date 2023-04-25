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
import eu.ebrains.kg.common.model.source.HasAccessibility;
import eu.ebrains.kg.common.model.source.IsCiteable;
import eu.ebrains.kg.common.model.source.openMINDSv3.commons.ListOrSingleStringAsListDeserializer;
import eu.ebrains.kg.common.model.source.openMINDSv3.commons.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class MetadataModelVersionV3 extends SourceInstanceV3 implements IsCiteable, HasAccessibility {
    private String fullName;
    private String versionIdentifier;
    private List<PersonOrOrganizationRef> developer;
    private String howToCite;
    private String doi;
    private String swhid;
    private String homepage;
    @JsonDeserialize(using = ListOrSingleStringAsListDeserializer.class)
    private List<String> keyword;
    private String versionInnovation;
    private Date releaseDate;
    private Date firstReleasedAt;
    private Date lastReleasedAt;
    private List<RelatedPublication> relatedPublications;
    private List<ExternalRef> license;
    private Copyright copyright;
    private List<FullNameRef> projects;
    private List<PersonOrOrganizationRef> custodian;
    private String description;
    private NameWithIdentifier accessibility;
    private FileRepository fileRepository;
    private String fullDocumentationUrl;
    private File fullDocumentationFile;
    private String fullDocumentationDOI;
    @JsonDeserialize(using = ListOrSingleStringAsListDeserializer.class)
    private List<String> supportChannel;

    private FullNameRef modelType;
    private List<FullNameRef> specificationFormat;
    private List<FullNameRef> serializationFormat;

    private MetaDataModelVersions metaDataModel;
    private List<FullNameRefForResearchProductVersion> usedDatasets;
    private List<FullNameRefForResearchProductVersion> producedDatasets;


    @Getter
    @Setter
    public static class MetaDataModelVersions extends Versions {

        @JsonProperty("metadataModelProjects")
        private List<FullNameRef> projects;

        @JsonProperty("metadataModelCustodian")
        private List<PersonOrOrganizationRef> custodian;

        @JsonProperty("metadataModelDeveloper")
        private List<PersonOrOrganizationRef> developer;

    }

}
