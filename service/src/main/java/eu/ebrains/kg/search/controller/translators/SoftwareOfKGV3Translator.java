package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.openMINDSv2.SoftwareV2;
import eu.ebrains.kg.search.model.source.openMINDSv3.SoftwareVersionV3;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Version;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Versions;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Software;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetExternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.utils.IdUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static eu.ebrains.kg.search.controller.translators.TranslatorCommons.emptyToNull;
import static eu.ebrains.kg.search.controller.translators.TranslatorCommons.firstItemOrNull;

public class SoftwareOfKGV3Translator implements Translator<SoftwareVersionV3, Software>{

    public Software translate(SoftwareVersionV3 softwareVersion, DataStage dataStage, boolean liveMode) {
        Software s = new Software();
        Versions software = softwareVersion.getSoftware();
        s.setVersion(softwareVersion.getVersion());
        s.setId(IdUtils.getUUID(softwareVersion.getId()));
        s.setIdentifier(IdUtils.getUUID(softwareVersion.getIdentifier()));

        if (software != null && !CollectionUtils.isEmpty(software.getVersions())) {
            List<Version> sortedVersions = Helpers.sort(software.getVersions());
            List<TargetInternalReference> references = sortedVersions.stream().map(v -> new TargetInternalReference(IdUtils.getUUID(v.getId()), v.getVersionIdentifier())).collect(Collectors.toList());
            s.setVersions(references);
        }
        if (!StringUtils.isBlank(softwareVersion.getDescription())) {
            s.setDescription(softwareVersion.getDescription());
        } else if (software != null) {
            s.setDescription(software.getDescription());
        }
//        if (!StringUtils.isBlank(softwareVersion.getFullName())) {
//            s.setTitle(softwareVersion.getFullName());
//        } else if (software != null {
//            s.setTitle(software.getFullName());
//        }
        // For the UI we don't need the version number in the title as it is set in de dropdown
        if (software != null) {
            s.setTitle(software.getFullName());
            s.setSoftwareVersions(new TargetInternalReference(IdUtils.getUUID(software.getId()), software.getFullName()));
        }

        s.setAppCategory(emptyToNull(softwareVersion.getApplicationCategory()));

        if (!CollectionUtils.isEmpty(softwareVersion.getSourceCode())) {
            s.setSourceCode(softwareVersion.getSourceCode().stream()
                    .map(sc -> new TargetExternalReference(sc, sc))
                    .collect(Collectors.toList()));
        }
        s.setFeatures(emptyToNull(softwareVersion.getFeatures()));
        if (!CollectionUtils.isEmpty(softwareVersion.getDocumentation())) {
            s.setDocumentation(softwareVersion.getDocumentation().stream()
                    .map(d -> new TargetExternalReference(d, d))
                    .collect(Collectors.toList()));
        }
        s.setLicense(emptyToNull(softwareVersion.getLicense()));
        s.setOperatingSystem(emptyToNull(softwareVersion.getOperatingSystem()));

        String homepage = softwareVersion.getHomepage();
        if (StringUtils.isNotBlank(homepage)) {
            s.setHomepage(Collections.singletonList(new TargetExternalReference(homepage, homepage)));
        }
        return s;
    }
}
