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

package eu.ebrains.kg.common.model.source.openMINDSv3.commons;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RelatedPublication {
    private String identifier;
    private List<String> type;

    public PublicationType resolvedType() {
        if (type == null || type.isEmpty()) {
            return PublicationType.UNDEFINED;
        }
        switch (type.get(0)) {
            case "https://openminds.ebrains.eu/core/DOI":
                return PublicationType.DOI;
            case "https://openminds.ebrains.eu/core/HANDLE":
                return PublicationType.HANDLE;
            case "https://openminds.ebrains.eu/core/ISBN":
                return PublicationType.ISBN;
            default: return PublicationType.UNDEFINED;
        }
    }
    public enum PublicationType {
        UNDEFINED, DOI, HANDLE, ISBN
    }
}
