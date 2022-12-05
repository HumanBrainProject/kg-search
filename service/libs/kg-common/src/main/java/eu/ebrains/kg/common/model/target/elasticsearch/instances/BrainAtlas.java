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

package eu.ebrains.kg.common.model.target.elasticsearch.instances;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.ebrains.kg.common.model.target.elasticsearch.ElasticSearchInfo;
import eu.ebrains.kg.common.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.common.model.target.elasticsearch.MetaInfo;
import eu.ebrains.kg.common.model.target.elasticsearch.TargetInstance;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;


@Getter
@Setter
@MetaInfo(name = "BrainAtlas")
public class BrainAtlas implements TargetInstance, HasCitation{
    @JsonIgnore
    private List<String> allIdentifiers;

    @ElasticSearchInfo(type = "keyword")
    private final Value<String> type = new Value<>("BrainAtlas");

    @FieldInfo(ignoreForSearch = true, visible = false)
    private String id;

    @ElasticSearchInfo(type = "keyword")
    @FieldInfo(ignoreForSearch = true, visible = false)
    private List<String> identifier;

    @ElasticSearchInfo(type = "keyword")
    private Value<String> category;

    @ElasticSearchInfo(type = "keyword")
    private Value<String> disclaimer;

    @FieldInfo(layout = "header")
    private Value<String> editorId;

    @FieldInfo(label = "Name", layout = "header", boost = 20, useForSuggestion = true)
    private Value<String> title;

    @FieldInfo(label = "Contributors", separator = "; ", layout = "header", type = FieldInfo.Type.TEXT, boost = 10, labelHidden = true, useForSuggestion = true)
    private List<TargetInternalReference> contributors;

    @FieldInfo(label = "Description", labelHidden = true, markdown = true, boost = 2, useForSuggestion = true, overview = true)
    private Value<String> description;

    //Overview
    @FieldInfo(label = "DOI", hint = "This is the brain atlas DOI you must cite if you reuse this data in a way that leads to a publication", componentType = FieldInfo.ComponentType.isSingleWord)
    private Value<String> doi;

    @FieldInfo(layout = "How to cite", labelHidden = true, componentType = FieldInfo.ComponentType.isCitation)
    private Value<String> citation;

    @FieldInfo(layout = "How to cite", labelHidden = true, componentType = FieldInfo.ComponentType.isCitation)
    private Value<String> customCitation;

    @FieldInfo(label = "Homepage")
    private TargetExternalReference homepage;

    @FieldInfo(labelHidden = true, componentType = FieldInfo.ComponentType.isHierarchical, layout = "Parcellation terminology")
    private BasicHierarchyElement<BrainAtlasOverview> parcellationTerminology;

    @FieldInfo(labelHidden = true, componentType = FieldInfo.ComponentType.isHierarchical, layout = "Versions")
    private BasicHierarchyElement<BrainAtlasOverview> versions;

    @Override
    @JsonIgnore
    public boolean isSearchableInstance() {
        return false;
    }


    @Getter
    @Setter
    @MetaInfo(name="BrainAtlasOverview")
    public static class BrainAtlasOverview {

        @ElasticSearchInfo(type = "keyword")
        private Value<String> type = new Value<>("BrainAtlas.BrainAtlasOverview");

        @FieldInfo
        private Value<String> title;

        @FieldInfo(label = "Parcellation entities")
        private Value<Integer> numberOfParcellationEntities;

        @FieldInfo(label = "Available versions")
        private Value<String> availableVersions;

    }


    @Getter
    @Setter
    @MetaInfo(name="BrainAtlasVersion")
    public static class BrainAtlasVersion {

        @FieldInfo(ignoreForSearch = true, visible = false)
        private String id;

        @ElasticSearchInfo(type = "keyword")
        private Value<String> type = new Value<>("BrainAtlas.BrainAtlasVersion");

        @FieldInfo
        private Value<String> title;

        @FieldInfo(label = "Coordinate space")
        private TargetInternalReference coordinateSpace;

        @FieldInfo(label = "Defined in")
        private List<TargetInternalReference> definedIn;
    }

    @Getter
    @Setter
    @MetaInfo(name="ParcellationEntity")
    public static class ParcellationEntity {

        @FieldInfo(ignoreForSearch = true, visible = false)
        private String id;

        @ElasticSearchInfo(type = "keyword")
        private Value<String> type = new Value<>("BrainAtlas.ParcellationEntity");

        @FieldInfo
        private Value<String> title;

        @FieldInfo(label = "Alternative name")
        private Value<String> alternativeName;

        @FieldInfo(markdown = true)
        private Value<String> definition;

        @FieldInfo(label="Ontology identifiers")
        private List<TargetExternalReference> ontologyIdentifiers;

        @FieldInfo(label = "Available in versions")
        private List<Children<RelevantBrainAtlasVersion>> versionGroups;
    }


    @Getter
    @Setter
    @MetaInfo(name="ParcellationEntityVersion")
    public static class ParcellationEntityVersion {

        @FieldInfo(ignoreForSearch = true, visible = false)
        private String id;

        @ElasticSearchInfo(type = "keyword")
        private Value<String> type = new Value<>("BrainAtlas.ParcellationEntityVersion");

        @FieldInfo
        private Value<String> title;
    }



    @Getter
    @Setter
    @MetaInfo(name="RelevantBrainAtlasVersion")
    public static class RelevantBrainAtlasVersion{
        @FieldInfo(labelHidden = true)
        private Value<String> name;

        @FieldInfo(labelHidden = true)
        List<Children<RelevantBrainAtlasVersionSub>> versionGroups;

    }

    @Getter
    @Setter
    @MetaInfo(name="RelevantBrainAtlasVersionSub")
    public static class RelevantBrainAtlasVersionSub{

        @FieldInfo(labelHidden = true)
        private Value<String> name;

        @FieldInfo(markdown = true, label="Color")
        private Value<String> color;
    }



}
