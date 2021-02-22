package eu.ebrains.kg.search.utils;

import eu.ebrains.kg.search.model.DataStage;

public class ESHelper {
    public static String getSearchIndex(String type, DataStage dataStage) {
        String indexPrefix = dataStage == DataStage.IN_PROGRESS ? "in_progress" : "publicly_released";
        return String.format("%s_%s", indexPrefix, type.toLowerCase());
    }

    public static String getIdentifierIndex(DataStage dataStage) {
        return dataStage == DataStage.IN_PROGRESS ? "identifiers_in_progress" : "identifiers_publicly_released";
    }

    public static String getIndexFromGroup(String type, String group) {
        String indexPrefix = group.equals("public") ? "publicly_released" : "in_progress";
        return String.format("%s_%s", indexPrefix, type.toLowerCase());
    }

}
