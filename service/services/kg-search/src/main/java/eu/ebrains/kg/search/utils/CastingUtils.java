package eu.ebrains.kg.search.utils;

import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CastingUtils {
    public static String getStringField(Map<String, Object> source, String fieldName) {
        if (source.containsKey(fieldName)) {
            try {
                String value = (String) source.get(fieldName);
                return value;
            } catch (ClassCastException ignored) {
            }
        }
        return null;
    }

    public static boolean getBooleanField(Map<String, Object> source, String fieldName) {
        return getBooleanField(source, fieldName, false);
    }

    public static boolean getBooleanField(Map<String, Object> source, String fieldName, boolean defaultValue) {
        if (source.containsKey(fieldName)) {
            try {
                boolean value = (boolean) source.get(fieldName);
                return value;
            } catch (ClassCastException ignored) {
            }
        }
        return defaultValue;
    }

    public static String getObjectFieldStringProperty(Map<String, Object> source, String fieldName, String propertyName) {
        if (source.containsKey(fieldName)) {
            try {
                Map<String, Object> field = (Map<String, Object>) source.get(fieldName);
                if (!CollectionUtils.isEmpty(field)) {
                    return getStringField(field, propertyName);
                }
            } catch (ClassCastException ignored) {
            }
        }
        return null;
    }

    public static boolean getObjectFieldBooleanProperty(Map<String, Object> source, String fieldName, String propertyName) {
        return getObjectFieldBooleanProperty(source, fieldName, propertyName, false);
    }

    public static boolean getObjectFieldBooleanProperty(Map<String, Object> source, String fieldName, String propertyName, boolean defaultValue) {
        if (source.containsKey(fieldName)) {
            try {
                Map<String, Object> field = (Map<String, Object>) source.get(fieldName);
                if (!CollectionUtils.isEmpty(field)) {
                    return getBooleanField(field, propertyName, defaultValue);
                }
            } catch (ClassCastException ignored) {
            }
        }
        return defaultValue;
    }

    public static String getValueField(Map<String, Object> source, String fieldName) {
        if (source.containsKey(fieldName)) {
            try {
                Map<String, String> field = (Map<String, String>) source.get(fieldName);
                if (!CollectionUtils.isEmpty(field) && field.containsKey("value")) {
                    return field.get("value");
                }
            } catch (ClassCastException ignored) {
            }
        }
        return null;
    }

    public static List<Object> getListField(Map<String, Object> source, String fieldName) {
        if (source.containsKey(fieldName)) {
            try {
                List<Object> list = (List<Object>) source.get(fieldName);
                if (CollectionUtils.isEmpty(list)) {
                    return Collections.emptyList();
                }
                return list;
            } catch (ClassCastException ignored) {
            }
        }
        return Collections.emptyList();
    }
}
