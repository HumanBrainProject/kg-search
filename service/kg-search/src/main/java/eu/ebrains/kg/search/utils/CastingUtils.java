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

package eu.ebrains.kg.search.utils;

import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CastingUtils {
    public static String getStringField(Map<String, Object> source, String fieldName) {
        if (source.containsKey(fieldName)) {
            try {
                return (String) source.get(fieldName);
            } catch (ClassCastException ignored) {
            }
        }
        return null;
    }

    public static boolean getBooleanField(Map<String, Object> source, String fieldName, boolean defaultValue) {
        if (source.containsKey(fieldName)) {
            try {
                return (boolean) source.get(fieldName);
            } catch (ClassCastException ignored) {
            }
        }
        return defaultValue;
    }

    public static String getObjectFieldStringProperty(Map<String, Object> source, String fieldName, String propertyName) {
        if (source.containsKey(fieldName)) {
            try {
                Map<String, Object> field = (Map<String, Object>) source.get(fieldName);
                if (!CollectionUtils.isEmpty(field)) {
                    return getStringField(field, propertyName);
                }
            } catch (ClassCastException ignored) {
            }
        }
        return null;
    }

    public static boolean getObjectFieldBooleanProperty(Map<String, Object> source, String fieldName, String propertyName) {
        return getObjectFieldBooleanProperty(source, fieldName, propertyName, false);
    }

    public static boolean getObjectFieldBooleanProperty(Map<String, Object> source, String fieldName, String propertyName, boolean defaultValue) {
        if (source.containsKey(fieldName)) {
            try {
                Map<String, Object> field = (Map<String, Object>) source.get(fieldName);
                if (!CollectionUtils.isEmpty(field)) {
                    return getBooleanField(field, propertyName, defaultValue);
                }
            } catch (ClassCastException ignored) {
            }
        }
        return defaultValue;
    }

    public static Object getValueField(Map<String, Object> source, String fieldName) {
        if (source.containsKey(fieldName)) {
            try {
                Map<String, Object> field = (Map<String, Object>) source.get(fieldName);
                if (!CollectionUtils.isEmpty(field) && field.containsKey("value")) {
                    return field.get("value");
                }
            } catch (ClassCastException ignored) {
            }
        }
        return null;
    }

    public static String getStringValueField(Map<String, Object> source, String fieldName) {
        Object o = getValueField(source, fieldName);
        if (o != null) {
            try {
                return (String) o;
            } catch (ClassCastException ignored) {
            }
        }
        return null;
    }

    public static List<Object> getListField(Map<String, Object> source, String fieldName) {
        if (source.containsKey(fieldName)) {
            try {
                List<Object> list = (List<Object>) source.get(fieldName);
                if (CollectionUtils.isEmpty(list)) {
                    return Collections.emptyList();
                }
                return list;
            } catch (ClassCastException ignored) {
            }
        }
        return Collections.emptyList();
    }
}
