package eu.ebrains.kg.search.controller.kg;

import eu.ebrains.kg.search.model.DataStage;

public interface KG {

    <T> T executeQuery(Class<T> clazz, DataStage dataStage, String queryId, int from, int size);

    <T> T executeQuery(Class<T> clazz, DataStage dataStage, String queryId, String id);
}
