package eu.ebrains.kg.search.controller.labels;

import eu.ebrains.kg.search.controller.Constants;
import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.MetaInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.RibbonInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Children;
import eu.ebrains.kg.search.utils.MetaModelUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

@Component
public class LabelsController {

    private static final String SEARCH_UI_NAMESPACE = "https://schema.hbp.eu/searchUi/";
    private static final String GRAPHQUERY_NAMESPACE = "https://schema.hbp.eu/graphQuery/";

    private final MetaModelUtils utils;

    public LabelsController(MetaModelUtils utils) {
        this.utils = utils;
    }

    public Map<String, Object> generateLabels() {
        Map<String, Object> labels = new HashMap<>();
        for (int i = 0; i < Constants.TARGET_MODELS_ORDER.size(); i++) {
            Class<?> targetModel = Constants.TARGET_MODELS_ORDER.get(i);
            labels.put(utils.getNameForClass(targetModel), generateLabels(targetModel, i + 1));
        }
        return labels;
    }

    public Map<String, Object> generateLabels(Class<?> clazz, int order) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put(SEARCH_UI_NAMESPACE + "order", order);
        MetaInfo metaInfo = clazz.getAnnotation(MetaInfo.class);
        if (metaInfo != null) {
            result.put("http://schema.org/identifier", metaInfo.identifier());
            if (metaInfo.defaultSelection()) {
                result.put(SEARCH_UI_NAMESPACE + "defaultSelection", true);
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
        allFields.forEach(f -> {
            try {
                handleField(f, fields);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
        result.put("http://schema.org/name", utils.getNameForClass(clazz));
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
            if (info.layout() != defaultFieldInfo.layout()) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "layout", info.layout().name().toLowerCase());
            }
            if (info.facetOrder() != defaultFieldInfo.facetOrder()) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "facet_order", info.facetOrder().name().toLowerCase()); // TODO: change to camelCase (facetOrder)
            }
            if (!info.linkIcon().equals(defaultFieldInfo.linkIcon())) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "link_icon", info.linkIcon()); // TODO: change to camelCase (linkIcon)
            }
            if (!info.tagIcon().equals(defaultFieldInfo.tagIcon())) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "tag_icon", info.tagIcon()); // TODO: change to camelCase (tagIcon)
            }
            if (info.boost() != defaultFieldInfo.boost()) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "boost", info.boost());
            }
            if (info.order() != defaultFieldInfo.order()) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "order", info.order());
            }
            if (info.facet() != defaultFieldInfo.facet()) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "facet", info.facet().name().toLowerCase());
            }
            if (info.aggregate() != defaultFieldInfo.aggregate()) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "aggregate", info.aggregate().name().toLowerCase());
            }
            if (info.isButton() != defaultFieldInfo.isButton()) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "isButton", info.isButton());
            }
            if (info.isHierarchicalFiles() != defaultFieldInfo.isHierarchicalFiles()) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "isHierarchicalFiles", info.isHierarchicalFiles());
            }
            if (info.termsOfUse() != defaultFieldInfo.termsOfUse()) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "termsOfUse", info.termsOfUse());
            }
            if (info.isFilterableFacet() != defaultFieldInfo.isFilterableFacet()) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "isFilterableFacet", info.isFilterableFacet());
            }
            if (info.isTable() != defaultFieldInfo.isTable()) {
                propertyDefinition.put(SEARCH_UI_NAMESPACE + "isTable", info.isTable());
            }
            Map<String, Object> children = handleChildren(topTypeToHandle);
            propertyDefinition.putAll(children);
        }
    }

}
