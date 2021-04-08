package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.openMINDSv3.DatasetV3;
import eu.ebrains.kg.search.model.source.openMINDSv3.DatasetVersionV3;
import eu.ebrains.kg.search.model.source.openMINDSv3.DigitalIdentifierV3;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.InternalDatasetVersion;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.DatasetVersions;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.utils.IdUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static eu.ebrains.kg.search.controller.translators.TranslatorCommons.firstItemOrNull;

public class DatasetVersionsOfKGV3Translator implements Translator<DatasetV3, DatasetVersions>{

    public DatasetVersions translate(DatasetV3 dataset, DataStage dataStage, boolean liveMode) {
        DatasetVersions d = new DatasetVersions();

            d.setId(IdUtils.getUUID(dataset.getId()));
            d.setIdentifier(IdUtils.getUUID(dataset.getIdentifier()));
            d.setDescription(dataset.getDescription());
            d.setTitle(dataset.getFullName());
            if (!CollectionUtils.isEmpty(dataset.getAuthors())) {
                d.setAuthors(dataset.getAuthors().stream()
                        .map(a -> new TargetInternalReference(
                                IdUtils.getUUID(a.getId()),
                                Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                        )).collect(Collectors.toList()));
            }
            DigitalIdentifierV3 digitalIdentifier = firstItemOrNull(dataset.getDigitalIdentifier());
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
            if (!CollectionUtils.isEmpty(dataset.getComponents())) {
                d.setComponents(dataset.getComponents().stream()
                        .map(c -> new TargetInternalReference(
                                IdUtils.getUUID(c.getId()),
                                c.getFullName()
                        )).collect(Collectors.toList()));
            }
            if (!CollectionUtils.isEmpty(dataset.getDatasetVersions())) {
                List<InternalDatasetVersion> sortedVersions = Helpers.sort(dataset.getDatasetVersions());                                         //v.getFullName()
                List<TargetInternalReference> references = sortedVersions.stream().map(v -> new TargetInternalReference(IdUtils.getUUID(v.getId()), v.getVersionIdentifier())).collect(Collectors.toList());
                d.setDatasets(references);
            }
        return d;
    }
}
