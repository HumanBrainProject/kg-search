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

package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.target.elasticsearch.instances.Contributor;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.ISODateValue;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Value;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

public class ContributorHelpers {

    public static List<String> merge(List<String> list1, List<String> list2) {
        if (CollectionUtils.isEmpty(list1)) {
            return list2;
        }
        if (CollectionUtils.isEmpty(list2)) {
            return list1;
        }
        List<String> result = new ArrayList<>();
        result.addAll(list2);
        result.addAll(list1);
        return result.stream().distinct().collect(Collectors.toList());
    }

    public static Value<String> merge(Value<String> value1, Value<String> value2) {
        if (value2 == null) {
            return value1;
        }
        return value2;
    }

    public static List<String> mergeToListString(List<Value<String>> list1, List<Value<String>> list2) {
        if (CollectionUtils.isEmpty(list1) && CollectionUtils.isEmpty(list2)) {
            return null;
        }
        if (CollectionUtils.isEmpty(list1)) {
            return list2.stream().map(Value::getValue).collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(list2)) {
            return list1.stream().map(Value::getValue).collect(Collectors.toList());
        }
        Set<String> result = new HashSet<>();
        list1.forEach(value -> result.add(value.getValue()));
        list2.forEach(value -> result.add(value.getValue()));
        return new ArrayList<>(result);
    }

    public static List<TargetInternalReference> mergeReferences(List<TargetInternalReference> list1, List<TargetInternalReference> list2) {
        if (CollectionUtils.isEmpty(list1)) {
            return list2;
        }
        if (CollectionUtils.isEmpty(list2)) {
            return list1;
        }
        Map<String, TargetInternalReference> result = new HashMap<>();
        list1.forEach(ref -> result.put(ref.getUuid(), ref));
        list2.forEach(ref -> result.put(ref.getUuid(), ref));
        return new ArrayList<>(result.values());
    }

    public static ISODateValue merge(ISODateValue date1, ISODateValue date2) {
        if (date2 == null) {
            return date1;
        }
        return date2;
    }

    public static Contributor merge(Contributor contributorOfKGV2, Contributor contributorOfKGV3) {
        if (contributorOfKGV2 == null) {
            return contributorOfKGV3;
        }
        if (contributorOfKGV3 == null) {
            return contributorOfKGV2;
        }
        Contributor c = new Contributor();
        c.setId(contributorOfKGV3.getId());
        c.setIdentifier(merge(contributorOfKGV2.getIdentifier(), contributorOfKGV3.getIdentifier()));
        c.setFirstRelease(merge(contributorOfKGV2.getFirstRelease(), contributorOfKGV3.getFirstRelease()));
        c.setLastRelease(merge(contributorOfKGV2.getLastRelease(), contributorOfKGV3.getLastRelease()));
        c.setTitle(merge(contributorOfKGV2.getTitle(), contributorOfKGV3.getTitle()));
        c.setContributions(mergeReferences(contributorOfKGV2.getContributions(), contributorOfKGV3.getContributions()));
        c.setCustodianOf(mergeReferences(contributorOfKGV2.getCustodianOf(), contributorOfKGV3.getCustodianOf()));
        c.setCustodianOfModel(mergeReferences(contributorOfKGV2.getCustodianOfModel(), contributorOfKGV3.getCustodianOfModel()));
        c.setModelContributions(mergeReferences(contributorOfKGV2.getModelContributions(), contributorOfKGV3.getModelContributions()));
        c.setPublications(mergeToListString(contributorOfKGV2.getPublications(), contributorOfKGV3.getPublications()));
        c.setEditorId(merge(contributorOfKGV2.getEditorId(), contributorOfKGV3.getEditorId()));
        return c;
    }
}
