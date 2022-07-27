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
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ebrains.kg.common.model.target.elasticsearch.ElasticSearchInfo;
import eu.ebrains.kg.common.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.common.model.target.elasticsearch.MetaInfo;
import eu.ebrains.kg.common.model.target.elasticsearch.TargetInstance;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@MetaInfo(name="Software", order=7, searchable=true)
@Getter
@Setter
public class SoftwareVersion implements TargetInstance, VersionedInstance, HasCitation, HasTrendingInformation, HasBadges {
    @JsonIgnore
    private List<String> allIdentifiers;

    @ElasticSearchInfo(type = "keyword")
    private final Value<String> type = new Value<>("Software");

    @FieldInfo(ignoreForSearch = true, visible = false)
    private String id;

    @ElasticSearchInfo(type = "keyword")
    @FieldInfo(visible = false, ignoreForSearch = true)
    private List<String> identifier;

    @ElasticSearchInfo(type = "keyword")
    private Value<String> category;

    @ElasticSearchInfo(type = "keyword")
    private Value<String> disclaimer;

    @ElasticSearchInfo(type = "keyword")
    private List<String> badges;

    private boolean trending;

    private int last30DaysViews;

    @FieldInfo(label = "Name", boost = 20, useForSuggestion = true)
    private Value<String> title;

    /**
     * @deprecated  This is not needed for the new KG anymore since the id is consistent across search/editor
     */
    @FieldInfo(layout = "header")
    @Deprecated(forRemoval = true)
    private Value<String> editorId;

    @FieldInfo(label = "Developers", separator = "; ", layout = "header", type = FieldInfo.Type.TEXT, boost = 10, labelHidden = true, useForSuggestion = true)
    private List<TargetInternalReference> developers;

    @FieldInfo(layout = "How to cite", labelHidden = true, isCitation=true)
    private Value<String> citation;

    @FieldInfo(layout = "How to cite", labelHidden = true, isCitation=true)
    private Value<String> customCitation;

    //Overview
    @FieldInfo(label = "DOI")
    private Value<String> doi;

    /**
     * @deprecated
     */
    @Deprecated(forRemoval = true)
    @FieldInfo(label = "License", type = FieldInfo.Type.TEXT, facetOrder = FieldInfo.FacetOrder.BYVALUE, facet = FieldInfo.Facet.LIST)
    private List<Value<String>> licenseOld;

    @FieldInfo(label = "License", type = FieldInfo.Type.TEXT)
    private List<TargetExternalReference> license;


    @FieldInfo(label = "Copyright", type = FieldInfo.Type.TEXT)
    private Value<String> copyright;

    @FieldInfo(label = "Project", boost = 10, order = 3, useForSuggestion = true)
    private List<TargetInternalReference> projects;

    @FieldInfo(label = "Custodians", separator = "; ", hint = "A custodian is the person responsible for the data bundle.", boost = 10, useForSuggestion = true)
    private List<TargetInternalReference> custodians;

    /**
     * @deprecated use homepage for openMINDS instead
     */
    @Deprecated(forRemoval = true)
    @FieldInfo(label = "Homepage")
    private List<TargetExternalReference> homepageOld;

    @FieldInfo(label = "Homepage")
    private TargetExternalReference homepage;

    /**
     * @deprecated use sourceCode for openMINDS instead
     */
    @Deprecated(forRemoval = true)
    @FieldInfo(label = "Source code")
    private List<TargetExternalReference> sourceCodeOld;

    @FieldInfo(label = "Source code")
    private TargetExternalReference sourceCode;

    @FieldInfo(label = "Documentation")
    private List<TargetExternalReference> documentation;

    @FieldInfo(label = "Support")
    private List<TargetExternalReference> support;



    @FieldInfo(labelHidden = true, layout = "Publications", markdown = true)
    private List<Value<String>> publications;

    /**
     * @deprecated This is no longer in use for openMINDS
     */
    @Deprecated(forRemoval = true)
    @FieldInfo(label = "Latest Version", layout = "summary")
    private Value<String> versionOld;

    /**
     * @deprecated features are now references to controlled terms
     */
    @Deprecated(forRemoval = true)
    @FieldInfo(label = "Features", layout = "summary", tagIcon = "<svg width=\"50\" height=\"50\" viewBox=\"0 0 1792 1792\" xmlns=\"http://www.w3.org/2000/svg\"><path d=\"M576 448q0-53-37.5-90.5t-90.5-37.5-90.5 37.5-37.5 90.5 37.5 90.5 90.5 37.5 90.5-37.5 37.5-90.5zm1067 576q0 53-37 90l-491 492q-39 37-91 37-53 0-90-37l-715-716q-38-37-64.5-101t-26.5-117v-416q0-52 38-90t90-38h416q53 0 117 26.5t102 64.5l715 714q37 39 37 91z\"/></svg>")
    private List<Value<String>> featuresOld;

    @FieldInfo(label = "Features", layout = "summary", facet = FieldInfo.Facet.LIST, isFilterableFacet = true, useForSuggestion = true)
    private List<TargetInternalReference> features;

    @FieldInfo(label = "Input formats", visible = false, facet = FieldInfo.Facet.LIST, isFilterableFacet = true, useForSuggestion = true)
    private List<Value<String>> inputFormatsForFilter;

    @FieldInfo(label = "Output formats", visible = false, facet = FieldInfo.Facet.LIST, isFilterableFacet = true, useForSuggestion = true)
    private List<Value<String>> outputFormatsForFilter;

