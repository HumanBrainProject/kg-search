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

package eu.ebrains.kg.common.model.target.elasticsearch.instances;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ebrains.kg.common.model.target.elasticsearch.ElasticSearchInfo;
import eu.ebrains.kg.common.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.common.model.target.elasticsearch.MetaInfo;
import eu.ebrains.kg.common.model.target.elasticsearch.TargetInstance;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.*;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.schemaorg.SchemaOrgInstance;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@MetaInfo(name="WebService", searchable=false) //Currently disable the searchability of web services to keep them "hidden"
@Getter
@Setter
public class WebServiceVersion implements TargetInstance, VersionedInstance, HasTrendingInformation, HasBadges {
    @JsonIgnore
    private List<String> allIdentifiers;

    @ElasticSearchInfo(type = "keyword")
    private final Value<String> type = new Value<>("WebService");

    @FieldInfo(ignoreForSearch = true, visible = false)
    private String id;

    @ElasticSearchInfo(type = "keyword")
    @FieldInfo(visible = false, ignoreForSearch = true)
    private List<String> identifier;

    @FieldInfo(ignoreForSearch = true, visible = false)
    private SchemaOrgInstance meta;

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

    @FieldInfo(label = "Developers", separator = "; ", layout = "header", type = FieldInfo.Type.TEXT, boost = 10, labelHidden = true, useForSuggestion = true)
    private List<TargetInternalReference> developers;

    @FieldInfo(layout = "How to cite", labelHidden = true, fieldType = FieldInfo.FieldType.CITATION)
    private Value<String> citation;

    @FieldInfo(layout = "How to cite", labelHidden = true, fieldType = FieldInfo.FieldType.CITATION)
    private Value<String> customCitation;

    @FieldInfo(label = "Copyright", type = FieldInfo.Type.TEXT)
    private Value<String> copyright;

    @FieldInfo(label = "Project", boost = 10, order = 3, useForSuggestion = true)
    private List<TargetInternalReference> projects;

    @FieldInfo(label = "Custodians", separator = "; ", hint = "A custodian is the person responsible for the web service.", boost = 10, useForSuggestion = true)
    private List<TargetInternalReference> custodians;

    @FieldInfo(label = "Accessibility", facet = FieldInfo.Facet.LIST)
    private Value<String> accessibility;

    @FieldInfo(label = "Homepage")
    private TargetExternalReference homepage;

    @FieldInfo(label = "Source code")
    private TargetExternalReference sourceCode;

    @FieldInfo(label = "Documentation")
    private List<TargetExternalReference> documentation;

    @FieldInfo(label = "Support")
    private List<TargetExternalReference> support;

    @FieldInfo(label = "Used software")
    private List<TargetInternalReference> components;

    @FieldInfo(labelHidden = true, layout = "Publications", fieldType = FieldInfo.FieldType.MARKDOWN)
    private List<Value<String>> publications;

    @FieldInfo(label = "Input formats", visible = false, facet = FieldInfo.Facet.LIST, isFilterableFacet = true, useForSuggestion = true)
    private List<Value<String>> inputFormatsForFilter;

    @FieldInfo(label = "Output formats", visible = false, facet = FieldInfo.Facet.LIST, isFilterableFacet = true, useForSuggestion = true)
    private List<Value<String>> outputFormatsForFilter;

    @FieldInfo(label = "Input formats", layout = "Input formats", fieldType = FieldInfo.FieldType.TABLE, labelHidden = true)
    private List<Children<FileFormat>> inputFormat;

    @FieldInfo(label = "Output formats", layout = "Output formats", fieldType = FieldInfo.FieldType.TABLE, labelHidden = true)
    private List<Children<FileFormat>> outputFormats;



    @FieldInfo(labelHidden = true, fieldType = FieldInfo.FieldType.MARKDOWN, boost = 2, overview = true, useForSuggestion = true)
    private Value<String> description;

    @FieldInfo(label = "Version specification", fieldType = FieldInfo.FieldType.MARKDOWN, boost = 2, useForSuggestion = true)
    private Value<String> newInThisVersion;

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
