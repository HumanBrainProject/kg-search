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

package eu.ebrains.kg.common.controller.translators.kgv3;

import eu.ebrains.kg.common.controller.translators.Helpers;
import eu.ebrains.kg.common.model.DataStage;
import eu.ebrains.kg.common.model.source.ResultsOfKGv3;
import eu.ebrains.kg.common.model.source.openMINDSv3.BrainAtlasV3;
import eu.ebrains.kg.common.model.source.openMINDSv3.commons.FullNameRef;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.BrainAtlas;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.BasicHierarchyElement;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.Children;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.Value;
import eu.ebrains.kg.common.utils.IdUtils;
import eu.ebrains.kg.common.utils.TranslationException;
import eu.ebrains.kg.common.utils.TranslatorUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

public class BrainAtlasV3Translator extends TranslatorV3<BrainAtlasV3, BrainAtlas, BrainAtlasV3Translator.Result> {
    private static final String QUERY_ID = "a07dff10-c808-44b1-84aa-f725fa4aa119";

    @Override
    public List<String> getQueryIds() {
        return Collections.singletonList(QUERY_ID);
    }

    @Override
    public Class<BrainAtlasV3Translator.Result> getResultType() {
        return BrainAtlasV3Translator.Result.class;
    }

    @Override
    public Class<BrainAtlasV3> getSourceType() {
        return BrainAtlasV3.class;
    }

    @Override
    public Class<BrainAtlas> getTargetType() {
        return BrainAtlas.class;
    }

    @Override
    public List<String> semanticTypes() {
        return Collections.singletonList("https://openminds.ebrains.eu/sands/BrainAtlas");
    }

    public static class Result extends ResultsOfKGv3<BrainAtlasV3> {
    }

    public BrainAtlas translate(BrainAtlasV3 brainAtlasV3, DataStage dataStage, boolean liveMode, TranslatorUtils translatorUtils) throws TranslationException {
        BrainAtlas b = new BrainAtlas();
        b.setCategory(new Value<>("Brain Atlas"));
        b.setDisclaimer(new Value<>("Not correct? The openMINDS brain atlases are community-driven. Please get in touch with the openMINDS development team [openMINDS@ebrains.eu](mailto:openMINDS@ebrains.eu) or raise an issue on the openMINDS GitHub if you'd like to correct a brain atlas or want to add more information to an atlas."));
        b.setId(IdUtils.getUUID(brainAtlasV3.getId()));
        b.setAllIdentifiers(brainAtlasV3.getIdentifier());
        b.setIdentifier(IdUtils.getUUID(brainAtlasV3.getIdentifier()).stream().distinct().collect(Collectors.toList()));
        b.setTitle(value(brainAtlasV3.getFullName()));
        b.setDescription(value(brainAtlasV3.getDescription()));
        if (!CollectionUtils.isEmpty(brainAtlasV3.getAuthor())) {
            b.setContributors(brainAtlasV3.getAuthor().stream()
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        }
        b.setHomepage(link(brainAtlasV3.getHomepage()));
        handleCitation(brainAtlasV3, b);
        final List<BrainAtlasVersionGroup> brainAtlasVersionGroups = organizeBrainAtlasVersions(brainAtlasV3.getBrainAtlasVersion(), translatorUtils.getErrors());
        b.setParcellationTerminology(buildHierarchyTreeForParcellationTerminology(brainAtlasV3, brainAtlasVersionGroups));
        b.setVersions(buildHierarchyTreeForVersions(brainAtlasV3, brainAtlasVersionGroups));
        return b;
    }


    private BasicHierarchyElement<BrainAtlas.BrainAtlasOverview> buildHierarchyTreeForParcellationTerminology(BrainAtlasV3 brainAtlasV3, List<BrainAtlasVersionGroup> versionGroups) {
        BasicHierarchyElement<BrainAtlas.BrainAtlasOverview> e = new BasicHierarchyElement<>();
        e.setKey("root");
        e.setTitle(brainAtlasV3.getFullName());
        e.setColor("#e3dcdc");
        if(brainAtlasV3.getTerminology() != null && brainAtlasV3.getTerminology().getParcellationEntity() != null) {
            e.setChildren(organizeParcellationEntities(brainAtlasV3.getTerminology().getParcellationEntity().stream().sorted(Comparator.comparing(BrainAtlasV3.ParcellationEntity::getName, String.CASE_INSENSITIVE_ORDER)).collect(Collectors.toList()), versionGroups));
        }
        BrainAtlas.BrainAtlasOverview overview = new BrainAtlas.BrainAtlasOverview();
        e.setData(overview);
        if(brainAtlasV3.getTerminology() != null && brainAtlasV3.getTerminology().getParcellationEntity() != null) {
            overview.setNumberOfParcellationEntities(value(brainAtlasV3.getTerminology().getParcellationEntity().size()));
        }
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

    private List<BrainAtlasVersionGroup> organizeBrainAtlasVersions(List<BrainAtlasV3.BrainAtlasVersion> brainAtlasVersions, List<String> errors) {
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
                            String error = String.format("Contradicting sorting order of the brain atlas version group %s - its versions provide different orders in sequence", version.getId());
                            logger.error(error);
                            errors.add(error);
                        } else {
                            evaluatedPosition = index;
                        }
                    }
                } else {
                    if (newest == null) {
                        newest = true;
                    } else {
                        final String error = String.format("Contradicting information - brain atlas version %s is meant to be the newest but its alternative versions say something else", version.getId());
                        logger.error(error);
                        errors.add(error);
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
        e.setKey(IdUtils.getUUID(version.getId()));
        e.setTitle(versionGroup != null ? versionGroup.getReducedVersionName(version) : version.getVersionIdentifier());
        e.setColor("#ffbe00");
        BrainAtlas.BrainAtlasVersion data = new BrainAtlas.BrainAtlasVersion();
        data.setId(IdUtils.getUUID(version.getId()));
        data.setCoordinateSpace(ref(version.getCoordinateSpace()));
        data.setTitle(value(version.getVersionIdentifier()));
        data.setDefinedIn(ref(version.getDefinedIn()));
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
            pe.setDefinition(value(parcellationEntity.getDefinition()));
            pe.setOntologyIdentifiers(simpleLink(parcellationEntity.getOntologyIdentifier()));
            if(parcellationEntity.getVersions()!=null && !parcellationEntity.getVersions().isEmpty()) {
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
