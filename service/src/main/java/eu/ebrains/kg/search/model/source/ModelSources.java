package eu.ebrains.kg.search.model.source;

import eu.ebrains.kg.search.model.source.openMINDSv2.ModelV2;
import eu.ebrains.kg.search.model.source.openMINDSv3.ModelVersionV3;

public class ModelSources {
    private ModelV2 modelV2;
    private ModelVersionV3 modelVersionV3;

    public ModelV2 getModelV2() {
        return modelV2;
    }

    public void setModelV2(ModelV2 modelV2) {
        this.modelV2 = modelV2;
    }

    public ModelVersionV3 getModelVersionV3() {
        return modelVersionV3;
    }

    public void setModelVersionV3(ModelVersionV3 modelVersionV3) {
        this.modelVersionV3 = modelVersionV3;
    }
}
