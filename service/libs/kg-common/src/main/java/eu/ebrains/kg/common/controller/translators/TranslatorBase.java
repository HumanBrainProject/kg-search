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

package eu.ebrains.kg.common.controller.translators;

import eu.ebrains.kg.common.configuration.Configuration;
import eu.ebrains.kg.common.model.source.IsCiteable;
import eu.ebrains.kg.common.model.source.openMINDSv3.commons.*;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.HasCitation;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.*;
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

    private transient Configuration configuration;

    public Configuration getConfiguration() {
        return configuration != null ? configuration : Configuration.defaultConfiguration();
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

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

    protected TargetInternalReference ref(FullNameRefWithVersion ref) {
        if (ref != null) {
            final String uuid = IdUtils.getUUID(ref.getId());
            return new TargetInternalReference(uuid,  StringUtils.isNotBlank(ref.getFullName()) && StringUtils.isNotBlank(ref.getVersionIdentifier()) ? String.format("%s (%s)", ref.getFullName(), ref.getVersionIdentifier()) : StringUtils.defaultIfBlank(ref.getFullName(), uuid));
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

    protected List<TargetInternalReference> refVersion(List<? extends FullNameRefForResearchProduct> refs, boolean sorted) {
        if (!CollectionUtils.isEmpty(refs)) {
            Stream<TargetInternalReference> targetInternalReferenceStream = refs.stream().map(r -> {
                if (r instanceof FullNameRefForResearchProductVersion) {
                    return this.ref((FullNameRefForResearchProductVersion) r);
                }
                if (r instanceof FullNameRefForResearchProductVersionTarget) {
                    return this.ref((FullNameRefForResearchProductVersionTarget) r);
                }
                return null;
            }).filter(Objects::nonNull);
            if (sorted) {
                targetInternalReferenceStream = targetInternalReferenceStream.sorted();
            }
            return targetInternalReferenceStream.collect(Collectors.toList());
        }
        return null;
    }

    protected List<TargetInternalReference> refExtendedVersion(List<? extends ExtendedFullNameRefForResearchProductVersion> refs, boolean sort) {
        if (!CollectionUtils.isEmpty(refs)) {
            Stream<TargetInternalReference> targetInternalReferenceStream = refs.stream().filter(r -> r.getGrouping() == null).map(this::ref).filter(Objects::nonNull);
            if (sort) {
                targetInternalReferenceStream = targetInternalReferenceStream.sorted();
            }
            List<TargetInternalReference> result = targetInternalReferenceStream.collect(Collectors.toList());
            final Map<String, ? extends List<? extends ExtendedFullNameRefForResearchProductVersion>> byGrouping = refs.stream().filter(r -> r.getGrouping() != null).collect(Collectors.groupingBy(r -> r.getGrouping()));
            byGrouping.keySet().stream().sorted().forEach(group -> {
                if(!result.isEmpty()) {
                    result.add(new TargetInternalReference(null, ""));
                }
                result.add(new TargetInternalReference(null, group));
                final List<? extends ExtendedFullNameRefForResearchProductVersion> instances = byGrouping.get(group);
                Stream<TargetInternalReference> targetInternalReferenceStreamForGroups = instances.stream().map(this::ref).filter(Objects::nonNull);
                if (sort) {
                    targetInternalReferenceStreamForGroups = targetInternalReferenceStreamForGroups.sorted();
                }
                result.addAll(targetInternalReferenceStreamForGroups.collect(Collectors.toList()));
            });
            if(!CollectionUtils.isEmpty(result)){
                return result;
            }
        }
        return null;
    }

    protected TargetInternalReference ref(FullNameRefForResearchProductVersion ref) {
        if (ref != null) {
            String name = StringUtils.defaultIfBlank(ref.getFullName(), ref.getFallbackName());
            String uuid = IdUtils.getUUID(ref.getId());
            if (name == null) {
                name = uuid;
            }
            String versionedName = StringUtils.isNotBlank(ref.getVersionIdentifier()) ? String.format("%s %s", name, ref.getVersionIdentifier()) : name;
            return new TargetInternalReference(uuid, versionedName);
        }
        return null;
    }

    protected TargetInternalReference ref(FullNameRefForResearchProductVersionTarget ref) {
        if (ref != null) {
            FullNameRef researchProduct = ref.getResearchProduct();
            String name = StringUtils.defaultIfBlank(ref.getFullName(), researchProduct.getFullName());
            String uuid = IdUtils.getUUID(researchProduct.getId());
            if (name == null) {
                name = uuid;
            }
            String versionedName = StringUtils.isNotBlank(ref.getVersionIdentifier()) ? String.format("%s %s", name, ref.getVersionIdentifier()) : name;
            //TODO enable link when BrainAtals card should be enable
            return new TargetInternalReference(null, versionedName);
            //return new TargetInternalReference(uuid, versionedName, new TargetInternalReference.Context(ref.getTab(), IdUtils.getUUID(ref.getId())));
        }
        return null;
    }

    protected TargetInternalReference ref(ExtendedFullNameRefForResearchProductVersion ref) {
        if (ref != null) {
            return ref(ref.getRelevantReference());
        }
        return null;
    }

    protected TargetExternalReference link(FileRepository ref) {
        if (ref != null && StringUtils.isNotBlank(ref.getIri())) {
            return new TargetExternalReference(ref.getIri(), ref.getIri() != null ? ref.getFullName() : ref.getIri());
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

    protected void handleCitation(IsCiteable source, HasCitation target) {
        String doi = source.getDoi();
        String citation = source.getHowToCite();
        if (StringUtils.isNotBlank(citation)) {
            target.setCustomCitation(value(citation));
        }
        if (StringUtils.isNotBlank(doi)) {
            final String doiWithoutPrefix = Helpers.stripDOIPrefix(doi);
            target.setDoi(value(doiWithoutPrefix));
            if (StringUtils.isBlank(citation)) {
                target.setCitation(value(doiWithoutPrefix));
            }
        }
    }

    protected List<TargetInternalReference> refAnatomical(List<AnatomicalLocation> anatomicalLocations, boolean sorted) {
        if (!CollectionUtils.isEmpty(anatomicalLocations)) {
            Stream<TargetInternalReference> targetInternalReferenceStream = anatomicalLocations.stream().map(this::refAnatomical).filter(Objects::nonNull);
            if (sorted) {
                targetInternalReferenceStream = targetInternalReferenceStream.sorted();
            }
            return targetInternalReferenceStream.collect(Collectors.toList());
        }
        return null;
    }

    protected TargetInternalReference refAnatomical(AnatomicalLocation a){
        if(StringUtils.isNotBlank(a.getBrainAtlas())){
            return new TargetInternalReference(null, String.format("%s (%s)", StringUtils.isNotBlank(a.getFullName()) ? a.getFullName() : a.getFallbackName(),  a.getBrainAtlas()));
        }
        else if(a.getBrainAtlasVersion() != null){
            String name = String.format("%s %s", StringUtils.isNotBlank(a.getBrainAtlasVersion().getFullName()) ? a.getBrainAtlasVersion().getFullName() : a.getBrainAtlasVersion().getFallbackName(), a.getBrainAtlasVersion().getVersionIdentifier());
            return new TargetInternalReference(null, String.format("%s (%s)", StringUtils.isNotBlank(a.getFullName()) ? a.getFullName() : a.getFallbackName(), name));
        }
        else{
            return ref(a);
        }
    }
}