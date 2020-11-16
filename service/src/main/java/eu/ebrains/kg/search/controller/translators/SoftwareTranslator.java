package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.source.openMINDSv2.SoftwareV2;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Software;

public class SoftwareTranslator implements Translator<SoftwareV2, Software> {

    @Override
    public Software translate(SoftwareV2 softwareV2) {
        return null;
    }
}
