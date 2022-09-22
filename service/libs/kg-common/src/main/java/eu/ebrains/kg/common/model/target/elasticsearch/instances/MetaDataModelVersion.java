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
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.ISODateValue;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.TargetExternalReference;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.Value;
import eu.ebrains.kg.common.model.target.elasticsearch.ElasticSearchInfo;
import eu.ebrains.kg.common.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.common.model.target.elasticsearch.MetaInfo;
import eu.ebrains.kg.common.model.target.elasticsearch.TargetInstance;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@MetaInfo(name = "(Meta)Data Model", order = 6, searchable=true)
public class MetaDataModelVersion implements TargetInstance, VersionedInstance, HasCitation, HasBadges, HasTrendingInformation{

    @JsonIgnore
    private List<String> allIdentifiers;

    @ElasticSearchInfo(type = "keyword")
    private Value<String> type = new Value<>("(Meta)Data Model");

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

    private boolean trending = false;

    private int last30DaysViews;

    @JsonProperty("first_release")
    @FieldInfo(label = "First release", ignoreForSearch = true, visible = false, type = FieldInfo.Type.DATE)
    private ISODateValue firstRelease;

    @JsonProperty("last_release")
    @FieldInfo(label = "Last release", ignoreForSearch = true, visible = false, type = FieldInfo.Type.DATE)
    private ISODateValue lastRelease;

    private String version;

    @FieldInfo(ignoreForSearch = true, visible = false)
    private TargetInternalReference allVersionRef;

    private List<TargetInternalReference> versions;

    @JsonIgnore
    private boolean isSearchable;

    @Override
    public boolean isSearchableInstance() {
        return isSearchable;
    }


    //Global
    @FieldInfo(label = "Name", layout = "header", boost = 20, useForSuggestion = true)
    private Value<String> title;

    @FieldInfo(label = "Contributors", layout = "header", separator = "; ", type = FieldInfo.Type.TEXT, labelHidden = true, boost = 10, useForSuggestion = true)
    private List<TargetInternalReference> contributors;

    //Overview
    @FieldInfo(label = "DOI", hint = "This is the model DOI you must cite if you reuse this model in a way that leads to a publication")
    private Value<String> doi;

    @JsonProperty("license_info")
    @FieldInfo(label = "License", type = FieldInfo.Type.TEXT, facetOrder = FieldInfo.FacetOrder.BYVALUE)
    private List<TargetExternalReference> licenseInfo;

    @FieldInfo(label = "Project", boost = 10, order = 3, useForSuggestion = true)
    private List<TargetInternalReference> projects;

    @FieldInfo(label = "Custodians", layout = "summary", separator = "; ", type = FieldInfo.Type.TEXT, useForSuggestion = true, hint = "A custodian is the person responsible for the data bundle.")
    private List<TargetInternalReference> custodians;

    @FieldInfo(label = "Homepage")
    private TargetExternalReference homepage;

    @FieldInfo(label = "Description", markdown = true, boost = 2, labelHidden = true, overview = true, useForSuggestion = true)
    private Value<String> description;

    @FieldInfo(label = "New in this version", markdown = true, boost = 2)
    private Value<String> newInThisVersion;

    @FieldInfo(label = "Accessibility", visible = false, facet = FieldInfo.Facet.LIST)
    private Value<String> accessibility;

    @FieldInfo(layout = "How to cite", labelHidden = true, isCitation=true)
    private Value<String> citation;

    @FieldInfo(layout = "How to cite", labelHidden = true, isCitation=true)
    private Value<String> customCitation;

    @FieldInfo(layout = "Get (meta)data model", isFilePreview=true)
    private TargetExternalReference embeddedModelSource;

    @FieldInfo(layout = "Get (meta)data model")
    private TargetExternalReference externalDownload;

    @FieldInfo(layout = "Get (meta)data model", termsOfUse = true)
    private TargetExternalReference internalDownload;

    /**
     * Use fileRepositoryId instead, kept until next index (incremental) update
     */
    @Deprecated(forRemoval = true)
    @FieldInfo(layout = "Get (meta)data model", isHierarchicalFiles = true, isAsync=true, labelHidden = true)
    private String filesAsyncUrl;

    @FieldInfo(layout = "Get (meta)data model", isHierarchicalFiles = true, isAsync=true, labelHidden = true)
    private String fileRepositoryId;

    @FieldInfo(layout = "Get (meta)data model")
    private Value<String> embargo;

    //Publications
    @FieldInfo(layout = "Publications", markdown = true, labelHidden = true)
    private List<Value<String>> publications;

    @FieldInfo(label = "Type", layout = "summary", facet = FieldInfo.Facet.LIST)
    private TargetInternalReference metaDataModelType;

    @FieldInfo(label = "Keywords", facet = FieldInfo.Facet.LIST, order = 1, overviewMaxDisplay = 3, layout = "summary", overview = true, isFilterableFacet = true, tagIcon = "<svg width=\"50\" height=\"50\" viewBox=\"0 0 1792 1792\" xmlns=\"http://www.w3.org/2000/svg\"><path d=\"M576 448q0-53-37.5-90.5t-90.5-37.5-90.5 37.5-37.5 90.5 37.5 90.5 90.5 37.5 90.5-37.5 37.5-90.5zm1067 576q0 53-37 90l-491 492q-39 37-91 37-53 0-90-37l-715-716q-38-37-64.5-101t-26.5-117v-416q0-52 38-90t90-38h416q53 0 117 26.5t102 64.5l715 714q37 39 37 91z\"/></svg>")
    private List<Value<String>> keywords;

    @FieldInfo(label = "Support", layout = "summary", separator = "; ", type = FieldInfo.Type.TEXT)
    private List<TargetExternalReference> support;

    @FieldInfo(label = "Serialization format", layout = "summary")
    private List<TargetInternalReference> serializationFormat;

    @FieldInfo(label = "Specification format", layout = "summary")
    private List<TargetInternalReference> specificationFormat;

}
