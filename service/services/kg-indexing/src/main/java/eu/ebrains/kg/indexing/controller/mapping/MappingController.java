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

package eu.ebrains.kg.indexing.controller.mapping;

import eu.ebrains.kg.common.model.target.elasticsearch.ElasticSearchInfo;
import eu.ebrains.kg.common.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.Children;
import eu.ebrains.kg.common.utils.MetaModelUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

@Component
public class MappingController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final MetaModelUtils utils;
    public final static String TEXT_ANALYZER = "custom_text_analyzer";
    private final static String KEYWORD = "keyword";
    private final static String PROPERTIES = "properties";

    public MappingController(MetaModelUtils utils) {
        this.utils = utils;
    }

    public Map<String, Object> generateIdentifierMapping() {
        Map<String, Object> mapping = new LinkedHashMap<>();
        Map<String, Object> properties = new LinkedHashMap<>();
        Map<String, Object> timestamp = new LinkedHashMap<>();
        properties.put("id", Map.of("type", KEYWORD));
        properties.put("identifier", Map.of("type", KEYWORD));
        properties.put("type", Map.of(PROPERTIES, Map.of("value", Map.of("type", KEYWORD))));
        timestamp.put("type", "date");
        properties.put("@timestamp", timestamp);
        mapping.put(PROPERTIES, properties);
        mapping.put("dynamic", false);
        logger.info(String.format("Mapping created: %s", mapping));
        return mapping;
    }

    public Map<String, Object> generateMapping(Class<?> clazz) {
        Map<String, Object> mapping = new LinkedHashMap<>();
        Map<String, Object> properties = new LinkedHashMap<>();
        Map<String, Object> timestamp = new LinkedHashMap<>();
        mapping.put(PROPERTIES, properties);
        mapping.put("dynamic", false);
        timestamp.put("type", "date");
        properties.put("id", Map.of("type", KEYWORD));
        properties.put("type", Map.of("type", KEYWORD));
        properties.put("@timestamp", timestamp);
        properties.putAll(handleType(clazz, null));
        logger.info(String.format("Mapping created: %s", mapping));
        return mapping;
    }

    private Map<String, Object> handleType(Type type, ElasticSearchInfo parentInfo) {
        Map<String, Object> properties = new LinkedHashMap<>();
        List<MetaModelUtils.FieldWithGenericTypeInfo> allFields = utils.getAllFields(type);
        allFields.sort(Comparator.comparing(f -> utils.getPropertyName(f.getField())));
        allFields.forEach(field -> {
            Map<String, Object> fieldDefinition = handleField(field, parentInfo);
            if (!fieldDefinition.isEmpty()) {
                properties.put(utils.getPropertyName(field.getField()), fieldDefinition);
            }
        });
        return properties;
    }

    private Map<String, Object> handleField(MetaModelUtils.FieldWithGenericTypeInfo field, ElasticSearchInfo parentInfo) {
        try {
            ElasticSearchInfo esInfo = field.getField().getAnnotation(ElasticSearchInfo.class);
            FieldInfo fieldInfo = field.getField().getAnnotation(FieldInfo.class);
            boolean isSingleWord = fieldInfo != null && fieldInfo.isSingleWord();
            if(esInfo == null && parentInfo != null) {
                esInfo = parentInfo;
            }
            if (esInfo == null || esInfo.mapping()) {
                Type topTypeToHandle = field.getGenericType() != null ? field.getGenericType() : MetaModelUtils.getTopTypeToHandle(field.getField().getGenericType());
                Map<String, Object> fieldDefinition = new HashMap<>();


                if(topTypeToHandle instanceof ParameterizedType && ((ParameterizedType)topTypeToHandle).getRawType() == Children.class){
                    Map<String, Object> otherType = handleType(topTypeToHandle, esInfo);
                    //TODO check if nested shouldn't be defined one level further up
                    ((Map<String, Object>)otherType.get("children")).put("type", "nested");
                    fieldDefinition.put(PROPERTIES, otherType);

                    //TODO check why we need this "artificial" value mapping
                    Map<String,  Object> value= new LinkedHashMap<>();
                    otherType.put("value", value);
                    Map<String,  Object> fields = new LinkedHashMap<>();
                    value.put("fields", fields);
                    value.put("type", "text");
                    value.put("analyzer", isSingleWord?KEYWORD:TEXT_ANALYZER);
                    Map<String,  Object> keyword= new LinkedHashMap<>();
                    fields.put(KEYWORD, keyword);
                    keyword.put("type",KEYWORD);
                }
                else if (topTypeToHandle == String.class) {
                    if(esInfo != null && StringUtils.isNotBlank(esInfo.type())) {
                        fieldDefinition.put("type", esInfo.type());
                    } else {
                        fieldDefinition.put("type", "text");
                        Map<String, Object> fields = new HashMap<>();
                        fieldDefinition.put("fields", fields);
                        Map<String, Object> keyword = new LinkedHashMap<>();
                        fields.put(KEYWORD, keyword);
                        keyword.put("type", KEYWORD);
                        if (esInfo != null && esInfo.ignoreAbove() > 0) {
                            keyword.put("ignore_above", esInfo.ignoreAbove());
                        }
                        fieldDefinition.put("analyzer", isSingleWord?KEYWORD:TEXT_ANALYZER);
                    }
                } else if (topTypeToHandle == Date.class) {
                    fieldDefinition.put("type", "date");
                } else if (topTypeToHandle == Boolean.class || topTypeToHandle == boolean.class) {
                    fieldDefinition.put("type", "boolean");
                } else if (topTypeToHandle == Integer.class || topTypeToHandle == int.class) {
                    fieldDefinition.put("type", "integer");
                } else {
                    Map<String, Object> otherType = handleType(topTypeToHandle, esInfo);
                    fieldDefinition.put(PROPERTIES, otherType);
                }
                return fieldDefinition;
            } else {
                return Collections.emptyMap();
            }
        } catch (
                ClassNotFoundException e) {
            throw new RuntimeException(String.format("Was not able to map field %s in type %s", field.getField().getName(), field.getField().getDeclaringClass().getSimpleName()), e);
        }

    }

}
