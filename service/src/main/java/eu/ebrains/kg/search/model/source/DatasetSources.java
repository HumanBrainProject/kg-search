package eu.ebrains.kg.search.model.source;

import eu.ebrains.kg.search.model.source.openMINDSv1.DatasetV1;
import eu.ebrains.kg.search.model.source.openMINDSv3.DatasetVersionV3;

public class DatasetSources {
    private DatasetV1 datasetV1;
    private DatasetVersionV3 datasetVersionV3;

    public DatasetV1 getDatasetV1() {
        return datasetV1;
    }

    public void setDatasetV1(DatasetV1 datasetV1) {
        this.datasetV1 = datasetV1;
    }

    public DatasetVersionV3 getDatasetVersionV3() {
        return datasetVersionV3;
    }

    public void setDatasetVersionV3(DatasetVersionV3 datasetVersionV3) {
        this.datasetVersionV3 = datasetVersionV3;
    }
}
