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

package eu.ebrains.kg.common.controller.translators.kgv3;

import eu.ebrains.kg.common.model.DataStage;
import eu.ebrains.kg.common.model.source.ResultsOfKGv3;
import eu.ebrains.kg.common.model.source.openMINDSv3.BrainAtlasV3;
import eu.ebrains.kg.common.model.source.openMINDSv3.CoordinateSpaceV3;
import eu.ebrains.kg.common.model.source.openMINDSv3.commons.FullNameRef;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.BrainAtlas;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.CoordinateSpace;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.BasicHierarchyElement;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.Children;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.Value;
import eu.ebrains.kg.common.utils.IdUtils;
import eu.ebrains.kg.common.utils.TranslationException;
import eu.ebrains.kg.common.utils.TranslatorUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class CoordinateSpaceV3Translator extends TranslatorV3<CoordinateSpaceV3, CoordinateSpace, CoordinateSpaceV3Translator.Result> {
    private static final String QUERY_ID = "38bfc9cb-aba4-4e78-818d-5b3e536cfa99";

    @Override
    public List<String> getQueryIds() {
        return Collections.singletonList(QUERY_ID);
    }

    @Override
    public Class<CoordinateSpaceV3Translator.Result> getResultType() {
        return CoordinateSpaceV3Translator.Result.class;
    }

    @Override
    public Class<CoordinateSpaceV3> getSourceType() {
        return CoordinateSpaceV3.class;
    }

    @Override
    public Class<CoordinateSpace> getTargetType() {
        return CoordinateSpace.class;
    }

    @Override
    public List<String> semanticTypes() {
        return Collections.singletonList("https://openminds.ebrains.eu/sands/CommonCoordinateSpace");
    }

    public static class Result extends ResultsOfKGv3<CoordinateSpaceV3> {
    }

    public CoordinateSpace translate(CoordinateSpaceV3 coordinateSpaceV3, DataStage dataStage, boolean liveMode, TranslatorUtils translatorUtils) throws TranslationException {
        CoordinateSpace c = new CoordinateSpace();
        c.setCategory(new Value<>("Coordinate space"));
        c.setDisclaimer(new Value<>("Not correct? The openMINDS coordinate spaces are community-driven. Please get in touch with the openMINDS development team [openMINDS@ebrains.eu](mailto:openMINDS@ebrains.eu) or raise an issue on the openMINDS GitHub if you'd like to correct a coordinate space or want to add more information to an coordinate spaces."));
        c.setId(IdUtils.getUUID(coordinateSpaceV3.getId()));
        c.setAllIdentifiers(coordinateSpaceV3.getIdentifier());
        c.setIdentifier(IdUtils.getUUID(coordinateSpaceV3.getIdentifier()).stream().distinct().collect(Collectors.toList()));
        c.setTitle(value(coordinateSpaceV3.getFullName()));
        c.setVersionIdentifier(value(coordinateSpaceV3.getVersionIdentifier()));
        c.setDescription(value(coordinateSpaceV3.getDescription()));
        c.setHomepage(link(coordinateSpaceV3.getHomepage()));
        c.setAnatomicalAxesOritentation(ref(coordinateSpaceV3.getAnatomicalAxesOrientation()));
        c.setNativeUnit(ref(coordinateSpaceV3.getNativeUnit()));
        c.setOntologyIdentifiers(simpleLink(coordinateSpaceV3.getOntologyIdentifier()));
        return c;
    }


    private BasicHierarchyElement<BrainAtlas.BrainAtlasOverview> buildHierarchyTreeForParcellationTerminology(BrainAtlasV3 brainAtlasV3, List<BrainAtlasVersionGroup> versionGroups) {
        BasicHierarchyElement<BrainAtlas.BrainAtlasOverview> e = new BasicHierarchyElement<>();
        e.setKey("root");
        e.setTitle(brainAtlasV3.getFullName());
        e.setColor("#e3dcdc");
        e.setChildren(organizeParcellationEntities(brainAtlasV3.getTerminology().getParcellationEntity().stream().sorted(Comparator.comparing(BrainAtlasV3.ParcellationEntity::getName, String.CASE_INSENSITIVE_ORDER)).collect(Collectors.toList()), versionGroups));
        return e;
    }

    private BasicHierarchyElement<BrainAtlas.BrainAtlasOverview> buildHierarchyTreeForVersions(BrainAtlasV3 brainAtlasV3, List<BrainAtlasVersionGroup> versionGroups) {
        BasicHierarchyElement<BrainAtlas.BrainAtlasOverview> e = new BasicHierarchyElement<>();
        e.setKey("root");
        e.setTitle(brainAtlasV3.getFullName());
        e.setColor("#e3dcdc");
        e.setChildren(versionGroups.stream().map(v -> buildHierarchyTree(brainAtlasV3, v)).collect(Collectors.toList()));
        return e;
    }

    @Getter
    @Setter
    private class BrainAtlasVersionGroup {
        private final Set<BrainAtlasV3.BrainAtlasVersion> versions = new HashSet<>();

        public String getCommonPattern() {
            return StringUtils.getCommonPrefix(versions.stream().map(BrainAtlasV3.BrainAtlasVersion::getVersionIdentifier).toArray(String[]::new));
        }

        public String getGroupName() {
            return StringUtils.stripEnd(getCommonPattern(), " ,.:;/");
        }

        public String getReducedVersionName(BrainAtlasV3.BrainAtlasVersion brainAtlasVersion){
            return brainAtlasVersion.getVersionIdentifier().substring(getCommonPattern().length());
        }
    }

    private List<BrainAtlasVersionGroup> organizeBrainAtlasVersions(List<BrainAtlasV3.BrainAtlasVersion> brainAtlasVersions) {
        List<BrainAtlasVersionGroup> brainAtlasVersionGroups = new ArrayList<>();

        // Group by alternative versions
        final Map<String, BrainAtlasV3.BrainAtlasVersion> brainAtlasVersionsById = brainAtlasVersions.stream().collect(Collectors.toMap(FullNameRef::getId, v -> v));
        for (BrainAtlasV3.BrainAtlasVersion brainAtlasVersion : brainAtlasVersions) {
            if (brainAtlasVersion.getIsAlternativeVersionOf() != null) {

                final Optional<Optional<BrainAtlasVersionGroup>> existingBrainAtlasVersionGroup = brainAtlasVersion.getIsAlternativeVersionOf().stream().map(b -> brainAtlasVersionGroups.stream().filter(bavg -> bavg.getVersions().stream().anyMatch(v -> b.equals(v.getId()))).findFirst()).findFirst();
                BrainAtlasVersionGroup brainAtlasVersionGroup;
                if (existingBrainAtlasVersionGroup.isPresent() && existingBrainAtlasVersionGroup.get().isPresent()) {
                    brainAtlasVersionGroup = existingBrainAtlasVersionGroup.get().get();
                } else {
                    brainAtlasVersionGroup = new BrainAtlasVersionGroup();
                    brainAtlasVersionGroups.add(brainAtlasVersionGroup);
                }
                brainAtlasVersionGroup.getVersions().add(brainAtlasVersion);
                brainAtlasVersionGroup.getVersions().addAll(brainAtlasVersion.getIsAlternativeVersionOf().stream().map(brainAtlasVersionsById::get).collect(Collectors.toSet()));
            }
        }

        //Sort by "is new version of"
        final List<BrainAtlasVersionGroup> sortedResult = new ArrayList<>();
        for (BrainAtlasVersionGroup brainAtlasVersionGroup : brainAtlasVersionGroups) {
            Integer evaluatedPosition = null;
            Boolean newest = null;
            for (BrainAtlasV3.BrainAtlasVersion version : brainAtlasVersionGroup.getVersions()) {
                if (version.getIsNewVersionOf() != null) {
                    final Optional<BrainAtlasVersionGroup> previous = sortedResult.stream().filter(s -> s.getVersions().stream().anyMatch(v -> version.getIsNewVersionOf().equals(v.getId()))).findFirst();
                    if (previous.isPresent()) {
                        final int index = sortedResult.indexOf(previous.get());
                        if (evaluatedPosition != null && evaluatedPosition != index) {
                            logger.error("Contradicting sorting order of the brain atlas version group {} - its versions provide different orders in sequence", version.getId());
                        } else {
                            evaluatedPosition = index;
                        }
                    }
                } else {
                    if (newest == null) {
                        newest = true;
                    } else {
                        logger.error("Contradicting information - brain atlas version {} is meant to be the newest but its alternative versions say something else", version.getId());
                    }
                }
            }
            if (newest != null || evaluatedPosition == null) {
                sortedResult.add(brainAtlasVersionGroup);
            } else {
                sortedResult.add(evaluatedPosition + 1, brainAtlasVersionGroup);
            }
        }
        return sortedResult;
    }

    private BasicHierarchyElement<BrainAtlas.BrainAtlasVersion> buildHierarchyTree(BrainAtlasV3 brainAtlasV3, BrainAtlasVersionGroup versionGroup) {
        if (versionGroup.getVersions().size() == 1) {
            return buildHierarchyTree(brainAtlasV3, null, versionGroup.getVersions().iterator().next()); //We directly return the version since it's not actually a group
        } else {
            BasicHierarchyElement<BrainAtlas.BrainAtlasVersion> e = new BasicHierarchyElement<>();
            e.setKey(UUID.randomUUID().toString());
            e.setTitle(versionGroup.getGroupName());
            e.setColor("#ffbe00");
            e.setChildren(versionGroup.getVersions().stream().sorted(Comparator.comparing(BrainAtlasV3.BrainAtlasVersion::getVersionIdentifier)).map(v -> buildHierarchyTree(brainAtlasV3, versionGroup, v)).collect(Collectors.toList()));


            return e;
        }
    }

    private BasicHierarchyElement<BrainAtlas.BrainAtlasVersion> buildHierarchyTree(BrainAtlasV3 brainAtlasV3, BrainAtlasVersionGroup versionGroup, BrainAtlasV3.BrainAtlasVersion version) {
        BasicHierarchyElement<BrainAtlas.BrainAtlasVersion> e = new BasicHierarchyElement<>();
        e.setKey(version.getId());
        e.setTitle(versionGroup != null ? versionGroup.getReducedVersionName(version) : version.getVersionIdentifier());
        e.setColor("#ffbe00");
        BrainAtlas.BrainAtlasVersion data = new BrainAtlas.BrainAtlasVersion();
        data.setCoordinateSpace(ref(version.getCoordinateSpace()));
        data.setTitle(value(version.getVersionIdentifier()));
        e.setData(data);
        return e;
    }

    private List<Children<BrainAtlas.RelevantBrainAtlasVersion>> createBrainAtlasVersions(List<BrainAtlasV3.ParcellationEntityVersion> versions, List<BrainAtlasVersionGroup> versionGroups){
        Map<String, BrainAtlasVersionGroup> lookupMap = new HashMap<>();
        versionGroups.forEach(vg -> {
            vg.getVersions().forEach(version -> {
                lookupMap.put(version.getId(), vg);
            });
        });
        Set<String> relevantBAVersions = new HashSet<>();
        Set<BrainAtlasVersionGroup> relevantVersionGroups = new HashSet<>();
        versions.forEach(v -> v.getBrainAtlasVersion().forEach(bav -> {
            relevantVersionGroups.add(lookupMap.get(bav));
            relevantBAVersions.add(bav);
        }));
        return children(relevantVersionGroups.stream().sorted(Comparator.comparing(BrainAtlasVersionGroup::getGroupName)).map(vg -> {
            BrainAtlas.RelevantBrainAtlasVersion version = new BrainAtlas.RelevantBrainAtlasVersion();
            version.setName(value(vg.getGroupName()));
            final Set<BrainAtlasV3.BrainAtlasVersion> actualBAversions = vg.getVersions().stream().filter(v -> relevantBAVersions.contains(v.getId())).collect(Collectors.toSet());
            if(actualBAversions.size()>1){
                final List<Children<BrainAtlas.RelevantBrainAtlasVersionSub>> children = children(actualBAversions.stream().sorted(Comparator.comparing(BrainAtlasV3.BrainAtlasVersion::getVersionIdentifier)).map(v -> {
                    BrainAtlas.RelevantBrainAtlasVersionSub subVersion = new BrainAtlas.RelevantBrainAtlasVersionSub();
                    subVersion.setName(value(vg.getReducedVersionName(v)));
                    return subVersion;
                }).collect(Collectors.toList()));
                if(!children.isEmpty()) {
                    version.setVersionGroups(children);
                }
            }
            return version;
        }).collect(Collectors.toList()));

    }



    private List<BasicHierarchyElement<BrainAtlas.ParcellationEntity>> organizeParcellationEntities(List<BrainAtlasV3.ParcellationEntity> parcellationEntities, List<BrainAtlasVersionGroup> versionGroups) {
        List<BasicHierarchyElement<BrainAtlas.ParcellationEntity>> result = new ArrayList<>();
        Map<String, BasicHierarchyElement<BrainAtlas.ParcellationEntity>> lookupMap = new HashMap<>();
        parcellationEntities.forEach(parcellationEntity -> {
            BasicHierarchyElement<BrainAtlas.ParcellationEntity> e = new BasicHierarchyElement<>();
            e.setKey(parcellationEntity.getId());
            e.setTitle(parcellationEntity.getName());
            e.setColor("#8a1f0d");
            BrainAtlas.ParcellationEntity pe = new BrainAtlas.ParcellationEntity();
            e.setData(pe);
            if(parcellationEntity.getVersions()!=null) {
                pe.setVersionGroups(createBrainAtlasVersions(parcellationEntity.getVersions(), versionGroups));
//                e.setChildren(parcellationEntity.getVersions().stream().map(v -> {
//                    BasicHierarchyElement<BrainAtlas.ParcellationEntityVersion> pev = new BasicHierarchyElement<>();
//                    pev.setKey(v.getId());
//                    pev.setTitle(v.getName());
//                    pev.setColor("#dde64f");
//                    return pev;
//                }).collect(Collectors.toList()));
            }
            lookupMap.put(parcellationEntity.getId(), e);
        });
        //2nd run - now we're attaching the child elements to the already existing structures.
        parcellationEntities.forEach(parcellationEntity -> {
            final BasicHierarchyElement<BrainAtlas.ParcellationEntity> currentHierarchyElement = lookupMap.get(parcellationEntity.getId());
            List<String> parentIds = parcellationEntity.getHasParent() != null ? parcellationEntity.getHasParent() : Collections.emptyList();
            final Set<BasicHierarchyElement<BrainAtlas.ParcellationEntity>> parents = parentIds.stream().map(lookupMap::get).collect(Collectors.toSet());
            if (parents.isEmpty()) {
                result.add(currentHierarchyElement);
            } else {
                parents.forEach(parent -> {
                    List<BasicHierarchyElement<BrainAtlas.ParcellationEntity>> children = new ArrayList<>();
                    if (parent.getChildren() == null) {
                        parent.setChildren(children);
                    }
                    ((List<BasicHierarchyElement<BrainAtlas.ParcellationEntity>>) parent.getChildren()).add(currentHierarchyElement);
                });
            }
        });
        return result;
    }

}
