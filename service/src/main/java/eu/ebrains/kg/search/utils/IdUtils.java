package eu.ebrains.kg.search.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class IdUtils {
    public static String getUUID(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        String[] splitId = id.split("/");
        return splitId[splitId.length - 1];
    }

    public static List<String> getUUID(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return ids;
        }
        return ids.stream().map(IdUtils::getUUID).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
