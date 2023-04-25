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

package eu.ebrains.kg.common.utils;

import eu.ebrains.kg.common.model.DataStage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access= AccessLevel.PRIVATE)
public class ESHelper {

    private final static String INDEX_PREFIX_IN_PROGRESS = "in_progress";
    private final static String INDEX_PREFIX_PUBLICLY_RELEASED = "publicly_released";

    private final static String INDEX_SUFFIX_IDENTIFIERS = "identifiers";

    private static String getIndexPrefix(DataStage dataStage) {
        return dataStage == DataStage.IN_PROGRESS ? INDEX_PREFIX_IN_PROGRESS : INDEX_PREFIX_PUBLICLY_RELEASED;
    }

    public static String getSearchableIndex(DataStage dataStage, Class<?> type, boolean temporary) {
        return String.format("%s%s_searchable_%s", temporary ? "temporary_" : "", getIndexPrefix(dataStage), MetaModelUtils.getIndexNameForClass(type));
    }

    public static String getIdentifierIndex(DataStage dataStage) {
        return String.format("%s_%s", getIndexPrefix(dataStage), INDEX_SUFFIX_IDENTIFIERS);
    }

    public static String getIndexesForDocument(DataStage dataStage) {
        return String.format("%s_*", getIndexPrefix(dataStage));
    }

    public static String getIndexesForSearch(DataStage dataStage) {
        return String.format("%s_searchable_*", getIndexPrefix(dataStage));
    }

    public static String getAutoReleasedIndex(DataStage dataStage, Class<?> type, boolean temporary) {
        return String.format("%s%s_%s", temporary ? "temporary_" : "", getIndexPrefix(dataStage), MetaModelUtils.getIndexNameForClass(type));
    }

    public static String getResourcesIndex(){
        return "resources";
    }
}
