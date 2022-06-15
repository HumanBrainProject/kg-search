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

package eu.ebrains.kg.common.model.target.elasticsearch;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MetaInfo {
    String name();

    /**
     * @return an ordinal number which decides the sorting order of relative types.
     * This is only required for searchable types which is why it defaults to -1 for all others.
     */
    int order() default -1;

    /**
     * @return true if this is the type that shall be preselected by a UI. This value should be true only for a single
     * type to avoid ambiguity
     */
    boolean defaultSelection() default false;
    /**
     * Defines if a target object is searchable as such
     * (independent of the fact if all instances of this type are actually searchable e.g. for
     * {@link eu.ebrains.kg.search.model.target.elasticsearch.instances.DatasetVersion} only the latest version is searchable,
     * the annotation for the {@link eu.ebrains.kg.search.model.target.elasticsearch.instances.DatasetVersion} type is still searchable=true though)
     *
     * To define which individual instances are actually searchable, please specify {@link TargetInstance#isSearchableInstance()}
     */
    boolean searchable() default false;
}
