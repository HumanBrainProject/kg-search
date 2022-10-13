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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import eu.ebrains.kg.common.model.source.commonsV1andV2.ListOrSingleStringAsListDeserializer;
import eu.ebrains.kg.common.model.source.openMINDSv3.commons.*;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.TargetInternalReference;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
public class BrainAtlasV3 extends SourceInstanceV3 {

    private String fullName;
    private String description;
    private List<PersonOrOrganizationRef> author;
    private List<PersonOrOrganizationRef> custodian;
    private ParcellationTerminology terminology;
    private List<BrainAtlasVersion> brainAtlasVersion;

    @Getter
    @Setter
    public static class ParcellationTerminology{
        private String id;
        private List<ParcellationEntity> parcellationEntity;
    }

    @Getter
    @Setter
    public static class ParcellationEntity {
        private String id;
        private String name;
        @JsonDeserialize(using = ListOrSingleStringAsListDeserializer.class)
        private List<String> ontologyIdentifier;
        private String definition;
        private List<String> hasParent;
        private List<ParcellationEntityVersion> versions;
    }

    @Getter
    @Setter
    public static class BrainAtlasVersion extends FullNameRef{
        private String shortName;
        private String versionIdentifier;
        private String versionInnovation;
        @JsonDeserialize(using = ListOrSingleStringAsListDeserializer.class)
        private List<String> ontologyIdentifier;
        private String description;
        private String abbreviation;
        private String isNewVersionOf;
        private List<String> isAlternativeVersionOf;
        private List<PersonOrOrganizationRef> author;
        private List<PersonOrOrganizationRef> custodian;
        private FullNameRefWithVersion coordinateSpace;
        private List<FullNameRefForResearchProductVersion> definedIn;
    }

    @Getter
    @Setter
    public static class ParcellationTerminologyVersion {
        private String id;
        private List<ParcellationEntityVersion> parcellationEntityVersion;
        @JsonDeserialize(using = ListOrSingleStringAsListDeserializer.class)
        private List<String> ontologyIdentifier;
    }

    @Getter
    @Setter
    public static class ParcellationEntityVersion {
        private String id;
        private String name;
        private String additionalRemarks;
        private String correctedName;
        @JsonDeserialize(using = ListOrSingleStringAsListDeserializer.class)
        private List<String> ontologyIdentifier;
        private String versionIdentifier;
        private String versionInnovation;
        private List<String> brainAtlasVersion;
    }

}