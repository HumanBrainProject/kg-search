/*
 * Copyright 2018 - 2021 Swiss Federal Institute of Technology Lausanne (EPFL)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This open source software code was developed in part or in whole in the
 * Human Brain Project, funded from the European Union's Horizon 2020
 * Framework Programme for Research and Innovation under
 * Specific Grant Agreements No. 720270, No. 785907, and No. 945539
 * (Human Brain Project SGA1, SGA2 and SGA3).
 *
 */

package eu.ebrains.kg.search.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class IdUtils {
    public static String getUUID(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        String[] splitId = id.split("/");
        return splitId[splitId.length - 1];
    }

    /**
     * @return a list of all uuids as well as a prefixed variant of those. Please note, that this is only for compatibility reasons with the ids of the previous system.
     */
    public static List<String> getIdentifiersWithPrefix(String prefix, List<String> ids){
        final List<String> uuids = getUUID(ids);
        final List<String> prefixes = uuids.stream().map(uuid -> String.format("%s/%s", prefix, uuid)).collect(Collectors.toList());
        uuids.addAll(prefixes);
        return uuids;
    }

    public static List<String> getUUID(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return ids.stream().map(IdUtils::getUUID).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static String getTemplateQueryId(String queryId, String type){
        return UUID.nameUUIDFromBytes(String.format("%s/%s", queryId, type).getBytes(StandardCharsets.UTF_8)).toString();
    }
}
