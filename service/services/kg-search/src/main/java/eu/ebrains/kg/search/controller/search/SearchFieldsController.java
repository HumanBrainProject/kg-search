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

package eu.ebrains.kg.search.controller.search;

import eu.ebrains.kg.common.model.TranslatorModel;
import eu.ebrains.kg.common.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.Value;
import eu.ebrains.kg.common.utils.MetaModelUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@SuppressWarnings("java:S1452") // we keep the generics intentionally
public class SearchFieldsController {
    private final Set<String> FIELDS_TO_HIGHLIGHT = Stream.of(
            "title.value",
            "description.value",
            "contributors.value",
            "custodians.value",
            "owners.value",
            "component.value",
            "created_at.value",
            "releasedate.value",
            "activities.value"
    ).collect(Collectors.toSet());

    private final MetaModelUtils utils;

    public SearchFieldsController(
        MetaModelUtils utils
) {
        this.utils = utils;
    }


    @Cacheable(value = "highlight", key = "#type")
    public List<String> getHighlight(String type) {
        List<String> highlights = new ArrayList<>();
        if (StringUtils.isNotBlank(type)) {
            TranslatorModel.MODELS.stream().filter(m -> MetaModelUtils.getNameForClass(m.getTargetClass()).equals(type)).forEach(m -> {
                Class<?> targetModel = m.getTargetClass();
                addFieldsHighlight(highlights, targetModel);
            });
        }
        return highlights;
    }

    private void addFieldsHighlight(List<String> highlights, Class<?> clazz) {
        List<MetaModelUtils.FieldWithGenericTypeInfo> allFields = utils.getAllFields(clazz);
        allFields.forEach(f -> {
            try {
                addFieldHighlight(highlights, f, "");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void addFieldHighlight(List<String> highlights, MetaModelUtils.FieldWithGenericTypeInfo f, String parentPath) throws ClassNotFoundException {
        FieldInfo info = f.getField().getAnnotation(FieldInfo.class);
        if (info != null && !info.ignoreForSearch()) {
            String propertyName = utils.getPropertyName(f.getField());
            String path = String.format("%s%s", parentPath, propertyName);
            if (!propertyName.equals("children")) { // if (f.getField().getType() != Children.class) { if (f.getField().getDeclaringClass() != Children.class) {
                if (StringUtils.isBlank(parentPath) || f.getField().getType() == Value.class) {
                    String valuePath = String.format("%s.value", path);
                    if (FIELDS_TO_HIGHLIGHT.contains(valuePath)) {
                        highlights.add(valuePath);
                    }
                } else {
                    if (FIELDS_TO_HIGHLIGHT.contains(path)) {
                        highlights.add(path);
                    }
                }
            }
//          Type topTypeToHandle = f.getGenericType() != null ? f.getGenericType() : MetaModelUtils.getTopTypeToHandle(f.getField().getGenericType());
//          addChildrenFieldHighlight(highlights, topTypeToHandle, String.format("%s.children", path));
        }
    }


    @Cacheable(value = "queryFields", key = "#type")
    public List<String> getEsQueryFields(String type) {
        Map<String, Double> boosts = new HashMap<>();
        Class<?> targetModelForType = null;
        for (int i = 0; i < TranslatorModel.MODELS.size(); i++) {
            Class<?> targetModel = TranslatorModel.MODELS.get(i).getTargetClass();
            String targetModelName = MetaModelUtils.getNameForClass(targetModel);
            if (StringUtils.isNotBlank(type) && targetModelName.equals(type)) {
                targetModelForType = targetModel;
            } else {
                addFieldsBoost(boosts, targetModel);
            }
        }
        //selected type fields override others
        if (targetModelForType != null) {
            addFieldsBoost(boosts, targetModelForType);
        }
        List<String> fields = boosts.entrySet().stream().map(e -> {
            String field = e.getKey();
            double boost = e.getValue() == null?1.0:(double) e.getValue();
            return String.format("%s^%d", field, (int) boost);
        }).sorted().collect(Collectors.toList());
        return fields;
    }

    private void addFieldsBoost(Map<String, Double> boosts, Class<?> clazz) {
        List<MetaModelUtils.FieldWithGenericTypeInfo> allFields = utils.getAllFields(clazz);
        allFields.forEach(f -> {
            try {
                addFieldBoost(boosts, f, "");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void addFieldBoost(Map<String, Double> boosts, MetaModelUtils.FieldWithGenericTypeInfo f, String parentPath) throws ClassNotFoundException {
        FieldInfo info = f.getField().getAnnotation(FieldInfo.class);
        if (info != null && !info.ignoreForSearch()) {
            String propertyName = utils.getPropertyName(f.getField());
            String path = String.format("%s%s", parentPath, propertyName);
            if (!propertyName.equals("children")) { // if (f.getField().getType() != Children.class) { if (f.getField().getDeclaringClass() != Children.class) {
                if (StringUtils.isBlank(parentPath) || f.getField().getType() == Value.class) {
                    String valuePath = String.format("%s.value", path);
                    boosts.put(valuePath, info.boost());
                } else {
                    boosts.put(path, info.boost());
                }
            }

            Type topTypeToHandle = f.getGenericType() != null ? f.getGenericType() : MetaModelUtils.getTopTypeToHandle(f.getField().getGenericType());
            addChildrenFieldBoost(boosts, topTypeToHandle, String.format("%s.", path));
        }
    }

    private void addChildrenFieldBoost(Map<String, Double> boosts, Type type, String parentPath) {
        List<MetaModelUtils.FieldWithGenericTypeInfo> allFields = utils.getAllFields(type);
        allFields.forEach(field -> {
            try {
                addFieldBoost(boosts, field, parentPath);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
