package eu.ebrains.kg.search.model.source;

import eu.ebrains.kg.search.model.source.openMINDSv2.SoftwareV2;
import eu.ebrains.kg.search.model.source.openMINDSv3.SoftwareVersionV3;

public class SoftwareSources {
    private SoftwareV2 softwareV2;
    private SoftwareVersionV3 softwareVersionV3;

    public SoftwareV2 getSoftwareV2() {
        return softwareV2;
    }

    public void setSoftwareV2(SoftwareV2 softwareV2) {
        this.softwareV2 = softwareV2;
    }

    public SoftwareVersionV3 getSoftwareVersionV3() {
        return softwareVersionV3;
    }

    public void setSoftwareVersionV3(SoftwareVersionV3 softwareVersionV3) {
        this.softwareVersionV3 = softwareVersionV3;
    }
}
