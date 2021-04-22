package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.openMINDSv3.ModelVersionV3;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Model;

public class ModelOfKGV3Translator implements Translator<ModelVersionV3, Model>{

    public Model translate(ModelVersionV3 modelVersion, DataStage dataStage, boolean liveMode) {
      Model m = new Model();
      return m;
    }
}
