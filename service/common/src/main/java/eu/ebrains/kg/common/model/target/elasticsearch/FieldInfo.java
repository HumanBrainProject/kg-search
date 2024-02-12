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

package eu.ebrains.kg.common.model.target.elasticsearch;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FieldInfo {

    enum FieldType {
        FILE_PREVIEW, MARKDOWN, HIERARCHICAL, HIERARCHICAL_FILES, HIERARCHICAL_FILES_ASYNC, CITATION, TABLE, MERMAID, GROUPED_LINKS, DEFAULT;
    }

    String label() default "";

    String hint() default "";

    String icon() default "";

    FieldType fieldType() default FieldType.DEFAULT;

    boolean visible() default true;

    boolean labelHidden() default false;

    boolean overview() default false;

    boolean ignoreForSearch() default false;

    boolean termsOfUse() default false;

    boolean isFilterableFacet() default false;

    boolean useForSuggestion() default false;

    Type type() default Type.UNDEFINED;

    String layout() default "";

    String linkIcon() default "";

    String tagIcon() default "";

    String separator() default "";

    double boost() default 1.0;

    Facet facet() default Facet.UNDEFINED;

    FacetOrder facetOrder() default FacetOrder.UNDEFINED;

    Aggregate aggregate() default Aggregate.UNDEFINED;

    int order() default 0;

    int overviewMaxDisplay() default 0;

    boolean isSingleWord() default false;

    enum Facet {
        UNDEFINED, EXISTS, LIST
    }

    enum FacetOrder {
        UNDEFINED, BYCOUNT, BYVALUE
    }

    enum Aggregate {
        UNDEFINED, COUNT
    }

    enum Type {
        UNDEFINED, TEXT, DATE
    }
}
