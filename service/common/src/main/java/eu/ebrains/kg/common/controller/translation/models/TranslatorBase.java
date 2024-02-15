/*
 *  Copyright 2018 - 2021 Swiss Federal Institute of Technology Lausanne (EPFL)
 *  Copyright 2021 - 2023 EBRAINS AISBL
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0.
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  This open source software code was developed in part or in whole in the
 *  Human Brain Project, funded from the European Union's Horizon 2020
 *  Framework Programme for Research and Innovation under
 *  Specific Grant Agreements No. 720270, No. 785907, and No. 945539
 *  (Human Brain Project SGA1, SGA2 and SGA3).
 */

package eu.ebrains.kg.common.controller.translation.models;

import eu.ebrains.kg.common.controller.translation.utils.TranslationUtils;
import eu.ebrains.kg.common.model.source.*;
import eu.ebrains.kg.common.model.target.*;
import eu.ebrains.kg.common.model.target.HasCitation;
import eu.ebrains.kg.common.utils.IdUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class TranslatorBase {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected Value<Integer> value(Integer v) {
        if (v != null) {
            return new Value<>(v);
        }
        return null;
    }

    protected Value<Boolean> value(Boolean v) {
        if (v != null) {
            return new Value<>(v);
        }
        return null;
    }

    protected Value<String> value(String v) {
        if (StringUtils.isNotBlank(v)) {
            return new Value<>(v.trim());
        }
        return null;
    }


    protected ISODateValue value(Date date) {
        if (date != null) {
            return new ISODateValue(date);
        } else {
            return null;
        }

    }

    protected <T> List<Children<T>> children(List<T> values) {
        if (!CollectionUtils.isEmpty(values)) {
            return values.stream().map(Children::new).collect(Collectors.toList());
        }
        return null;
    }

    protected List<Value<String>> value(List<String> values) {
        if (!CollectionUtils.isEmpty(values)) {
            return values.stream().map(this::value).filter(Objects::nonNull).collect(Collectors.toList());
        }
        return null;
    }

    protected List<TargetExternalReference> simpleLink(List<String> urls) {
        if (!CollectionUtils.isEmpty(urls)) {
            return urls.stream().map(this::link).filter(Objects::nonNull).collect(Collectors.toList());
        }
        return null;
    }

    protected List<TargetExternalReference> link(List<ExternalRef> urls) {
        if (!CollectionUtils.isEmpty(urls)) {
            return urls.stream().map(this::link).filter(Objects::nonNull).collect(Collectors.toList());
        }
        return null;
    }

    protected <T> List<T> emptyToNull(List<T> list) {
        return CollectionUtils.isEmpty(list) ? null : list;
    }

    protected TargetExternalReference link(String url) {
        if (StringUtils.isNotBlank(url)) {
            return new TargetExternalReference(url.trim(), url.trim());
        }
        return null;
    }

    protected TargetInternalReference ref(FullNameRef ref) {
        if (ref != null) {
            final String uuid = IdUtils.getUUID(ref.getId());
            return new TargetInternalReference(uuid, StringUtils.defaultIfBlank(ref.getFullName(), uuid));
        }
        return null;
    }


    protected TargetInternalReference emptyRef(String ref) {
        return new TargetInternalReference(null, ref);
    }

    protected List<TargetInternalReference> emptyRef(List<String> refs) {
        if (!CollectionUtils.isEmpty(refs)) {
            return refs.stream().filter(Objects::nonNull).map(this::emptyRef).filter(Objects::nonNull).collect(Collectors.toList());
        }
        return null;
    }

    protected List<TargetInternalReference> ref(List<? extends FullNameRef> refs) {
        return ref(refs, false);
    }

    protected List<TargetInternalReference> ref(List<? extends FullNameRef> refs, boolean sorted) {
        if (!CollectionUtils.isEmpty(refs)) {
            Stream<TargetInternalReference> targetInternalReferenceStream = refs.stream().map(this::ref).filter(Objects::nonNull);
            if (sorted) {
                targetInternalReferenceStream = targetInternalReferenceStream.sorted();
            }
            return targetInternalReferenceStream.collect(Collectors.toList());
        }
        return null;
    }





    protected TargetExternalReference link(ExternalRef ref) {
        if (ref != null && StringUtils.isNotBlank(ref.getUrl())) {
            return new TargetExternalReference(ref.getUrl(), ref.getUrl() != null ? ref.getLabel() : ref.getUrl());
        }
        return null;
    }

    public <T> List<T> createList(T... items) {
        List<T> l = new ArrayList<>();
        for (T item : items) {
            if (item != null) {
                l.add(item);
            }
        }
        return l;
    }


}