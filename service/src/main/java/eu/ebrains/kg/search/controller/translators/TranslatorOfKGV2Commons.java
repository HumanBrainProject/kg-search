package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.source.commonsV1andV2.HasEmbargo;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetFile;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

public class TranslatorOfKGV2Commons extends TranslatorCommons {
    final static String EMBARGOED = "embargoed";
    final static String UNDER_REVIEW = "under review";

    static boolean hasEmbargoStatus(HasEmbargo hasEmbargo, String... status) {
        String embargo = firstItemOrNull(hasEmbargo.getEmbargo());
        if (embargo == null) {
            return false;
        }
        return Arrays.stream(status).anyMatch(s -> embargo.toLowerCase().equals(s));
    }

    static TargetFile.FileImage getFileImage(List<String> url, boolean b) {
        String s = firstItemOrNull(url);
        return (StringUtils.isNotBlank(s)) ?
                new TargetFile.FileImage(
                        b,
                        s
                ) : null;
    }
}