    @FieldInfo(label = "Input formats", layout = "Input formats", isTable = true, labelHidden = true)
    private List<Children<FileFormat>> inputFormat;

    @FieldInfo(label = "Output formats", layout = "Output formats", isTable = true, labelHidden = true)
    private List<Children<FileFormat>> outputFormats;

    /**
     * @deprecated use appCategory for openMINDS instead
     */
    @Deprecated(forRemoval = true)
    @FieldInfo(label = "Application Category", layout = "summary", separator = ", ", facet = FieldInfo.Facet.LIST)
    private List<Value<String>> appCategoryOld;

    @FieldInfo(label = "Application Category", layout = "summary", separator = ", ", facet = FieldInfo.Facet.LIST, useForSuggestion = true)
    private List<TargetInternalReference> appCategory;


    /**
     * @deprecated use operatingSystem for openMINDS instead
     */
    @Deprecated(forRemoval = true)
    @FieldInfo(label = "Operating System", layout = "summary", facet = FieldInfo.Facet.LIST, tagIcon = "<svg width=\"50\" height=\"50\" viewBox=\"0 0 11.377083 13.05244\" xmlns=\"http://www.w3.org/2000/svg\"><path d=\"M 5.6585847,-3.1036376e-7 2.8334327,1.5730297 0.0088,3.1455497 0.0047,6.4719597 0,9.7983697 2.8323857,11.42515 l 2.831867,1.62729 1.070218,-0.60358 c 0.588756,-0.33201 1.874409,-1.06813 2.856675,-1.63608 L 11.377083,9.7797697 v -3.24735 -3.24786 l -0.992187,-0.62477 C 9.8391917,2.3160397 8.5525477,1.5769697 7.5256387,1.0175097 Z M 5.6580697,3.7398297 a 2.7061041,2.7144562 0 0 1 2.706293,2.71456 2.7061041,2.7144562 0 0 1 -2.706293,2.71456 2.7061041,2.7144562 0 0 1 -2.70578,-2.71456 2.7061041,2.7144562 0 0 1 2.70578,-2.71456 z\"/></svg>")
    private List<Value<String>> operatingSystemOld;

    @FieldInfo(label = "Operating System", layout = "summary", facet = FieldInfo.Facet.LIST, useForSuggestion = true)
    private List<TargetInternalReference> operatingSystem;

    @FieldInfo(label = "Devices", layout = "summary", facet = FieldInfo.Facet.LIST, useForSuggestion = true)
    private List<TargetInternalReference> devices;

    @FieldInfo(label = "License", type = FieldInfo.Type.TEXT, visible = false, facetOrder = FieldInfo.FacetOrder.BYCOUNT, facet = FieldInfo.Facet.LIST)
    private List<Value<String>> licenseForFilter;


    @FieldInfo(label = "Programming languages", layout = "summary", facet = FieldInfo.Facet.LIST, useForSuggestion = true)
    private List<TargetInternalReference> programmingLanguages;

    @FieldInfo(label = "Requirements", useForSuggestion = true)
    private List<Value<String>> requirements;


    @FieldInfo(label = "Languages", layout = "summary", useForSuggestion = true)
    private List<TargetInternalReference> languages;

    /**
     * @deprecated keywords are - although existing in openMINDS - not very suitable for Software. Additionally, they would be TargetInternalReferences if there would be any. Therefore, this field is used for the old structure only.
     */
    @Deprecated(forRemoval = true)
    @FieldInfo(label = "Keywords", facet = FieldInfo.Facet.LIST, order = 1, overviewMaxDisplay = 3, layout = "summary", overview = true, isFilterableFacet = true, tagIcon = "<svg width=\"50\" height=\"50\" viewBox=\"0 0 1792 1792\" xmlns=\"http://www.w3.org/2000/svg\"><path d=\"M576 448q0-53-37.5-90.5t-90.5-37.5-90.5 37.5-37.5 90.5 37.5 90.5 90.5 37.5 90.5-37.5 37.5-90.5zm1067 576q0 53-37 90l-491 492q-39 37-91 37-53 0-90-37l-715-716q-38-37-64.5-101t-26.5-117v-416q0-52 38-90t90-38h416q53 0 117 26.5t102 64.5l715 714q37 39 37 91z\"/></svg>", useForSuggestion = true)
    private List<Value<String>> keywords;

    @FieldInfo(label = "Sub-components", layout = "Sub-components", labelHidden = true)
    private List<TargetInternalReference> components;

    @FieldInfo(labelHidden = true, markdown = true, boost = 2, overview = true, useForSuggestion = true)
    private Value<String> description;

    @JsonProperty("first_release")
    @FieldInfo(label = "First release", ignoreForSearch = true, visible = false, type=FieldInfo.Type.DATE)
    private ISODateValue firstRelease;

    @JsonProperty("last_release")
    @FieldInfo(label = "Last release", ignoreForSearch = true, visible = false, type=FieldInfo.Type.DATE)
    private ISODateValue lastRelease;

    private String version;

    private List<TargetInternalReference> versions;

    public TargetInternalReference getAllVersionRef() {
        return null;
    }

    @JsonIgnore
    private boolean isSearchable;

    @Override
    public boolean isSearchableInstance() {
        return isSearchable;
    }

    public void setSearchable(boolean searchable) {
        isSearchable = searchable;
    }

    @Getter
    @Setter
    public static class FileFormat {
        @FieldInfo(label = "Name")
        private TargetInternalReference name;

        @FieldInfo(label = "File extensions")
        private List<Value<String>> fileExtensions;

        @FieldInfo(label = "Media type")
        private Value<String> relatedMediaType;
    }

}
