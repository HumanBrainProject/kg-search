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

package eu.ebrains.kg.search.controller.definition;

import eu.ebrains.kg.common.model.TranslatorModel;
import eu.ebrains.kg.common.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.common.model.target.elasticsearch.MetaInfo;
import eu.ebrains.kg.common.model.target.elasticsearch.RibbonInfo;
import eu.ebrains.kg.common.utils.MetaModelUtils;
import eu.ebrains.kg.search.controller.facets.FacetsController;
import eu.ebrains.kg.search.model.Facet;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DefinitionController {

    private static final String SEARCH_UI_NAMESPACE = "https://schema.hbp.eu/searchUi/";
    private static final String GRAPHQUERY_NAMESPACE = "https://schema.hbp.eu/graphQuery/";

    private final MetaModelUtils utils;
    private final FacetsController facetsController;

    public DefinitionController(MetaModelUtils utils, FacetsController facetsController) {
        this.utils = utils;
        this.facetsController = facetsController;
    }

    //@Cacheable(value = "typeMappings", unless = "#result == null")
    public Map<String, Object> generateTypeMappings() {
        Map<String, Object> labels = new LinkedHashMap<>();
        for (TranslatorModel<?, ?, ?, ?> model : TranslatorModel.MODELS) {
            Class<?> targetModel = model.getTargetClass();
            labels.put(MetaModelUtils.getNameForClass(targetModel), generateTypeMappings(targetModel));
            //Also add inner models to the labels
            Arrays.stream(targetModel.getDeclaredClasses()).filter(c -> c.getAnnotation(MetaInfo.class) != null)
                    .forEachOrdered(innerClass -> {
                        labels.put(String.format("%s.%s", MetaModelUtils.getNameForClass(targetModel), MetaModelUtils.getNameForClass(innerClass)), generateTypeMappings(innerClass));
                    });
        }
        return labels;
    }

    public Map<String, Object> generateTypeMappings(Class<?> clazz) {
        Map<String, Object> result = new LinkedHashMap<>();
        String type = MetaModelUtils.getNameForClass(clazz);
        result.put("http://schema.org/name", type);
        RibbonInfo ribbonInfo = clazz.getAnnotation(RibbonInfo.class);
        if (ribbonInfo != null) {
            Map<String, Object> ribbonInfo_result = new HashMap<>();
            ribbonInfo_result.put(SEARCH_UI_NAMESPACE + "content", ribbonInfo.content());
            Map<String, Object> framed_result = new HashMap<>();
            framed_result.put(SEARCH_UI_NAMESPACE + "aggregation", ribbonInfo.aggregation());
            framed_result.put(SEARCH_UI_NAMESPACE + "dataField", ribbonInfo.dataField());
            Map<String, Object> suffix_result = new HashMap<>();
            suffix_result.put(SEARCH_UI_NAMESPACE + "singular", ribbonInfo.singular());
            suffix_result.put(SEARCH_UI_NAMESPACE + "plural", ribbonInfo.plural());
            framed_result.put(SEARCH_UI_NAMESPACE + "suffix", suffix_result);
            ribbonInfo_result.put(SEARCH_UI_NAMESPACE + "framed", framed_result);
            ribbonInfo_result.put(SEARCH_UI_NAMESPACE + "icon", ribbonInfo.icon());
            result.put(SEARCH_UI_NAMESPACE + "ribbon", ribbonInfo_result);
        }
        List<MetaModelUtils.FieldWithGenericTypeInfo> allFields = utils.getAllFields(clazz);
        Map<String, Object> fields = new LinkedHashMap<>();
        result.put("fields", fields);
        allFields.forEach(f -> {
            try {
                handleField(f, fields);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
        return result;
    }

    private Map<String, Object> handleChildren(Type type) {
        Map<String, Object> properties = new LinkedHashMap<>();
        List<MetaModelUtils.FieldWithGenericTypeInfo> allFields = utils.getAllFields(type);
        allFields.forEach(field -> {
            try {
                handleField(field, properties);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
        return properties;
    }

    private void handleField(MetaModelUtils.FieldWithGenericTypeInfo f, Map<String, Object> fields) throws ClassNotFoundException {
        FieldInfo info = f.getField().getAnnotation(FieldInfo.class);
        Type topTypeToHandle = f.getGenericType() != null ? f.getGenericType() : MetaModelUtils.getTopTypeToHandle(f.getField().getGenericType());

        if (info != null) {
            String propertyName = utils.getPropertyName(f.getField());
            Map<String, Object> propertyDefinition = new LinkedHashMap<>();
            fields.put(propertyName, propertyDefinition);
            FieldInfo defaultFieldInfo = utils.defaultFieldInfo();
            if (!info.label().equals(defaultFieldInfo.label())) {
                propertyDefinition.put(GRAPHQUERY_NAMESPACE + "label", info.label());
            }
            if (!info.hint().equals(defaultFieldInfo.hint())) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "hint", info.hint());
            }
            if (info.groupBy() != defaultFieldInfo.groupBy()) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "groupby", info.groupBy()); // TODO: change to camelCase (groupBy)
            }
            if (info.visible() != defaultFieldInfo.visible()) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "visible", info.visible());
            }
            if (info.labelHidden() != defaultFieldInfo.labelHidden()) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "labelHidden", info.labelHidden());
            }
            if (info.markdown() != defaultFieldInfo.markdown()) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "markdown", info.markdown());
            }
            if (info.overview() != defaultFieldInfo.overview()) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "overview", info.overview());
            }
            if (info.overviewMaxDisplay() != defaultFieldInfo.overviewMaxDisplay()) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "overviewMaxDisplay", info.overviewMaxDisplay());
            }
            if (info.type() != defaultFieldInfo.type()) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "type", info.type().name().toLowerCase());
            }
            if (!info.separator().equals(defaultFieldInfo.separator())) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "separator", info.separator());
            }
            if (!Objects.equals(info.layout(), defaultFieldInfo.layout())) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "layout", info.layout());
            }
            if (!info.linkIcon().equals(defaultFieldInfo.linkIcon())) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "linkIcon", info.linkIcon());
            }
            if (!info.tagIcon().equals(defaultFieldInfo.tagIcon())) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "tagIcon", info.tagIcon());
            }
            if (!info.icon().equals(defaultFieldInfo.icon())) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "icon", info.icon());
            }
            if (info.order() != defaultFieldInfo.order()) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "order", info.order());
            }
            if (info.aggregate() != defaultFieldInfo.aggregate()) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "aggregate", info.aggregate().name().toLowerCase());
            }
            if (info.isFilePreview() != defaultFieldInfo.isFilePreview()) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "isFilePreview", info.isFilePreview());
            }
            if (info.isHierarchicalFiles() != defaultFieldInfo.isHierarchicalFiles()) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "isHierarchicalFiles", info.isHierarchicalFiles());
            }
            if (info.isHierarchical() != defaultFieldInfo.isHierarchical()) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "isHierarchical", info.isHierarchical());
            }
            if (info.isCitation() != defaultFieldInfo.isCitation()) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "isCitation", info.isCitation());
            }
            if (info.termsOfUse() != defaultFieldInfo.termsOfUse()) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "termsOfUse", info.termsOfUse());
            }
            if (info.isTable() != defaultFieldInfo.isTable()) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "isTable", info.isTable());
            }
            if (info.isDirectDownload() != defaultFieldInfo.isDirectDownload()) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "isDirectDownload", info.isDirectDownload());
            }
            if (info.isGroupedLinks() != defaultFieldInfo.isGroupedLinks()) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "isGroupedLinks", info.isGroupedLinks());
            }
            if (info.isAsync() != defaultFieldInfo.isAsync()) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "isAsync", info.isAsync());
            }
            Map<String, Object> children = handleChildren(topTypeToHandle);
            propertyDefinition.putAll(children);
        }
    }

    //@Cacheable(value = "types", unless = "#result == null")
    public List<Object> generateTypes() {
        Map<Integer, Object> types = new LinkedHashMap<>();
        for (TranslatorModel<?, ?, ?, ?> model : TranslatorModel.MODELS) {
            Class<?> targetModel = model.getTargetClass();
            Map<String, Object> type = generateType(targetModel, MetaModelUtils.getNameForClass(targetModel));
            if (type != null) {
                types.put(types.size(), type);
            }
            //Also add inner models to the types
            Arrays.stream(targetModel.getDeclaredClasses()).filter(c -> c.getAnnotation(MetaInfo.class) != null)
                    .forEachOrdered(innerClass -> {
                        Map<String, Object> innerType = generateType(innerClass, String.format("%s.%s", MetaModelUtils.getNameForClass(targetModel), MetaModelUtils.getNameForClass(innerClass)));
                        if (innerType != null) {
                            types.put(types.size()+1, innerType);
                        }
                    });
        }
        ArrayList<Map.Entry<Integer, Object>> list = new ArrayList<>(types.entrySet());
        return list.stream().sorted(new TypeComparator()).map(Map.Entry::getValue).collect(Collectors.toList());
    }

    private static class TypeComparator implements Comparator<Map.Entry<Integer, Object>> {
        @Override
        public int compare(Map.Entry<Integer, Object> a, Map.Entry<Integer, Object> b) {
            return a.getKey().compareTo(b.getKey());
        }
    }

    public Map<String, Object> generateType(Class<?> clazz, String label) {
        MetaInfo metaInfo = clazz.getAnnotation(MetaInfo.class);
        if (metaInfo == null || !metaInfo.searchable()) {
            return null;
        }
        String type = MetaModelUtils.getNameForClass(clazz);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("type", type);
        result.put("label", label);
        result.put("facets", listFacets(type));
        List<MetaModelUtils.FieldWithGenericTypeInfo> allFields = utils.getAllFields(clazz);
        List<Map<String, String>> sortFields = new ArrayList<>();
        result.put("sortFields", sortFields);
        sortFields.add(Map.of(
                "label", "Relevance",
                "value", "newestFirst"
        ));
        allFields.forEach(f -> {
            try {
                handleSortField(f, sortFields);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
        if (metaInfo.defaultSelection()) {
            result.put("defaultSelection", true);
        }
        return result;
    }

    private List<Object> listFacets(String type) {
        List<Facet> facets = facetsController.getFacets(type);
        List<Object> result = new ArrayList<>();
        facets.forEach(f -> {
            Map<String, Object> facet = new LinkedHashMap<>();
            result.add(facet);
            facet.put("name", f.getName());
            if (StringUtils.isNotBlank(f.getLabel())) {
                facet.put("label", f.getLabel());
            }
            if (StringUtils.isNotBlank(f.getType().name())) {
                facet.put("type", f.getType().name().toLowerCase());
            }
            if (f.getIsFilterable() != null) {
                facet.put("isFilterable", f.getIsFilterable());
            }
            if (f.getIsHierarchical() != null) {
                facet.put("isHierarchical", f.getIsHierarchical());
            }
        });
        return result;
    }

    private void handleSortField(MetaModelUtils.FieldWithGenericTypeInfo f, List<Map<String, String>> sortFields) throws ClassNotFoundException {
        FieldInfo info = f.getField().getAnnotation(FieldInfo.class);
        if (info != null && info.sort()) {
            sortFields.add(Map.of(
                    "label", info.label(),
                    "value", utils.getPropertyName(f.getField())
            ));
        }
    }
}
