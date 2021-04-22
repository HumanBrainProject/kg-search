package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.openMINDSv3.ModelV3;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.ModelVersions;

public class ModelVersionsOfKGV3Translator implements Translator<ModelV3, ModelVersions>{

    public ModelVersions translate(ModelV3 model, DataStage dataStage, boolean liveMode) {
        ModelVersions m = new ModelVersions();


        return m;
    }
}
