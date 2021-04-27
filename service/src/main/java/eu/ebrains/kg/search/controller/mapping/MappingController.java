package eu.ebrains.kg.search.controller.mapping;

import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Children;
import eu.ebrains.kg.search.utils.MetaModelUtils;
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

    public MappingController(MetaModelUtils utils) {
        this.utils = utils;
    }

    public Map<String, Object> generateIdentifierMapping() {
        Map<String, Object> mapping = new LinkedHashMap<>();
        Map<String, Object> properties = new LinkedHashMap<>();
        Map<String, Object> timestamp = new LinkedHashMap<>();
        properties.put("id", Map.of("type", "keyword"));
        properties.put("identifier", Map.of("type", "keyword"));
        properties.put("type", Map.of("type", "keyword"));
        timestamp.put("type", "date");
        properties.put("@timestamp", timestamp);
        mapping.put("properties", properties);
        mapping.put("dynamic", false);
        logger.info(String.format("Mapping created: %s", mapping));
        return mapping;
    }

    public Map<String, Object> generateMapping(Class<?> clazz) {
        Map<String, Object> mapping = new LinkedHashMap<>();
        Map<String, Object> properties = new LinkedHashMap<>();
        Map<String, Object> timestamp = new LinkedHashMap<>();
        mapping.put("properties", properties);
        mapping.put("dynamic", false);
        timestamp.put("type", "date");
        properties.put("id", Map.of("type", "keyword"));
        properties.put("type", Map.of("type", "keyword"));
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
            if (fieldDefinition != null) {
                properties.put(utils.getPropertyName(field.getField()), fieldDefinition);
            }
        });
        return properties;
    }

    private Map<String, Object> handleField(MetaModelUtils.FieldWithGenericTypeInfo field, ElasticSearchInfo parentInfo) {
        try {
            ElasticSearchInfo esInfo = field.getField().getAnnotation(ElasticSearchInfo.class);
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
                    fieldDefinition.put("properties", otherType);

                    //TODO check why we need this "artificial" value mapping
                    Map<String,  Object> value= new LinkedHashMap<>();
                    otherType.put("value", value);
                    Map<String,  Object> fields = new LinkedHashMap<>();
                    value.put("fields", fields);
                    value.put("type", "text");
                    Map<String,  Object> keyword= new LinkedHashMap<>();
                    fields.put("keyword", keyword);
                    keyword.put("type","keyword");

                }
                else if (topTypeToHandle == String.class) {
                    if(esInfo != null && StringUtils.isNotBlank(esInfo.type())) {
                        fieldDefinition.put("type", esInfo.type());
                    } else {
                        fieldDefinition.put("type", "text");
                        Map<String, Object> fields = new HashMap<>();
                        fieldDefinition.put("fields", fields);
                        Map<String, Object> keyword = new LinkedHashMap<>();
                        fields.put("keyword", keyword);
                        keyword.put("type", "keyword");
                        if (esInfo != null && esInfo.ignoreAbove() > 0) {
                            keyword.put("ignore_above", esInfo.ignoreAbove());
                        }
                    }
                } else if (topTypeToHandle == Date.class) {
                    fieldDefinition.put("type", "date");
                } else if (topTypeToHandle == Boolean.class || topTypeToHandle == boolean.class) {
                    fieldDefinition.put("type", "boolean");
                } else {
                    Map<String, Object> otherType = handleType(topTypeToHandle, esInfo);
                    fieldDefinition.put("properties", otherType);
                }
                return fieldDefinition;
            } else {
                return null;
            }
        } catch (
                ClassNotFoundException e) {
            throw new RuntimeException(String.format("Was not able to map field %s in type %s", field.getField().getName(), field.getField().getDeclaringClass().getSimpleName()), e);
        }

    }

}
