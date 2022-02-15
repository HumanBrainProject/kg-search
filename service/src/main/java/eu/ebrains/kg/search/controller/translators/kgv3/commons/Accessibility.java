package eu.ebrains.kg.search.controller.translators.kgv3.commons;

import eu.ebrains.kg.search.model.source.openMINDSv3.DatasetVersionV3;
import eu.ebrains.kg.search.model.source.openMINDSv3.MetadataModelVersionV3;
import eu.ebrains.kg.search.model.source.openMINDSv3.ModelVersionV3;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.NameWithIdentifier;

import java.util.Arrays;

public enum Accessibility {
    FREE_ACCESS(Constants.OPENMINDS_INSTANCES + "/productAccessibility/freeAccess"),
    CONTROLLED_ACCESS(Constants.OPENMINDS_INSTANCES + "/productAccessibility/controlledAccess"),
    RESTRICTED_ACCESS(Constants.OPENMINDS_INSTANCES + "/productAccessibility/restrictedAccess"),
    UNDER_EMBARGO(Constants.OPENMINDS_INSTANCES + "/productAccessibility/underEmbargo");

    private final String identifier;

    Accessibility(String identifier) {
        this.identifier = identifier;
    }

    public static Accessibility fromPayload(DatasetVersionV3 datasetVersion) {
        return datasetVersion != null ? from(datasetVersion.getAccessibility()) : null;
    }

    public static Accessibility fromPayload(MetadataModelVersionV3 metadataModelVersionV3) {
        return metadataModelVersionV3 != null ? from(metadataModelVersionV3.getAccessibility()) : null;
    }
    public static Accessibility fromPayload(ModelVersionV3 modelVersionV3){
        return modelVersionV3 != null ? from(modelVersionV3.getAccessibility()) : null;
    }

    private static Accessibility from(NameWithIdentifier nameWithIdentifier){
        if (nameWithIdentifier != null && nameWithIdentifier.getIdentifier() != null) {
            return Arrays.stream(Accessibility.values()).filter(a -> nameWithIdentifier.getIdentifier().contains(a.identifier)).findFirst().orElse(null);
        }
        return null;
    }



}
