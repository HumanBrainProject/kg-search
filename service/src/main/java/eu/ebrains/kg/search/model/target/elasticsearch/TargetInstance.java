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

package eu.ebrains.kg.search.model.target.elasticsearch;

import java.util.List;

public interface TargetInstance {
    String getId();

    /**
     *
     * @return all identifiers to identify duplicates across the multiple KG versions.
     */
    List<String> getAllIdentifiers();


    List<String> getIdentifier();

    /**
     * @return true if this instance shall be available for search.
     * This allows to only flag a subset of a type for search-indexing
     * (e.g. only the latest version of a {@link eu.ebrains.kg.search.model.target.elasticsearch.instances.DatasetVersion})
     *
     * Please note, that if any instance of a type is a searchable instance,
     * you also need to specify this in its {@link MetaInfo}
     */
    boolean isSearchableInstance();
}
