package eu.ebrains.kg.search.controller.labels;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ebrains.kg.search.controller.Constants;
import eu.ebrains.kg.search.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.MetaInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Contributor;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Software;
import eu.ebrains.kg.search.utils.MetaModelUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
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
            labels.put(utils.getNameForClass(targetModel), generateLabels(targetModel, i+1));
        }
        return labels;
    }

    public Map<String, Object> generateLabels(Class<?> clazz, int order) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put(SEARCH_UI_NAMESPACE + "order", order);
        MetaInfo metaInfo = clazz.getAnnotation(MetaInfo.class);
        if (metaInfo != null) {
            result.put("http://schema.org/identifier", metaInfo.identifier());
        }
        List<Field> allFields = utils.getAllFields(clazz);
        Map<String, Object> fields = new LinkedHashMap<>();
        result.put("fields", fields);
        allFields.stream().forEach(f -> {
            FieldInfo info = f.getAnnotation(FieldInfo.class);
            if (info != null) {
                String propertyName = utils.getPropertyName(f);
                Map<String, Object> propertyDefinition = new HashMap<>();
                fields.put(propertyName, propertyDefinition);
                FieldInfo defaultFieldInfo = utils.defaultFieldInfo();
                if (!info.label().equals(defaultFieldInfo.label())) {
                    propertyDefinition.put(GRAPHQUERY_NAMESPACE + "label", info.label());
                }
                if (!info.hint().equals(defaultFieldInfo.hint())) {
                    propertyDefinition.put(SEARCH_UI_NAMESPACE + "hint", info.hint());
                }
                if (!info.optional() == defaultFieldInfo.optional()) {
                    propertyDefinition.put(SEARCH_UI_NAMESPACE + "optional", info.optional());
                }
                if (!info.sort() == defaultFieldInfo.sort()) {
                    propertyDefinition.put(SEARCH_UI_NAMESPACE + "sort", info.sort());
                }
                if (!info.visible() == defaultFieldInfo.visible()) {
                    propertyDefinition.put(SEARCH_UI_NAMESPACE + "visible", info.visible());
                }
                if (!info.labelHidden() == defaultFieldInfo.labelHidden()) {
                    propertyDefinition.put(SEARCH_UI_NAMESPACE + "labelHidden", info.labelHidden());
                }
                if (!info.markdown() == defaultFieldInfo.markdown()) {
                    propertyDefinition.put(SEARCH_UI_NAMESPACE + "markdown", info.markdown());
                }
                if (!info.overview() == defaultFieldInfo.overview()) {
                    propertyDefinition.put(SEARCH_UI_NAMESPACE + "overview", info.overview());
                }
                if (!info.ignoreForSearch() == defaultFieldInfo.ignoreForSearch()) {
                    propertyDefinition.put(SEARCH_UI_NAMESPACE + "ignoreForSearch", info.ignoreForSearch());
                }
                if (info.type() != defaultFieldInfo.type()) {
                    propertyDefinition.put(SEARCH_UI_NAMESPACE + "type", info.type().name().toLowerCase());
                }
                if (info.layout() != defaultFieldInfo.layout()) {
                    propertyDefinition.put(SEARCH_UI_NAMESPACE + "layout", info.layout().name().toLowerCase());
                }
                if (!info.linkIcon().equals(defaultFieldInfo.linkIcon())) {
                    propertyDefinition.put(SEARCH_UI_NAMESPACE + "linkIcon", info.linkIcon());
                }
                if (!info.tagIcon().equals(defaultFieldInfo.tagIcon())) {
                    propertyDefinition.put(SEARCH_UI_NAMESPACE + "tagIcon", info.tagIcon());
                }
                if (info.boost() != defaultFieldInfo.boost()) {
                    propertyDefinition.put(SEARCH_UI_NAMESPACE + "boost", info.boost());
                }
                if (info.facet() != defaultFieldInfo.facet()) {
                    propertyDefinition.put(SEARCH_UI_NAMESPACE + "facet", info.facet().name().toLowerCase());
                }
                if (info.aggregate() != defaultFieldInfo.aggregate()) {
                    propertyDefinition.put(SEARCH_UI_NAMESPACE + "aggregate", info.aggregate().name().toLowerCase());
                }
            }
        });
        result.put("http://schema.org/name", utils.getNameForClass(clazz));
        return result;

    }


}
