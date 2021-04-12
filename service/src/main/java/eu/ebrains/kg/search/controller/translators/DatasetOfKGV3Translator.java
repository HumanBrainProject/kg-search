package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.openMINDSv3.DatasetVersionV3;
import eu.ebrains.kg.search.model.source.openMINDSv3.DigitalIdentifierV3;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.InternalDatasetVersion;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Dataset;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.utils.IdUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static eu.ebrains.kg.search.controller.translators.TranslatorCommons.firstItemOrNull;

public class DatasetOfKGV3Translator implements Translator<DatasetVersionV3, Dataset>{

    public Dataset translate(DatasetVersionV3 datasetVersion, DataStage dataStage, boolean liveMode) {
        Dataset d = new Dataset();
        DatasetVersionV3.Dataset dataset = datasetVersion.getDataset();
        d.setVersion(datasetVersion.getVersionIdentifier());
        d.setId(IdUtils.getUUID(datasetVersion.getId()));
        d.setIdentifier(IdUtils.getUUID(datasetVersion.getIdentifier()));
        if (dataset != null && !CollectionUtils.isEmpty(dataset.getDatasetVersions())) {
            List<InternalDatasetVersion> sortedVersions = Helpers.sort(dataset.getDatasetVersions());
            List<TargetInternalReference> references = sortedVersions.stream().map(v -> new TargetInternalReference(IdUtils.getUUID(v.getId()), v.getVersionIdentifier())).collect(Collectors.toList());
            d.setVersions(references);
        }
        if (!StringUtils.isBlank(datasetVersion.getDescription())) {
            d.setDescription(datasetVersion.getDescription());
        } else if (dataset != null) {
            d.setDescription(dataset.getDescription());
        }
//        if (!StringUtils.isBlank(datasetVersion.getFullName())) {
//            d.setTitle(datasetVersion.getFullName());
//        } else if (dataset != null {
//            d.setTitle(datasetV3.getFullName());
//        }
        // For the UI we don't need the version number in the title as it is set in de dropdown
        if (dataset != null) {
            d.setTitle(dataset.getFullName());
            d.setDatasetVersions(new TargetInternalReference(IdUtils.getUUID(dataset.getId()), dataset.getFullName()));
        }
        if (!CollectionUtils.isEmpty(datasetVersion.getAuthors())) {
            d.setContributors(datasetVersion.getAuthors().stream() // TODO: setAuthors
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        }
        DigitalIdentifierV3 digitalIdentifier = firstItemOrNull(datasetVersion.getDigitalIdentifier());
        if (digitalIdentifier != null) {
            String citation = digitalIdentifier.getHowToCite();
            String doi = digitalIdentifier.getIdentifier();
            if (StringUtils.isNotBlank(citation) && StringUtils.isNotBlank(doi)) {
                String url = URLEncoder.encode(doi, StandardCharsets.UTF_8);
                d.setCitation(citation + String.format(" [DOI: %s]\n[DOI: %s]: https://doi.org/%s", doi, doi, url));
            }
            if (StringUtils.isNotBlank(doi)) {
                d.setDoi(doi);
            }
        }
//            if (!CollectionUtils.isEmpty(dataset.getComponents())) {
//                d.setComponents(dataset.getComponents().stream()
//                        .map(c -> new TargetInternalReference(
//                                IdUtils.getUUID(c.getId()),
//                                c.getFullName()
//                        )).collect(Collectors.toList()));
//            }
        return d;
    }
}
