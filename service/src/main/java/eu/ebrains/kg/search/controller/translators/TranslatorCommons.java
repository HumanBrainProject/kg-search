package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.source.commons.HasEmbargo;
import eu.ebrains.kg.search.model.source.openMINDSv1.DatasetV1;
import eu.ebrains.kg.search.model.source.openMINDSv1.SampleV1;
import eu.ebrains.kg.search.model.source.openMINDSv2.ModelV2;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetFile;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;

public class TranslatorCommons {

    final static String EMBARGOED = "embargoed";
    final static String UNDER_REVIEW = "under review";

    static <T> T firstItemOrNull(List<T> list) {
        return list != null && !list.isEmpty() ? list.get(0) : null;
    }

    static <T> List<T> emptyToNull(List<T> list){
        return CollectionUtils.isEmpty(list) ? null : list;
    }

    static boolean hasEmbargoStatus(HasEmbargo hasEmbargo, String... status) {
        String embargo = firstItemOrNull(hasEmbargo.getEmbargo());
        if (embargo == null) {
            return false;
        }
        return Arrays.stream(status).anyMatch(s -> embargo.toLowerCase().equals(s));
    }

    static TargetFile.FileImage getFileImage(List<String> url, boolean b) {
        return (!CollectionUtils.isEmpty(url) && url.get(0) != null) ?
                new TargetFile.FileImage(
                        url.get(0),
                        b
                ) : null;
    }

}
