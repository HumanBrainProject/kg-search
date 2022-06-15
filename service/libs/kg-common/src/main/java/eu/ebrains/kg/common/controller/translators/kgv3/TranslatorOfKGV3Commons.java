package eu.ebrains.kg.common.controller.translators.kgv3;

import eu.ebrains.kg.common.controller.translators.TranslatorCommons;
import eu.ebrains.kg.common.model.source.openMINDSv3.commons.HasEmbargo;

import java.util.Arrays;

public class TranslatorOfKGV3Commons extends TranslatorCommons {
    final static String FREE_ACCESS = "free access";
    final static String UNDER_EMBARGO = "under embargo";
    final static String CONTROLLED_ACCESS = "controlled access"; // named "under review" in KGv1
    final static String RESTRICTED_ACCESS = "restricted access";

    static boolean hasEmbargoStatus(HasEmbargo hasEmbargo, String... status) {
        String embargo = firstItemOrNull(hasEmbargo.getEmbargo());
        if (embargo == null) {
            return false;
        }
        return Arrays.stream(status).anyMatch(s -> embargo.toLowerCase().equals(s));
    }

}
