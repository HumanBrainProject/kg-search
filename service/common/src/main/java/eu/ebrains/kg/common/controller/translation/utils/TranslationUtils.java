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

package eu.ebrains.kg.common.controller.translation.utils;

import eu.ebrains.kg.common.controller.translation.models.Stats;
import eu.ebrains.kg.common.model.source.ResultsOfKG;
import eu.ebrains.kg.common.model.target.TargetInternalReference;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TranslationUtils {
    private final static Logger logger = LoggerFactory.getLogger(TranslationUtils.class);
    private final static Map<Class<?>, Set<Field>> nonStaticTransientFields = new HashMap<>();

    public static <E> Stats getStats(ResultsOfKG<E> result, int from) {
        int pageSize = CollectionUtils.isEmpty(result.getData()) ? 0 : result.getData().size();
        int cumulatedSize = from + pageSize;
        String percentage = (CollectionUtils.isEmpty(result.getData()) || result.getTotal() == null || result.getTotal() == 0) ? "unknown%" : String.format("%d%s", Math.round(100.0 * cumulatedSize / result.getTotal()), "%");
        String info = String.format("%d out of %d, %s", cumulatedSize, result.getTotal(), percentage);
        return new Stats(pageSize, info);
    }

    private synchronized static Set<Field> getNonStaticNonTransientFields(Class<?> clazz) {
        Set<Field> fields = nonStaticTransientFields.get(clazz);
        if (fields == null) {
            fields = new HashSet<>();
            collectAllNonStaticNonTransientFields(clazz, fields);
            nonStaticTransientFields.put(clazz, fields);
        }
        return fields;
    }


    private static void collectAllNonStaticNonTransientFields(Class<?> clazz, Set<Field> collector) {
        if (clazz.getCanonicalName().startsWith("eu.ebrains.kg")) {
            final Field[] declaredFields = clazz.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                if (!Modifier.isTransient(declaredField.getModifiers()) && !Modifier.isStatic(declaredField.getModifiers())) {
                    collector.add(declaredField);
                }
            }
            collectAllNonStaticNonTransientFields(clazz.getSuperclass(), collector);
        }
    }

    public static void collectAllTargetInternalReferences(Object obj, List<TargetInternalReference> collector) {
        if (obj == null) {
            return;
        }
        if (obj instanceof TargetInternalReference) {
            collector.add((TargetInternalReference) obj);
            return;
        }
        getNonStaticNonTransientFields(obj.getClass()).forEach(f -> {
            try {
                f.setAccessible(true);
                Object value = f.get(obj);
                if (value != null) {
                    if (value instanceof Collection) {
                        ((Collection<?>) value).forEach(c -> collectAllTargetInternalReferences(c, collector));
                    } else if (value instanceof Map) {
                        ((Map<?, ?>) value).forEach((k, v) -> collectAllTargetInternalReferences(v, collector));
                    } else {
                        collectAllTargetInternalReferences(value, collector);
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });

    }

}
