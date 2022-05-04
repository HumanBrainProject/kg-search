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

package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.source.IsCiteable;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.HasCitation;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Value;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("java:S1118") //We inherit this one from other classes.
public class TranslatorCommons {

    public static <T> T firstItemOrNull(List<T> list) {
        return list != null && !list.isEmpty() ? list.get(0) : null;
    }

    public static Value<String> toValue(String value){
        return StringUtils.isBlank(value) ? null : new Value<>(value);
    }

    public static <T extends Comparable<T>> Value<T> toValue(T value){
        return value == null ? null : new Value<>(value);
    }

    public static <T> List<T> emptyToNull(List<T> list) {
        return CollectionUtils.isEmpty(list) ? null : list;
    }

    public static <T extends Comparable<T>> List<Value<T>> emptyToNullValueList(List<T> list) {
        return CollectionUtils.isEmpty(list) ? null : list.stream().map(Value::new).collect(Collectors.toList());
    }


}
