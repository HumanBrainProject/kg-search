package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.openMINDSv3.SoftwareV3;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Version;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.SoftwareVersions;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.utils.IdUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static eu.ebrains.kg.search.controller.translators.TranslatorCommons.firstItemOrNull;

public class SoftwareVersionsOfKGV3Translator implements Translator<SoftwareV3, SoftwareVersions>{

    public SoftwareVersions translate(SoftwareV3 software, DataStage dataStage, boolean liveMode) {
        SoftwareVersions s = new SoftwareVersions();
        s.setId(IdUtils.getUUID(software.getId()));
        s.setIdentifier(IdUtils.getUUID(software.getIdentifier()));
        s.setDescription(software.getDescription());
        s.setTitle(software.getTitle());
        if (!CollectionUtils.isEmpty(software.getDeveloper())) {
            s.setDevelopers(software.getDeveloper().stream()
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        }
        if (!CollectionUtils.isEmpty(software.getCustodian())) {
            s.setCustodians(software.getCustodian().stream()
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        }

        String citation = software.getHowToCite();
        String digitalIdentifier = firstItemOrNull(software.getDigitalIdentifier());
        if (digitalIdentifier != null) {
            if (StringUtils.isNotBlank(citation) && StringUtils.isNotBlank(digitalIdentifier)) {
                String url = URLEncoder.encode(digitalIdentifier, StandardCharsets.UTF_8);
                s.setCitation(citation + String.format(" [DOI: %s]\n[DOI: %s]: https://doi.org/%s", digitalIdentifier, digitalIdentifier, url));
            }
            if (StringUtils.isNotBlank(digitalIdentifier)) {
                s.setDoi(digitalIdentifier);
            }
        }
        if (!CollectionUtils.isEmpty(software.getVersions())) {
            List<Version> sortedVersions = Helpers.sort(software.getVersions());                                         //v.getFullName()
            List<TargetInternalReference> references = sortedVersions.stream().map(v -> new TargetInternalReference(IdUtils.getUUID(v.getId()), v.getVersionIdentifier())).collect(Collectors.toList());
            s.setVersions(references);
        }
        return s;
    }
}
