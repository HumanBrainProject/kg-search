package eu.ebrains.kg.search.controller.mapping;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ebrains.kg.search.controller.Constants;
import eu.ebrains.kg.search.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.MetaInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Contributor;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Software;
import eu.ebrains.kg.search.utils.MetaModelUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

@Component
public class MappingController {

    private static final String SEARCH_UI_NAMESPACE = "https://schema.hbp.eu/searchUi/";
    private static final String GRAPHQUERY_NAMESPACE = "https://schema.hbp.eu/graphQuery/";

    private final MetaModelUtils utils;

    public MappingController(MetaModelUtils utils) {
        this.utils = utils;
    }

    public Map<String, Object> generateMapping() {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> mappings = new HashMap<>();
        result.put("mappings", mappings);
        Constants.TARGET_MODELS_ORDER.forEach(targetModel -> {
            result.put(utils.getNameForClass(targetModel), generateMapping(targetModel));
        });
        return result;
    }

    public Map<String, Object> generateMapping(Class<?> clazz) {
        Map<String, Object> mapping = new LinkedHashMap<>();
        Map<String, Object> all = new HashMap<>();
        all.put("enabled", true);
        mapping.put("_all", all);
        Map<String, Object> properties = new LinkedHashMap<>();
        mapping.put("properties", properties);
        Map<String, Object> timestamp = new LinkedHashMap<>();
        timestamp.put("type", "date");
        properties.put("@timestamp", timestamp);

        List<Field> allFields = utils.getAllFields(clazz);
        allFields.sort(Comparator.comparing(utils::getPropertyName));

        allFields.forEach(field -> {
            Map<String, Object> property = new LinkedHashMap<>();
            properties.put(utils.getPropertyName(field), property);
            Class<?> type = getTypeForField(field);
            List<Field> subFields = utils.getAllFields(type);

            System.out.println(type.getName());
        });
        return mapping;

    }

    private Class<?> getTypeForField(Field f){
        if(f.getGenericType() instanceof ParameterizedType){
            Type[] actualTypeArguments = ((ParameterizedType) f.getGenericType()).getActualTypeArguments();
            if(actualTypeArguments.length==1){
                try {
                    return Class.forName(actualTypeArguments[0].getTypeName());
                } catch (ClassNotFoundException e) {
                    return null;
                }
            }
            else{
                return null;
            }
        }
        else{
            return f.getType();
        }
    }


}
