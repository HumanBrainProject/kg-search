package eu.ebrains.kg.search.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ebrains.kg.search.controller.labels.LabelsController;
import eu.ebrains.kg.search.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.MetaInfo;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;

@Component
public class MetaModelUtils {

    @FieldInfo
    private static Object defaultFieldAnnotations;

    public FieldInfo defaultFieldInfo() {
        try {
            return getClass().getDeclaredField("defaultFieldAnnotations").getAnnotation(FieldInfo.class);
        } catch (NoSuchFieldException e) {
            //This doesn't happen (unless there is a change in the name
            throw new RuntimeException(e);
        }
    }


    public List<Field> getAllFields(Class<?> clazz) {
        if (clazz == null) {
            return Collections.emptyList();
        }
        List<Field> result = new ArrayList<>(getAllFields(clazz.getSuperclass()));
        result.addAll(Arrays.asList(clazz.getDeclaredFields()));
        return result;
    }

    public String getNameForClass(Class<?> clazz) {
        MetaInfo metaInfo = clazz.getAnnotation(MetaInfo.class);
        if (metaInfo != null && !metaInfo.name().isBlank()) {
            return metaInfo.name();
        } else {
            return clazz.getSimpleName();
        }
    }

    public String getPropertyName(Field f){
        JsonProperty propertyOverride = f.getAnnotation(JsonProperty.class);
        return propertyOverride == null ? f.getName() : propertyOverride.value();
    }


}
