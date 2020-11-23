package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.DatabaseScope;
import eu.ebrains.kg.search.model.source.openMINDSv2.SoftwareV2;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Software;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetExternalReference;

import java.util.Comparator;
import java.util.stream.Collectors;

public class SoftwareTranslator implements Translator<SoftwareV2, Software> {

    public Software translate(SoftwareV2 softwareV2, DatabaseScope databaseScope, boolean liveMode) {
        Software s = new Software();
        softwareV2.getVersions().sort(Comparator.comparing(SoftwareV2.Version::getVersion).reversed());
        SoftwareV2.Version version = softwareV2.getVersions().get(0);
        if (databaseScope == DatabaseScope.INFERRED) {
            s.setEditorId(softwareV2.getEditorId());
        }
        s.setAppCategory(version.getApplicationCategory());
        s.setIdentifier(softwareV2.getIdentifier());
        s.setTitle(softwareV2.getTitle());
        s.setDescription(softwareV2.getDescription());
        s.setSourceCode(version.getSourceCode().stream()
                .map(sc -> new TargetExternalReference(sc, sc))
                .collect(Collectors.toList()));
        s.setFeatures(version.getFeatures());
        s.setDocumentation(version.getDocumentation().stream()
                .map(d -> new TargetExternalReference(d, d))
                .collect(Collectors.toList()));
        s.setLicense(version.getLicense());
        s.setOperatingSystem(version.getOperatingSystem());
        s.setVersion(version.getVersion());
        s.setHomepage(version.getHomepage().stream()
                .map(h -> new TargetExternalReference(h, h))
                .collect(Collectors.toList()));
        s.setFirstRelease(softwareV2.getFirstReleaseAt());
        s.setLastRelease(softwareV2.getLastReleaseAt());
        return s;
    }
}
