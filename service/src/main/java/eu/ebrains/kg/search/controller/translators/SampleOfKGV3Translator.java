package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.openMINDSv3.SampleV3;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Sample;

public class SampleOfKGV3Translator  implements Translator<SampleV3, Sample>{

    public Sample translate(SampleV3 sample, DataStage dataStage, boolean liveMode) {
        Sample s = new Sample();
        return s;
    }
}
