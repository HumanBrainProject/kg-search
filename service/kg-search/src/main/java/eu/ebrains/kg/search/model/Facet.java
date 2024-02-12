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

package eu.ebrains.kg.search.model;

import eu.ebrains.kg.common.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.search.utils.FacetsUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class Facet {
    private String name;
    @Setter(AccessLevel.NONE)
    private String parentPath;
    @Setter(AccessLevel.NONE)
    private String path;
    @Setter(AccessLevel.NONE)
    private String property;
    private FieldInfo.Facet type;
    private FieldInfo.FacetOrder order = FieldInfo.FacetOrder.BYCOUNT;
    private String label;
    private Boolean exclusiveSelection = false;
    private Boolean isHierarchical = false;
    private Boolean isFilterable = false;
    private String missingTerm = "Others";

    public Facet(String parentPath, String path, String property) {
        this.name = String.format("%s", FacetsUtils.getPath(path, property));
        this.parentPath = parentPath;
        this.path = path;
        this.property = property;
    }

    public String getField(String path) {
        return String.format("%s.value.keyword", path);
    }

    public boolean isChild() {
        return StringUtils.isNotBlank(this.path);
    }
}

