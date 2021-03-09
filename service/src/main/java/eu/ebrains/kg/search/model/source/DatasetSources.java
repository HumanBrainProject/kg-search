package eu.ebrains.kg.search.model.source;

import eu.ebrains.kg.search.model.source.openMINDSv1.DatasetV1;
import eu.ebrains.kg.search.model.source.openMINDSv3.DatasetV3;

public class DatasetSources {
    private DatasetV1 datasetV1;
    private DatasetV3 datasetV3;

    public DatasetV1 getDatasetV1() {
        return datasetV1;
    }

    public void setDatasetV1(DatasetV1 datasetV1) {
        this.datasetV1 = datasetV1;
    }

    public DatasetV3 getDatasetV3() {
        return datasetV3;
    }

    public void setDatasetV3(DatasetV3 datasetV3) {
        this.datasetV3 = datasetV3;
    }
}
