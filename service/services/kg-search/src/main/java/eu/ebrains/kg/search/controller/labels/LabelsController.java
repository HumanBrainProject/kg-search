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

package eu.ebrains.kg.search.controller.labels;

import eu.ebrains.kg.common.model.TranslatorModel;
import eu.ebrains.kg.common.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.common.model.target.elasticsearch.MetaInfo;
import eu.ebrains.kg.common.model.target.elasticsearch.RibbonInfo;
import eu.ebrains.kg.common.utils.MetaModelUtils;
import eu.ebrains.kg.search.controller.facets.FacetsController;
import eu.ebrains.kg.search.model.Facet;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.*;

@Component
public class LabelsController {

    private static final String SEARCH_UI_NAMESPACE = "https://schema.hbp.eu/searchUi/";
    private static final String GRAPHQUERY_NAMESPACE = "https://schema.hbp.eu/graphQuery/";

    private final MetaModelUtils utils;
    private final FacetsController facetsController;

    public LabelsController(MetaModelUtils utils, FacetsController facetsController) {
        this.utils = utils;
        this.facetsController = facetsController;
    }

    //@Cacheable(value = "labels", unless = "#result == null")
    public Map<String, Object> generateLabels() {
        Map<String, Object> labels = new LinkedHashMap<>();
        for (TranslatorModel<?, ?, ?, ?> model : TranslatorModel.MODELS) {
            Class<?> targetModel = model.getTargetClass();
            labels.put(MetaModelUtils.getNameForClass(targetModel), generateLabels(targetModel, labels.size()));
            //Also add inner models to the labels
            Arrays.stream(targetModel.getDeclaredClasses()).filter(c -> c.getAnnotation(MetaInfo.class) != null)
                    .forEachOrdered(innerClass -> {
                        labels.put(String.format("%s.%s", MetaModelUtils.getNameForClass(targetModel), MetaModelUtils.getNameForClass(innerClass)), generateLabels(innerClass, labels.size()+1));
                    });
        }
        return labels;
    }

    public Map<String, Object> generateLabels(Class<?> clazz, int order) {
        Map<String, Object> result = new LinkedHashMap<>();
        String type = MetaModelUtils.getNameForClass(clazz);
        result.put("http://schema.org/name", type);
        result.put(SEARCH_UI_NAMESPACE + "order", order);
        MetaInfo metaInfo = clazz.getAnnotation(MetaInfo.class);
        if (metaInfo != null) {
            if (metaInfo.defaultSelection()) {
                result.put(SEARCH_UI_NAMESPACE + "defaultSelection", true);
            }
            if (metaInfo.searchable()) {
                result.put(SEARCH_UI_NAMESPACE + "searchable", true);
            }
        }
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
        result.put("facets", listFacets(type));
        allFields.forEach(f -> {
            try {
                handleField(f, fields);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
        result.put("facets", listFacets(type));
        return result;
    }

    private List<Object> listFacets(String type) {
        List<Facet> facets = facetsController.getFacets(type);
        List<Object> result = new ArrayList<>();
        facets.forEach(f -> {
            Map<String, Object> facet = new LinkedHashMap<>();
            result.add(facet);
            facet.put("id", f.getId());
            facet.put("name", f.getName());
            if (StringUtils.isNotBlank(f.getFieldLabel())) {
                facet.put("fieldLabel", f.getFieldLabel());
            }
            if (StringUtils.isNotBlank(f.getFilterType().name())) {
                facet.put("filterType", f.getFilterType().name().toLowerCase());
            }
            if (StringUtils.isNotBlank(f.getFilterOrder().name())) {
                facet.put("filterOrder", f.getFilterOrder().name().toLowerCase());
            }
            if (f.getIsFilterable() != null) {
                facet.put("isFilterable", f.getIsFilterable());
            }
            if (f.getExclusiveSelection() != null) {
                facet.put("exclusiveSelection", f.getExclusiveSelection());
            }
            if (f.getIsHierarchical() != null) {
                facet.put("isHierarchical", f.getIsHierarchical());
            }
            if (f.isChild()) {
                facet.put("isChild", true);
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
            if (info.sort() != defaultFieldInfo.sort()) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "sort", info.sort());
            }
            if (info.groupBy() != defaultFieldInfo.groupBy()) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "groupby", info.groupBy()); // TODO: change to camelCase (groupBy)
            }
            if (info.visible() != defaultFieldInfo.visible()) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "visible", info.visible());
            }
            if (info.labelHidden() != defaultFieldInfo.labelHidden()) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "label_hidden", info.labelHidden()); // TODO: change to camelCase (labelHidden)
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
            if (info.ignoreForSearch() != defaultFieldInfo.ignoreForSearch()) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "ignoreForSearch", info.ignoreForSearch());
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
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "link_icon", info.linkIcon()); // TODO: change to camelCase (linkIcon)
            }
            if (!info.tagIcon().equals(defaultFieldInfo.tagIcon())) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "tag_icon", info.tagIcon()); // TODO: change to camelCase (tagIcon)
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

}
