package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.openMINDSv3.DatasetV3;
import eu.ebrains.kg.search.model.source.openMINDSv3.DatasetVersionV3;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Dataset;

import java.util.List;
import java.util.stream.Collectors;

public class VersionedDatasetTranslator implements  VersionedTranslator<DatasetV3, Dataset>{

    public Dataset translate(DatasetV3 datasetV3, DataStage dataStage, boolean liveMode, String versionIdentifier, boolean useDatasetIdentifier) {
        Dataset d = new Dataset();
        DatasetVersionV3 datasetVersion = getDatasetVersion(datasetV3.getDatasetVersions(), versionIdentifier);
        if(datasetVersion != null) {
            if(useDatasetIdentifier) {
                d.setIdentifier(datasetV3.getIdentifier());
            } else {
                d.setIdentifier(datasetVersion.getIdentifiers());
            }
            d.setId(datasetVersion.getId());
            d.setVersions(datasetV3.getDatasetVersions());
            d.setDescription(datasetVersion.getDescription());
            d.setTitle(datasetVersion.getFullName());
        } else {
            d.setId(datasetV3.getId());
            d.setIdentifier(datasetV3.getIdentifier());
            d.setDescription(datasetV3.getDescription());
            d.setTitle(datasetV3.getFullName());
        }
        return d;
    }

    private DatasetVersionV3 getDatasetVersion(List<DatasetVersionV3> datasetVersions, String versionIdentifier) {
        if (datasetVersions != null && versionIdentifier != null) {
            return datasetVersions.stream().filter(d -> d.getVersionIdentifier().equals(versionIdentifier)).collect(Collectors.toList()).stream().findFirst().orElse(null);
        }
        return null;
    }

}
