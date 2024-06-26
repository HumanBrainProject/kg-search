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

package eu.ebrains.kg.projects.ebrains.target.schemaorg;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ebrains.kg.common.model.target.SchemaOrgInstance;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SchemaOrgDataset implements SchemaOrgInstance {

    @JsonProperty("@context")
    private final String context = "https://schema.org/";

    @JsonProperty("@type")
    private final String type = "Dataset";

    private String name;
    private String description;
    private String url;
    private List<String> identifier;
    private List<String> keywords;
    private String license;
    private List<Creator> creator;
    private String version;

    public interface Creator{}


    @Getter
    @Setter
    public static class Organization implements Creator {
        @JsonProperty("@type")
        private final String type = "Organization";
        private String url;
        private String name;
    }


    @Getter
    @Setter
    public static class Person implements Creator {
        @JsonProperty("@type")
        private final String type = "Person";
        private String sameAs;
        private String givenName;
        private String familyName;
        private String name;
    }



}
