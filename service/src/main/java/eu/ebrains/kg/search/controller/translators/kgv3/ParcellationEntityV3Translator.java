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

package eu.ebrains.kg.search.controller.translators.kgv3;


import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.ResultsOfKGv3;
import eu.ebrains.kg.search.model.source.openMINDSv3.ParcellationEntityV3;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Version;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.ParcellationEntity;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Children;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetExternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Value;
import eu.ebrains.kg.search.services.DOICitationFormatter;
import eu.ebrains.kg.search.utils.IdUtils;
import eu.ebrains.kg.search.utils.TranslationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class ParcellationEntityV3Translator extends TranslatorV3<ParcellationEntityV3, ParcellationEntity, ParcellationEntityV3Translator.Result> {

    public static class Result extends ResultsOfKGv3<ParcellationEntityV3> {
    }

    @Override
    public Class<ParcellationEntityV3> getSourceType() {
        return ParcellationEntityV3.class;
    }

    @Override
    public Class<ParcellationEntity> getTargetType() {
        return ParcellationEntity.class;
    }

    @Override
    public Class<ParcellationEntityV3Translator.Result> getResultType() {
        return ParcellationEntityV3Translator.Result.class;
    }

    @Override
    public List<String> getQueryIds() {
        return Collections.singletonList("7c61cc82-415e-4ba1-8dee-940741e8f128");
    }

    @Override
    public List<String> semanticTypes() {
        return Collections.singletonList("https://openminds.ebrains.eu/sands/ParcellationEntity");
    }


    private void handleVersion(ParcellationEntityV3.VersionWithServiceLink s, List<Children<ParcellationEntity.VersionWithServiceLink>> collector) {
        ParcellationEntity.VersionWithServiceLink version = new ParcellationEntity.VersionWithServiceLink();
        version.setVersion(value(s.getVersionIdentifier()));
        version.setLaterality(ref(s.getLaterality()));
        if(!CollectionUtils.isEmpty(s.getInspiredBy())){
            version.setInspiredBy(s.getInspiredBy().stream().map(i -> {
                TargetInternalReference reference = ref(i.getDataset());
                return reference;
            }).distinct().sorted().collect(Collectors.toList()));
        }
        if(!CollectionUtils.isEmpty(s.getVisualizedIn())){
            version.setVisualizedIn(s.getVisualizedIn().stream().map(i -> {
                TargetInternalReference reference = ref(i.getDataset());
                return reference;
            }).distinct().sorted().collect(Collectors.toList()));
        }
        collector.add(new Children<>(version));
    }

    private void handleVersionWithServiceLink(ParcellationEntityV3.VersionWithServiceLink s, List<Children<ParcellationEntity.VersionWithServiceLink>> collector) {
        ParcellationEntity.VersionWithServiceLink version = new ParcellationEntity.VersionWithServiceLink();
        version.setVersion(value(s.getVersionIdentifier()));
        if (s.getViewer() != null) {
            version.setViewer(new TargetExternalReference(s.getViewer().getUrl(), String.format("Show in %s", s.getViewer().getService())));
            collector.add(new Children<>(version));
        }
    }


    private void findNewVersion(List<Version> versions, String versionIdentifier, List<String> collector) {
        final Optional<Version> version = versions.stream().filter(v -> v.getIsNewVersionOf() != null && v.getIsNewVersionOf().equals(versionIdentifier)).findFirst();
        if (version.isPresent()) {
            final String identifier = version.get().getVersionIdentifier();
            collector.add(identifier);
            findNewVersion(versions, identifier, collector);
        }
    }

    private List<List<String>> sortDependencyChains(List<Version> versions) {
        Set<String> containedVersions = versions.stream().map(Version::getVersionIdentifier).filter(Objects::nonNull).collect(Collectors.toSet());
        final List<Version> startingPoints = versions.stream().filter(v -> v.getIsNewVersionOf() == null || !containedVersions.contains(v.getIsNewVersionOf())).collect(Collectors.toList());
        Map<String, List<String>> dependencyChains = new HashMap<>();
        return startingPoints.stream().map(startingPoint -> {
            final ArrayList<String> collector = new ArrayList<>();
            collector.add(startingPoint.getVersionIdentifier());
            dependencyChains.put(startingPoint.getVersionIdentifier(), collector);
            findNewVersion(versions, startingPoint.getVersionIdentifier(), collector);
            return collector;
        }).collect(Collectors.toList());

    }

    private List<List<String>> sortIdentifierGroups(List<ParcellationEntityV3.VersionWithServiceLink> versions) {
        List<List<String>> versionIdentifierGroups = new ArrayList<>();
        //We exclude versions which do not have a brainAtlasVersion, since they are not complete...
        for (ParcellationEntityV3.VersionWithServiceLink v : versions) {
            final Version brainAtlasVersion = v.getBrainAtlasVersion();
            List<String> versionIdentifiers = new ArrayList<>();
            versionIdentifiers.add(brainAtlasVersion.getVersionIdentifier());
            versionIdentifiers.addAll(brainAtlasVersion.getIsAlternativeVersionOf());
            final List<String> existing = versionIdentifierGroups.stream().filter(grp -> grp.stream().anyMatch(versionIdentifiers::contains)).flatMap(Collection::stream).collect(Collectors.toList());
            existing.addAll(versionIdentifiers);
            versionIdentifierGroups = versionIdentifierGroups.stream().filter(grp -> grp.stream().noneMatch(versionIdentifiers::contains)).collect(Collectors.toList());
            versionIdentifierGroups.add(existing.stream().distinct().collect(Collectors.toList()));
        }

        final List<Version> brainAtlasVersions = versions.stream().map(ParcellationEntityV3.VersionWithServiceLink::getBrainAtlasVersion).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        final List<List<String>> dependencyChains = sortDependencyChains(brainAtlasVersions);

        List<List<String>> sortedVersionIdentifierGroups = new ArrayList<>(versionIdentifierGroups.size());
        //Ensure that we're having a steady progress - otherwise, we end up in a infinite loop
        Integer lastSize = null;
        while (versionIdentifierGroups.size() > 0 && (lastSize == null || lastSize > versionIdentifierGroups.size())) {
            lastSize = versionIdentifierGroups.size();
            for (List<String> v : versionIdentifierGroups) {
                int maxIndex = 0;
                for (String identifier : v) {
                    for (List<String> dependencyChain : dependencyChains) {
                        final int index = dependencyChain.indexOf(identifier);
                        if (index > maxIndex) {
                            maxIndex = index;
                        }
                    }
                }
                if (maxIndex == 0) {
                    sortedVersionIdentifierGroups.add(v);
                    dependencyChains.forEach(d -> d.removeAll(v));
                    versionIdentifierGroups.remove(v);
                    break;
                }
            }
        }
        return sortedVersionIdentifierGroups;
    }

    private List<Children<ParcellationEntity.VersionWithServiceLink>> sortAndHandleGroups(List<ParcellationEntityV3.VersionWithServiceLink> versions, BiConsumer<ParcellationEntityV3.VersionWithServiceLink, List<Children<ParcellationEntity.VersionWithServiceLink>>> consumer) {
        List<Children<ParcellationEntity.VersionWithServiceLink>> collector = new ArrayList<>();
        for (List<String> versionIdentifierGroup : sortIdentifierGroups(versions)) {
            List<ParcellationEntityV3.VersionWithServiceLink> group = new ArrayList<>();
            for (ParcellationEntityV3.VersionWithServiceLink version : versions) {
                if (versionIdentifierGroup.contains(version.getBrainAtlasVersion().getVersionIdentifier())) {
                    group.add(version);
                }
            }
            group.sort(Comparator.comparing(ParcellationEntityV3.VersionWithServiceLink::getVersionIdentifier));
            if (!CollectionUtils.isEmpty(group)) {
                group.forEach(s -> consumer.accept(s, collector));
            }
            if(collector.size()>0 && StringUtils.isNotBlank(collector.get(collector.size()-1).getChildren().getVersion().getValue())){
                final ParcellationEntity.VersionWithServiceLink emptyCategory = new ParcellationEntity.VersionWithServiceLink();
                emptyCategory.setVersion(new Value<>(" "));
                collector.add(new Children<>(emptyCategory));
            }
        }
        if(collector.size()>0 && StringUtils.isBlank(collector.get(collector.size()-1).getChildren().getVersion().getValue())){
            collector.remove(collector.size()-1);
        }
        return collector;
    }

    public ParcellationEntity translate(ParcellationEntityV3 parcellationEntity, DataStage dataStage, boolean liveMode, DOICitationFormatter doiCitationFormatter) throws TranslationException {
        ParcellationEntity pe = new ParcellationEntity();

        pe.setCategory(new Value<>("Parcellation Entity Overview"));
        pe.setDisclaimer(new Value<>("Please alert us at [curation-support@ebrains.eu](mailto:curation-support@ebrains.eu) for errors or quality concerns regarding the dataset, so we can forward this information to the Data Custodian responsible."));


        final List<ParcellationEntityV3.VersionWithServiceLink> versions = parcellationEntity.getVersions().stream().filter(v -> v.getBrainAtlasVersion() != null && v.getBrainAtlasVersion().getVersionIdentifier() != null).collect(Collectors.toList());

        final List<Children<ParcellationEntity.VersionWithServiceLink>> versionTable = sortAndHandleGroups(versions, this::handleVersion);
        if (!CollectionUtils.isEmpty(versionTable)) {
            pe.setVersionsTable(versionTable);
        }
        final List<Children<ParcellationEntity.VersionWithServiceLink>> viewerLinks = sortAndHandleGroups(versions, this::handleVersionWithServiceLink);
        if (!CollectionUtils.isEmpty(viewerLinks)) {
            pe.setViewerLinks(viewerLinks);
        }
        pe.setId(IdUtils.getUUID(pe.getId()));

        pe.setAllIdentifiers(pe.getIdentifier());
        pe.setIdentifier(IdUtils.getUUID(pe.getIdentifier()).stream().distinct().collect(Collectors.toList()));
        if (StringUtils.isNotBlank(parcellationEntity.getName())) {
            pe.setTitle(value(parcellationEntity.getName()));
        }
        //TODO switch to link once the brain atlas has its own representation
        if (parcellationEntity.getBrainAtlas() != null) {
            pe.setBrainAtlas(value(parcellationEntity.getBrainAtlas().getFullName()));
        }
        pe.setParents(ref(parcellationEntity.getParents(), true));
        pe.setChildren(ref(parcellationEntity.getIsParentOf(), true));
        pe.setRelatedUberonTerm(ref(parcellationEntity.getRelatedUBERONTerm()));
        pe.setOntologyIdentifier(value(parcellationEntity.getOntologyIdentifier()));

        return pe;
    }
}
