package eu.ebrains.kg.common.controller.kg;

import eu.ebrains.kg.common.model.DataStage;

public interface KG {

    <T> T executeQuery(Class<T> clazz, DataStage dataStage, String queryId, int from, int size);

    <T> T executeQueryForInstance(Class<T> clazz, DataStage dataStage, String queryId, String id, boolean asServiceAccount);
}
