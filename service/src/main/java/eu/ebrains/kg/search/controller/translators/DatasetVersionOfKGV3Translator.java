package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.openMINDSv3.DatasetVersionV3;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Version;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.DatasetVersion;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.utils.IdUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static eu.ebrains.kg.search.controller.translators.TranslatorCommons.firstItemOrNull;

public class DatasetVersionOfKGV3Translator implements Translator<DatasetVersionV3, DatasetVersion>{

    public DatasetVersion translate(DatasetVersionV3 datasetVersion, DataStage dataStage, boolean liveMode) {
        DatasetVersion d = new DatasetVersion();
        DatasetVersionV3.DatasetVersions dataset = datasetVersion.getDataset();
        d.setVersion(datasetVersion.getVersion());
        d.setId(IdUtils.getUUID(datasetVersion.getId()));
        d.setIdentifier(IdUtils.getUUID(datasetVersion.getIdentifier()));
        if (dataset != null && !CollectionUtils.isEmpty(dataset.getVersions())) {
            List<Version> sortedVersions = Helpers.sort(dataset.getVersions());
            List<TargetInternalReference> references = sortedVersions.stream().map(v -> new TargetInternalReference(IdUtils.getUUID(v.getId()), v.getVersionIdentifier())).collect(Collectors.toList());
            d.setVersions(references);
            d.setSearchable(sortedVersions.get(0).getId().equals(datasetVersion.getId()));
        } else {
            d.setSearchable(true);
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
            d.setDataset(new TargetInternalReference(IdUtils.getUUID(dataset.getId()), dataset.getFullName()));
        }
        if (!CollectionUtils.isEmpty(datasetVersion.getAuthor())) {
            d.setContributors(datasetVersion.getAuthor().stream()
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        } else if (dataset != null && !CollectionUtils.isEmpty(dataset.getAuthor())) {
            d.setContributors(dataset.getAuthor().stream()
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        }
        String citation = datasetVersion.getHowToCite();
        String digitalIdentifier = firstItemOrNull(datasetVersion.getDigitalIdentifier());
        if (digitalIdentifier != null) {
            if (StringUtils.isNotBlank(citation) && StringUtils.isNotBlank(digitalIdentifier)) {
                String url = URLEncoder.encode(digitalIdentifier, StandardCharsets.UTF_8);
                d.setCitation(citation + String.format(" [DOI: %s]\n[DOI: %s]: https://doi.org/%s", digitalIdentifier, digitalIdentifier, url));
            }
            if (StringUtils.isNotBlank(digitalIdentifier)) {
                d.setDoi(digitalIdentifier);
            }
        }
        return d;
    }
}
