package eu.ebrains.kg.search.model.source;

import eu.ebrains.kg.search.model.source.openMINDSv1.DatasetV1;
import eu.ebrains.kg.search.model.source.openMINDSv3.DatasetVersionV3;

public class IdSources {
    private String idV1;
    private String idV2;
    private String idV3;

    public String getIdV1() {
        return idV1;
    }

    public void setIdV1(String idV1) {
        this.idV1 = idV1;
    }

    public String getIdV2() {
        return idV2;
    }

    public void setIdV2(String idV2) {
        this.idV2 = idV2;
    }

    public String getIdV3() {
        return idV3;
    }

    public void setIdV3(String idV3) {
        this.idV3 = idV3;
    }
}
