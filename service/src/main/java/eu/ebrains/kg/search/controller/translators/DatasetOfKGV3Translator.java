package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.openMINDSv3.DatasetV3;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Version;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Dataset;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.utils.IdUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class DatasetOfKGV3Translator implements Translator<DatasetV3, Dataset> {

    public Dataset translate(DatasetV3 dataset, DataStage dataStage, boolean liveMode) {
        if (!CollectionUtils.isEmpty(dataset.getVersions()) && dataset.getVersions().size() > 1) {
            Dataset d = new Dataset();
            List<Version> sortedVersions = Helpers.sort(dataset.getVersions());
            List<TargetInternalReference> references = sortedVersions.stream().map(v -> new TargetInternalReference(IdUtils.getUUID(v.getId()), v.getVersionIdentifier())).collect(Collectors.toList());
            d.setDatasets(references);
            d.setId(IdUtils.getUUID(dataset.getId()));
            d.setIdentifier(IdUtils.getUUID(dataset.getIdentifier()));
            d.setDescription(dataset.getDescription());
            if (StringUtils.isNotBlank(dataset.getFullName())) {
                d.setTitle(dataset.getFullName());
            }
            if (!CollectionUtils.isEmpty(dataset.getAuthor())) {
                d.setAuthors(dataset.getAuthor().stream()
                        .map(a -> new TargetInternalReference(
                                IdUtils.getUUID(a.getId()),
                                Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                        )).collect(Collectors.toList()));
            }
            String citation = dataset.getHowToCite();
            String doi = dataset.getDoi();
            if (StringUtils.isNotBlank(doi)) {
                if (StringUtils.isNotBlank(citation)) {
                    d.setDoi(doi);
                    String url = URLEncoder.encode(doi, StandardCharsets.UTF_8);
                    d.setCitation(citation + String.format(" [DOI: %s]\n[DOI: %s]: https://doi.org/%s", doi, doi, url));
                }
            }
            return d;
        }
        return null;
    }
}
