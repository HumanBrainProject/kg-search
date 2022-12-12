package eu.ebrains.kg.common.controller.translators.kgv3.commons;

import eu.ebrains.kg.common.model.source.HasAccessibility;
import eu.ebrains.kg.common.model.source.openMINDSv3.DatasetVersionV3;
import eu.ebrains.kg.common.model.source.openMINDSv3.MetadataModelVersionV3;
import eu.ebrains.kg.common.model.source.openMINDSv3.ModelVersionV3;
import eu.ebrains.kg.common.model.source.openMINDSv3.commons.NameWithIdentifier;

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

    public static Accessibility fromPayload(HasAccessibility instance) {
        return instance != null ? from(instance.getAccessibility()) : null;
    }

    private static Accessibility from(NameWithIdentifier nameWithIdentifier){
        if (nameWithIdentifier != null && nameWithIdentifier.getIdentifier() != null) {
            return Arrays.stream(Accessibility.values()).filter(a -> nameWithIdentifier.getIdentifier().contains(a.identifier)).findFirst().orElse(null);
        }
        return null;
    }


}
