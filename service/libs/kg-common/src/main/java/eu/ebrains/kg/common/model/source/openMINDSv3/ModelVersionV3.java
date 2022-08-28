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

package eu.ebrains.kg.common.model.source.openMINDSv3;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import eu.ebrains.kg.common.model.source.HasMetrics;
import eu.ebrains.kg.common.model.source.IsCiteable;
import eu.ebrains.kg.common.model.source.commonsV1andV2.ListOrSingleStringAsListDeserializer;
import eu.ebrains.kg.common.model.source.openMINDSv3.commons.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class ModelVersionV3 extends SourceInstanceV3 implements IsCiteable, HasMetrics {
    private String fullName;
    private List<PersonOrOrganizationRef> developer;
    private String howToCite;
    private String doi;
    private String swhid;
    private String homepage;
    @JsonDeserialize(using = ListOrSingleStringAsListDeserializer.class)
    private List<String> keyword;
    private String version;
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
    private FullNameRef modelFormat;

    private String fullDocumentationUrl;
    private String fullDocumentationDOI;
    private File fullDocumentationFile;
    private ModelVersions model;
    private List<FullNameRefForResearchProductVersion> usedDatasets;
    private List<FullNameRefForResearchProductVersion> producedDatasets;
    private Integer last30DaysViews;
    private List<DOI> inputDOIs;
    private List<ExtendedFullNameRefForResearchProductVersion> inputResearchProductsFromInputFileBundles;
    private List<ExtendedFullNameRefForResearchProductVersion> inputResearchProductsFromInputFiles;
    private List<ExtendedFullNameRefForResearchProductVersion> outputResearchProductsFromReverseInputDOIs;
    private List<ExtendedFullNameRefForResearchProductVersion> outputResearchProductsFromReverseInputFileBundles;
    private List<ExtendedFullNameRefForResearchProductVersion> outputResearchProductsFromReverseInputFiles;
    private List<String> inputURLs;
    private List<DOI> outputDOIs;
    private List<ExtendedFullNameRefForResearchProductVersion> outputResearchProductsFromOutputFileBundles;
    private List<ExtendedFullNameRefForResearchProductVersion> outputResearchProductsFromOutputFiles;
    private List<ExtendedFullNameRefForResearchProductVersion> inputResearchProductsFromReverseOutputDOIs;
    private List<ExtendedFullNameRefForResearchProductVersion> inputResearchProductsFromReverseOutputFileBundles;
    private List<ExtendedFullNameRefForResearchProductVersion> inputResearchProductsFromReverseOutputFiles;
    private List<String> outputURLs;

    @Getter
    @Setter
    public static class ModelVersions extends Versions {

        @JsonProperty("modelCustodian")
        private List<PersonOrOrganizationRef> custodian;

        @JsonProperty("modelDeveloper")
        private List<PersonOrOrganizationRef> developer;

        @JsonProperty("modelProjects")
        private List<FullNameRef> projects;

        private List<StudyTarget> studyTarget;

        private FullNameRef abstractionLevel;

        @JsonProperty("modelScope")
        private FullNameRef scope;

    }

}
