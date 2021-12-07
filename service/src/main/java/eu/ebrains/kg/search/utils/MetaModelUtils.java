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

package eu.ebrains.kg.search.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ebrains.kg.search.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.MetaInfo;
import org.springframework.stereotype.Component;

import java.lang.reflect.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class MetaModelUtils {

    @FieldInfo
    private static Object defaultFieldAnnotations;

    public static class FieldWithGenericTypeInfo {
        private final Field field;
        private final Type genericType;

        public FieldWithGenericTypeInfo(Field field, Type genericType) {
            this.field = field;
            this.genericType = genericType;
        }

        public Field getField() {
            return field;
        }

        public Type getGenericType() {
            return genericType;
        }

    }

    public FieldInfo defaultFieldInfo() {
        try {
            return getClass().getDeclaredField("defaultFieldAnnotations").getAnnotation(FieldInfo.class);
        } catch (NoSuchFieldException e) {
            //This doesn't happen (unless there is a change in the name
            throw new RuntimeException(e);
        }
    }


    public List<FieldWithGenericTypeInfo> getAllFields(Type type) {
        if (type == null) {
            return Collections.emptyList();
        }
        Class<?> rawType = null;

        if (type instanceof ParameterizedType) {
            rawType = ((Class<?>) ((ParameterizedType) type).getRawType());
        } else if (type instanceof Class<?>) {
            rawType = ((Class<?>) type);
        }
        if (rawType != null) {
            //System.out.println(String.format("Getting fields for %s", type.getTypeName()));
            List<FieldWithGenericTypeInfo> result = new ArrayList<>(getAllFields(rawType.getGenericSuperclass()));
            Map<String, Type> genericTypes = genericTypes(type);
            result.addAll(Arrays.stream(rawType.getDeclaredFields()).filter(field -> !Modifier.isStatic(field.getModifiers())).map(field ->
                    {
                        String typeName = field.getGenericType().getTypeName();
                        return new FieldWithGenericTypeInfo(field, genericTypes.get(typeName));
                    }
            ).collect(Collectors.toList()));
            return result;
        } else {
            return Collections.emptyList();
        }
    }

    private Map<String, Type> genericTypes(Type type) {
        if (type instanceof ParameterizedType) {
            Class<?> rawType = ((Class<?>) ((ParameterizedType) type).getRawType());
            Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
            TypeVariable<? extends Class<?>>[] typeParameters = rawType.getTypeParameters();
            Map<String, Type> result = new HashMap<>();
            for (int i = 0; i < typeParameters.length; i++) {
                result.put(typeParameters[i].getName(), actualTypeArguments[i]);
            }
            return result;
        }
        return Collections.emptyMap();
    }

    public static String getIndexNameForClass(Class<?> clazz){
        return getNameForClass(clazz).toLowerCase().replaceAll(" ", "_");
    }


    public static String getNameForClass(Class<?> clazz) {
        MetaInfo metaInfo = clazz.getAnnotation(MetaInfo.class);
        if (metaInfo != null && !metaInfo.name().isBlank()) {
            return metaInfo.name();
        } else {
            return clazz.getSimpleName();
        }
    }

    public String getPropertyName(Field f) {
        JsonProperty propertyOverride = f.getAnnotation(JsonProperty.class);
        return propertyOverride == null ? f.getName() : propertyOverride.value();
    }


    public static Type getTopTypeToHandle(Type type) throws ClassNotFoundException {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = ((ParameterizedType) type);
            Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
            if (actualTypeArguments.length > 0) {
                Type typeArgument = actualTypeArguments[0];
                if (Collection.class.isAssignableFrom(Class.forName(parameterizedType.getRawType().getTypeName()))) {
                    return getTopTypeToHandle(typeArgument);
                } else {
                    return parameterizedType;
                }
            }
        }
        return type;

    }

    private final static Pattern UUID_REGEX_PATTERN =
            Pattern.compile("^[{]?[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}[}]?$");

    public static boolean isValidUUID(String str) {
        if (str == null) {
            return false;
        }
        return UUID_REGEX_PATTERN.matcher(str).matches();
    }
}
