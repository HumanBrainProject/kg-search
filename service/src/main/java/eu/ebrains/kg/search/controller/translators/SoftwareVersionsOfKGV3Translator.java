package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.openMINDSv3.SoftwareV3;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.SoftwareVersions;

public class SoftwareVersionsOfKGV3Translator implements Translator<SoftwareV3, SoftwareVersions>{

    public SoftwareVersions translate(SoftwareV3 software, DataStage dataStage, boolean liveMode) {
        SoftwareVersions s = new SoftwareVersions();


        return s;
    }
}
