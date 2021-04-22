package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.openMINDSv3.SoftwareVersionV3;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Software;

public class SoftwareOfKGV3Translator implements Translator<SoftwareVersionV3, Software>{

    public Software translate(SoftwareVersionV3 softwareVersion, DataStage dataStage, boolean liveMode) {
        Software s = new Software();
        return s;
    }
}
