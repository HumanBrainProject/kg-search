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
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Getter
@Setter
@MetaInfo(name = "Dataset", defaultSelection = true, searchable = true)
public class DatasetVersion implements TargetInstance, VersionedInstance, HasCitation, HasPreviews, HasBadges, HasTrendingInformation {
    @JsonIgnore
    private List<String> allIdentifiers;

    public static final String RESTRICTED_ACCESS_MESSAGE = "This dataset has restricted access. Although the metadata is publicly available, the data remain on an access restricted server.";

    public static String createHDGMessage(String id, String containerUrl) {
        if (containerUrl != null && !containerUrl.startsWith("https://data-proxy.ebrains.eu") && !containerUrl.startsWith("https://object.cscs.ch")) {
            return String.format("<a href=\"%s\" target=\"_blank\">%s</a>", containerUrl, containerUrl);
        }
        return String.format("This data requires you to explicitly <a href=\"https://data-proxy.ebrains.eu/datasets/%s\" target=\"_blank\">request access</a> with your EBRAINS account. If you don't have such an account yet, please <a href=\"https://ebrains.eu/register/\" target=\"_blank\">register</a>.", id);
    }

    //Internal
    @ElasticSearchInfo(type = "keyword")
    private Value<String> type = new Value<>("Dataset");

    @ElasticSearchInfo(type = "keyword")
    private Value<String> category;

    @ElasticSearchInfo(type = "keyword")
    private Value<String> disclaimer;

    @ElasticSearchInfo(type = "keyword")
    private List<String> badges;

    @ElasticSearchInfo(type = "keyword")
    private List<String> tags;

    @ElasticSearchInfo(type = "keyword")
    @FieldInfo(ignoreForSearch = true, visible = false)
    private List<String> specimenIds;

    @FieldInfo(ignoreForSearch = true, visible = false)
    private SchemaOrgInstance meta;

    private boolean trending;

    private int last30DaysViews;


    @FieldInfo(ignoreForSearch = true, visible = false)
    private String id;

    @ElasticSearchInfo(type = "keyword")
    @FieldInfo(ignoreForSearch = true, visible = false)
    private List<String> identifier;

    @JsonProperty("first_release")
    @FieldInfo(ignoreForSearch = true, visible = false, type = FieldInfo.Type.DATE)
    private ISODateValue firstRelease;

    @JsonProperty("last_release")
    @FieldInfo(ignoreForSearch = true, visible = false, type = FieldInfo.Type.DATE)
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

    @FieldInfo(label = "Contributors", separator = "; ", layout = "header", type = FieldInfo.Type.TEXT, boost = 10, labelHidden = true, useForSuggestion = true)
    private List<TargetInternalReference> contributors;

    //Overview
    @FieldInfo(label = "DOI", hint = "This is the dataset DOI you must cite if you reuse this data in a way that leads to a publication", isSingleWord = true)
    private Value<String> doi;

    @FieldInfo(label = "Released", isSingleWord = true, overview = true, ignoreForSearch = true)
    private Value<String> releasedAt;

    //HDG terms of use are going to be a license too
    @JsonProperty("license_info")
    @FieldInfo(label = "License", type = FieldInfo.Type.TEXT, facetOrder = FieldInfo.FacetOrder.BYVALUE)
    private TargetExternalReference licenseInfo;

    @FieldInfo(label = "Ethics assessment")
    private Value<String> ethicsAssessment;

    @FieldInfo(label = "Project", boost = 10, order = 3, useForSuggestion = true)
    private List<TargetInternalReference> projects;

    @FieldInfo(label = "Custodians", separator = "; ", hint = "A custodian is the person responsible for the data bundle.", boost = 10, useForSuggestion = true)
    private List<TargetInternalReference> custodians;

    @FieldInfo(label = "Description", labelHidden = true, fieldType = FieldInfo.FieldType.MARKDOWN, boost = 2, useForSuggestion = true, overview = true)
    private Value<String> description;

    @FieldInfo(label = "Homepage")
    private TargetExternalReference homepage;

    @FieldInfo(label = "Support channel")
    private List<TargetExternalReference> supportChannels;

    @FieldInfo(label = "Version specification", fieldType = FieldInfo.FieldType.MARKDOWN, boost = 2, useForSuggestion = true)
    private Value<String> newInThisVersion;

    @FieldInfo(labelHidden = true, boost = 2, visible = false)
    private List<PreviewObject> previewObjects;

    @FieldInfo(label = "View data", fieldType = FieldInfo.FieldType.GROUPED_LINKS)
    private Map<String, List<TargetExternalReference>> viewData;

    //Start filter only
    @FieldInfo(label = "Data accessibility", visible = false, facet = FieldInfo.Facet.LIST)
    private Value<String> dataAccessibility;

    @FieldInfo(label = "Species", facet = FieldInfo.Facet.LIST, visible = false, type = FieldInfo.Type.TEXT, isFilterableFacet = true)
    private List<Value<String>> speciesFilter;

    @FieldInfo(label = "Experimental approach", type = FieldInfo.Type.TEXT, visible = false, facet = FieldInfo.Facet.LIST, isFilterableFacet = true, useForSuggestion = true)
    private List<Value<String>> experimentalApproachForFilter;
    //End filter only

    @FieldInfo(label = "Studied brain region", layout = "summary", useForSuggestion = true, facet = FieldInfo.Facet.LIST, isFilterableFacet = true)
    private List<TargetInternalReference> studiedBrainRegion;

    @FieldInfo(label = "Study targets", layout = "summary", useForSuggestion = true)
    private List<TargetInternalReference> studyTargets;

    @FieldInfo(label = "Anat. location of tissue samples", layout = "summary", hint = "Please see the section \"Specimens\" for distribution of the anatomical locations", useForSuggestion = true)
    private List<TargetInternalReference> anatomicalLocationOfTissueSamples;

    @FieldInfo(label = "Behavioral protocols", layout = "summary", useForSuggestion = true)
    private List<TargetInternalReference> behavioralProtocols;

    @FieldInfo(label = "Preparation", layout = "summary", useForSuggestion = true)
    private List<TargetInternalReference> preparation;

    @FieldInfo(label = "Experimental approach", layout = "summary", useForSuggestion = true)
    private List<TargetInternalReference> experimentalApproach;

    @FieldInfo(label = "Technique", layout = "summary", useForSuggestion = true)
    private List<TargetInternalReference> technique;

    @FieldInfo(label = "Data-descriptor", fieldType = FieldInfo.FieldType.FILE_PREVIEW, layout = "Data descriptor", labelHidden = true)
    private TargetExternalReference dataDescriptor;

    @FieldInfo(layout = "How to cite", labelHidden = true, fieldType = FieldInfo.FieldType.CITATION)
    private Value<String> citation;

    @FieldInfo(layout = "How to cite", labelHidden = true, fieldType = FieldInfo.FieldType.CITATION)
    private Value<String> customCitation;

    @FieldInfo(layout = "Get data", labelHidden = true)
    private Value<String> embargoRestrictedAccess;

    @FieldInfo(layout = "Get data", labelHidden = true)
    private Value<String> embargo;

    @FieldInfo(layout = "Get data", termsOfUse = true)
    private TargetExternalReference downloadAsZip;

    @FieldInfo(layout = "Get data", fieldType = FieldInfo.FieldType.HIERARCHICAL_FILES_ASYNC, labelHidden = true)
    private String fileRepositoryId;

    @FieldInfo(layout = "Get data", termsOfUse = true)
    private TargetExternalReference dataProxyLink;

    @JsonProperty("external_datalink")
    @FieldInfo(layout = "Get data", label = "Data download")
    private List<TargetExternalReference> externalDatalink;

    @FieldInfo(layout = "View data", labelHidden = true, linkIcon = "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 40 40\">\n    <path opacity=\"0.6\" fill=\"#4D4D4D\"\n          d=\"M13.37 33.67a5 5 0 0 1-1.69-.29 4.68 4.68 0 0 1-1.41-.9 4.86 4.86 0 0 0-.94-.66 10.15 10.15 0 0 0-1.07-.42 6.25 6.25 0 0 1-2.05-1 2.33 2.33 0 0 1-.73-2.54 3 3 0 0 0 0-1.29 2.68 2.68 0 0 0-.86-1.13 4.26 4.26 0 0 1-1-1.19 3.88 3.88 0 0 1-.25-2 6.81 6.81 0 0 0 0-.86A4.44 4.44 0 0 0 3 19.91c-.1-.24-.21-.49-.3-.75-.58-1.69-.05-3.45.59-5.26.56-1.58 1.17-3.05 2.54-3.84l.26-.14a1.79 1.79 0 0 0 .53-.36 1.71 1.71 0 0 0 .27-.64C7.32 7.4 8.11 5.35 10 5.27a4.13 4.13 0 0 1 .46 0 1.69 1.69 0 0 0 .55 0 1.71 1.71 0 0 0 .59-.41l.23-.21c.37-.31 2.27-1.81 3.83-1.62a1.81 1.81 0 0 1 1.28.75 2.46 2.46 0 0 1 .29.52 7.37 7.37 0 0 1 .22 3.54c0 .4-.08.79-.09 1.17L17.23 11c0 1.12-.09 2.23-.12 3.35A102.75 102.75 0 0 0 17.22 25c0 .39.08.83.14 1.28.3 2.45.7 5.81-2.17 7a4.79 4.79 0 0 1-1.82.39zm-3.3-27.52H10c-1 0-1.69 1-2.26 3a2.49 2.49 0 0 1-.45 1 2.49 2.49 0 0 1-.8.57l-.23.12c-1.1.63-1.64 2-2.14 3.37-.59 1.65-1.07 3.26-.59 4.67.08.24.18.46.27.69a5.18 5.18 0 0 1 .5 1.71 7.58 7.58 0 0 1 0 1 3.18 3.18 0 0 0 .16 1.59 3.56 3.56 0 0 0 .8.94 3.36 3.36 0 0 1 1.11 1.55 3.76 3.76 0 0 1 0 1.66c-.12.72-.2 1.19.43 1.72a5.51 5.51 0 0 0 1.77.86 10.82 10.82 0 0 1 1.14.4 5.59 5.59 0 0 1 1.11.77 4 4 0 0 0 1.15.75 4.11 4.11 0 0 0 2.88 0c2.25-.93 1.92-3.67 1.63-6.08-.06-.47-.11-.92-.14-1.32a103.52 103.52 0 0 1-.11-10.83c0-1.12.08-2.23.12-3.35L16.42 9c0-.4.06-.82.1-1.22a6.73 6.73 0 0 0-.16-3.12 1.6 1.6 0 0 0-.18-.34.93.93 0 0 0-.68-.4 5.19 5.19 0 0 0-3.15 1.43l-.21.18a2.41 2.41 0 0 1-.93.6 2.39 2.39 0 0 1-.86 0z\"/>\n    <path opacity=\"0.6\" fill=\"#4D4D4D\"\n          d=\"M12.85 11.92a.44.44 0 0 1-.2 0 3.59 3.59 0 0 1-2.13-2.81 6 6 0 0 1 0-.83v-.61a3.36 3.36 0 0 0-1-1.34.44.44 0 0 1 .48-.77 3.84 3.84 0 0 1 1.38 2v.7a5.25 5.25 0 0 0 0 .71A2.73 2.73 0 0 0 13 11.08a.44.44 0 0 1-.2.84zm-3.33 6.17a4.38 4.38 0 0 1-3.77-2.47c-.11-.21-.21-.43-.31-.65a6.21 6.21 0 0 0-.46-.9c-.21-.32-.55-.61-.82-.54a.44.44 0 1 1-.23-.85 1.69 1.69 0 0 1 1.79.9 7 7 0 0 1 .53 1c.09.2.18.41.28.6a3.28 3.28 0 0 0 3.82 1.86.44.44 0 1 1 .28.84 3.49 3.49 0 0 1-1.11.21zm-5.61 4.28a.44.44 0 0 1-.09-.87 3.47 3.47 0 0 0 .63-.89c.49-.84 1.1-1.88 2.12-2a2.5 2.5 0 0 1 2 .95.44.44 0 0 1-.62.62c-.51-.51-.94-.74-1.3-.69-.58.07-1.07.91-1.47 1.58S4.49 22.26 4 22.36zm-.1-.87zm12.43 10.7a.43.43 0 0 1-.37-.21 4 4 0 0 1-.33-1.48c-.1-1-.22-2.08-1-2.51a9.43 9.43 0 0 0-1-.43 6.51 6.51 0 0 1-1.54-.81 3 3 0 0 1-1.05-2 5.53 5.53 0 0 0-.22-.81.43.43 0 0 1 .8-.3 6.28 6.28 0 0 1 .25.93 2.2 2.2 0 0 0 .74 1.49 5.77 5.77 0 0 0 1.36.69 10.33 10.33 0 0 1 1 .47c1.18.64 1.34 2.1 1.45 3.17a3.83 3.83 0 0 0 .22 1.14.43.43 0 0 1-.37.64zm-8.66-3.79c-.33 0-.66 0-1-.06s-.48 0-.68 0a.44.44 0 0 1 0-.88c.23 0 .49 0 .76.05.76.07 1.62.16 2-.32a.44.44 0 0 1 .68.56 2.19 2.19 0 0 1-1.76.65zm3.54-12.48a1.86 1.86 0 0 1-.66-.12.44.44 0 0 1 .31-.83c.65.25 1.41-.32 1.63-.5s.37-.33.55-.5a5.85 5.85 0 0 1 1.25-1 3.22 3.22 0 0 1 2.36-.26.44.44 0 1 1-.24.85 2.33 2.33 0 0 0-1.71.19 5.11 5.11 0 0 0-1.05.85c-.19.19-.39.37-.6.54a3 3 0 0 1-1.84.78zm2.09-6.8a.47.47 0 0 1-.21-.87 12.15 12.15 0 0 0 2.31-1.71 2.89 2.89 0 0 0 .93-2.1.47.47 0 0 1 .94-.09A3.7 3.7 0 0 1 16 7.2a12.85 12.85 0 0 1-2.49 1.86.47.47 0 0 1-.3.07zm-1.79 12.49h-.26a.44.44 0 1 1 0-.88 3.24 3.24 0 0 0 2.5-.65 3.72 3.72 0 0 0 .51-1c.35-.87.84-2.05 2.28-1.81a.44.44 0 0 1-.15.87c-.67-.11-.91.28-1.31 1.27a4.35 4.35 0 0 1-.67 1.24 3.68 3.68 0 0 1-2.9.96zM8 14.23a.44.44 0 0 1-.34-.23 1.05 1.05 0 0 1-.11-.91 1.29 1.29 0 0 1 .84-.72l.3-.1a1.74 1.74 0 0 0 .5-.2.26.26 0 0 0 .12-.19.41.41 0 0 0-.11-.31.44.44 0 1 1 .66-.57 1.29 1.29 0 0 1 .33 1 1.15 1.15 0 0 1-.48.81 2.46 2.46 0 0 1-.76.33l-.25.08c-.22.08-.31.16-.32.2a.28.28 0 0 0 .05.17.44.44 0 0 1-.38.66zm0 11.72h-.15a.44.44 0 0 1-.26-.57 4.2 4.2 0 0 0 .16-1.28A3.85 3.85 0 0 1 8 22.28a.44.44 0 1 1 .77.43 3.2 3.2 0 0 0-.22 1.39 4.89 4.89 0 0 1-.21 1.56.44.44 0 0 1-.34.29z\"/>\n    <path opacity=\"0.6\" fill=\"#4D4D4D\"\n          d=\"M20.51 33.67a4.79 4.79 0 0 1-1.82-.35c-2.87-1.19-2.46-4.55-2.17-7 .06-.46.11-.89.14-1.28a102.83 102.83 0 0 0 .11-10.73c0-1.12-.08-2.23-.12-3.35L16.57 9c0-.38-.05-.77-.09-1.17a7.37 7.37 0 0 1 .22-3.54 2.47 2.47 0 0 1 .3-.51A1.81 1.81 0 0 1 18.26 3c1.55-.2 3.46 1.32 3.83 1.62l.23.21a1.7 1.7 0 0 0 .58.41 1.69 1.69 0 0 0 .55 0 3.93 3.93 0 0 1 .46 0c1.85.08 2.64 2.13 3.07 3.66a1.71 1.71 0 0 0 .27.64 1.79 1.79 0 0 0 .53.36l.26.14c1.36.78 2 2.26 2.54 3.84.64 1.81 1.17 3.57.59 5.26-.09.26-.19.5-.3.75a4.43 4.43 0 0 0-.43 1.42 6.75 6.75 0 0 0 0 .86 3.89 3.89 0 0 1-.25 2 4.26 4.26 0 0 1-1 1.19 2.67 2.67 0 0 0-.86 1.13 3 3 0 0 0 0 1.29 2.33 2.33 0 0 1-.73 2.54 6.24 6.24 0 0 1-2.05 1 10.12 10.12 0 0 0-1.07.42 4.86 4.86 0 0 0-.94.66 4.68 4.68 0 0 1-1.41.9 5 5 0 0 1-1.62.37zM17.69 4.3a1.6 1.6 0 0 0-.18.34 6.74 6.74 0 0 0-.16 3.12c0 .4.08.82.1 1.22l.07 1.95c0 1.12.09 2.23.12 3.35a103.53 103.53 0 0 1-.11 10.83c0 .41-.08.85-.14 1.32-.29 2.41-.62 5.14 1.63 6.08a4.11 4.11 0 0 0 2.88 0 4 4 0 0 0 1.1-.71 5.59 5.59 0 0 1 1.16-.8 10.91 10.91 0 0 1 1.17-.46 5.51 5.51 0 0 0 1.77-.86c.63-.53.55-1 .43-1.72a3.77 3.77 0 0 1 0-1.66 3.36 3.36 0 0 1 1.11-1.55 3.56 3.56 0 0 0 .8-.94 3.18 3.18 0 0 0 .16-1.59 7.63 7.63 0 0 1 0-1 5.18 5.18 0 0 1 .5-1.71c.1-.22.19-.45.27-.69.49-1.42 0-3-.59-4.67-.5-1.41-1-2.73-2.14-3.37l-.23-.12a2.49 2.49 0 0 1-.8-.57 2.49 2.49 0 0 1-.45-1c-.57-2-1.29-3-2.26-3h-.36a2.39 2.39 0 0 1-.86 0 2.4 2.4 0 0 1-.93-.6l-.21-.18a5.19 5.19 0 0 0-3.17-1.41.93.93 0 0 0-.68.4z\"/>\n    <path opacity=\"0.6\" fill=\"#4D4D4D\"\n          d=\"M21 11.92a.44.44 0 0 1-.2-.84A2.73 2.73 0 0 0 22.46 9a5.24 5.24 0 0 0 0-.71v-.7a3.84 3.84 0 0 1 1.38-2 .44.44 0 0 1 .52.72 3.36 3.36 0 0 0-1 1.34v.61a6 6 0 0 1 0 .83 3.59 3.59 0 0 1-2.13 2.81.44.44 0 0 1-.23.02zm3.35 6.17a3.49 3.49 0 0 1-1.12-.18.44.44 0 0 1 .28-.84 3.28 3.28 0 0 0 3.82-1.86c.1-.19.19-.4.28-.6a7 7 0 0 1 .53-1 1.69 1.69 0 0 1 1.79-.9.44.44 0 1 1-.23.85c-.27-.07-.61.21-.82.54a6.25 6.25 0 0 0-.46.91c-.1.22-.2.44-.31.65a4.38 4.38 0 0 1-3.76 2.43zM30 22.37h-.09c-.5-.1-.82-.63-1.21-1.31s-.88-1.5-1.47-1.58c-.36 0-.8.19-1.3.69a.44.44 0 0 1-.62-.62 2.5 2.5 0 0 1 2-.95c1 .13 1.62 1.17 2.12 2a3.39 3.39 0 0 0 .63.89.44.44 0 0 1 .34.52.44.44 0 0 1-.4.36zM17.64 32.2a.43.43 0 0 1-.37-.64 3.83 3.83 0 0 0 .22-1.14c.11-1.07.27-2.54 1.45-3.17a10.29 10.29 0 0 1 1-.47 5.78 5.78 0 0 0 1.36-.69 2.2 2.2 0 0 0 .74-1.49 6.27 6.27 0 0 1 .25-.93.43.43 0 0 1 .8.3 5.56 5.56 0 0 0-.22.81 3 3 0 0 1-1.06 2 6.51 6.51 0 0 1-1.56.81 9.45 9.45 0 0 0-1 .43c-.79.42-.9 1.53-1 2.51A4 4 0 0 1 18 32a.43.43 0 0 1-.36.2zm8.65-3.79a2.19 2.19 0 0 1-1.81-.7.44.44 0 0 1 .68-.56c.39.48 1.25.39 2 .32.27 0 .53-.05.76-.05a.44.44 0 0 1 0 .88h-.68c-.24 0-.62.11-.95.11zm-3.54-12.48a3 3 0 0 1-1.85-.76c-.21-.17-.4-.36-.6-.54a5.11 5.11 0 0 0-1.05-.84 2.32 2.32 0 0 0-1.71-.19.44.44 0 1 1-.24-.85 3.21 3.21 0 0 1 2.36.26 5.85 5.85 0 0 1 1.24 1c.18.17.36.34.55.5s1 .74 1.63.5a.44.44 0 1 1 .31.83 1.85 1.85 0 0 1-.64.09zm-2.09-6.8a.47.47 0 0 1-.25-.07 12.85 12.85 0 0 1-2.49-1.86 3.7 3.7 0 0 1-1.19-2.84.47.47 0 0 1 .51-.43.47.47 0 0 1 .43.51 2.89 2.89 0 0 0 .93 2.1 12.15 12.15 0 0 0 2.31 1.71.47.47 0 0 1-.25.87zm1.8 12.49a3.68 3.68 0 0 1-2.91-.95 4.34 4.34 0 0 1-.67-1.24c-.41-1-.64-1.39-1.31-1.27a.44.44 0 1 1-.15-.87c1.44-.24 1.92.94 2.28 1.81a3.7 3.7 0 0 0 .51 1 3.24 3.24 0 0 0 2.5.65.44.44 0 0 1 0 .88zm3.36-7.39a.44.44 0 0 1-.38-.66.28.28 0 0 0 .05-.17s-.09-.12-.32-.2l-.25-.08a2.46 2.46 0 0 1-.76-.33 1.15 1.15 0 0 1-.48-.81A1.29 1.29 0 0 1 24 11a.44.44 0 1 1 .66.59.41.41 0 0 0-.11.31.26.26 0 0 0 .12.19 1.75 1.75 0 0 0 .5.2l.3.1a1.3 1.3 0 0 1 .84.72 1.05 1.05 0 0 1-.11.91.44.44 0 0 1-.38.21zm.09 11.72a.44.44 0 0 1-.41-.29 4.89 4.89 0 0 1-.21-1.56 3.19 3.19 0 0 0-.22-1.39.44.44 0 1 1 .77-.43 3.84 3.84 0 0 1 .33 1.79 4.2 4.2 0 0 0 .16 1.28.44.44 0 0 1-.26.57z\"/>\n    <path d=\"M13.37 33.67a5 5 0 0 1-1.69-.29 4.68 4.68 0 0 1-1.41-.9 4.86 4.86 0 0 0-.94-.66 10.15 10.15 0 0 0-1.07-.42 6.25 6.25 0 0 1-2.05-1 2.33 2.33 0 0 1-.73-2.54 3 3 0 0 0 0-1.29 2.68 2.68 0 0 0-.86-1.13 4.26 4.26 0 0 1-1-1.19 3.88 3.88 0 0 1-.25-2 6.81 6.81 0 0 0 0-.86A4.44 4.44 0 0 0 3 19.91c-.1-.24-.21-.49-.3-.75-.58-1.69-.05-3.45.59-5.26.56-1.58 1.17-3.05 2.54-3.84l.26-.14a1.79 1.79 0 0 0 .53-.36 1.71 1.71 0 0 0 .27-.64C7.32 7.4 8.11 5.35 10 5.27a4.13 4.13 0 0 1 .46 0 1.69 1.69 0 0 0 .55 0 1.71 1.71 0 0 0 .59-.41l.23-.21c.37-.31 2.27-1.81 3.83-1.62a1.81 1.81 0 0 1 1.28.75 2.46 2.46 0 0 1 .29.52 7.37 7.37 0 0 1 .22 3.54c0 .4-.08.79-.09 1.17L17.23 11c0 1.12-.09 2.23-.12 3.35A102.75 102.75 0 0 0 17.22 25c0 .39.08.83.14 1.28.3 2.45.7 5.81-2.17 7a4.79 4.79 0 0 1-1.82.39zm-3.3-27.52H10c-1 0-1.69 1-2.26 3a2.49 2.49 0 0 1-.45 1 2.49 2.49 0 0 1-.8.57l-.23.12c-1.1.63-1.64 2-2.14 3.37-.59 1.65-1.07 3.26-.59 4.67.08.24.18.46.27.69a5.18 5.18 0 0 1 .5 1.71 7.58 7.58 0 0 1 0 1 3.18 3.18 0 0 0 .16 1.59 3.56 3.56 0 0 0 .8.94 3.36 3.36 0 0 1 1.11 1.55 3.76 3.76 0 0 1 0 1.66c-.12.72-.2 1.19.43 1.72a5.51 5.51 0 0 0 1.77.86 10.82 10.82 0 0 1 1.14.4 5.59 5.59 0 0 1 1.11.77 4 4 0 0 0 1.15.75 4.11 4.11 0 0 0 2.88 0c2.25-.93 1.92-3.67 1.63-6.08-.06-.47-.11-.92-.14-1.32a103.52 103.52 0 0 1-.11-10.83c0-1.12.08-2.23.12-3.35L16.42 9c0-.4.06-.82.1-1.22a6.73 6.73 0 0 0-.16-3.12 1.6 1.6 0 0 0-.18-.34.93.93 0 0 0-.68-.4 5.19 5.19 0 0 0-3.15 1.43l-.21.18a2.41 2.41 0 0 1-.93.6 2.39 2.39 0 0 1-.86 0z\"/>\n    <path d=\"M12.85 11.92a.44.44 0 0 1-.2 0 3.59 3.59 0 0 1-2.13-2.81 6 6 0 0 1 0-.83v-.61a3.36 3.36 0 0 0-1-1.34.44.44 0 0 1 .48-.77 3.84 3.84 0 0 1 1.38 2v.7a5.25 5.25 0 0 0 0 .71A2.73 2.73 0 0 0 13 11.08a.44.44 0 0 1-.2.84zm-3.33 6.17a4.38 4.38 0 0 1-3.77-2.47c-.11-.21-.21-.43-.31-.65a6.21 6.21 0 0 0-.46-.9c-.21-.32-.55-.61-.82-.54a.44.44 0 1 1-.23-.85 1.69 1.69 0 0 1 1.79.9 7 7 0 0 1 .53 1c.09.2.18.41.28.6a3.28 3.28 0 0 0 3.82 1.86.44.44 0 1 1 .28.84 3.49 3.49 0 0 1-1.11.21zm-5.61 4.28a.44.44 0 0 1-.09-.87 3.47 3.47 0 0 0 .63-.89c.49-.84 1.1-1.88 2.12-2a2.5 2.5 0 0 1 2 .95.44.44 0 0 1-.62.62c-.51-.51-.94-.74-1.3-.69-.58.07-1.07.91-1.47 1.58S4.49 22.26 4 22.36zm-.1-.87zm12.43 10.7a.43.43 0 0 1-.37-.21 4 4 0 0 1-.33-1.48c-.1-1-.22-2.08-1-2.51a9.43 9.43 0 0 0-1-.43 6.51 6.51 0 0 1-1.54-.81 3 3 0 0 1-1.05-2 5.53 5.53 0 0 0-.22-.81.43.43 0 0 1 .8-.3 6.28 6.28 0 0 1 .25.93 2.2 2.2 0 0 0 .74 1.49 5.77 5.77 0 0 0 1.36.69 10.33 10.33 0 0 1 1 .47c1.18.64 1.34 2.1 1.45 3.17a3.83 3.83 0 0 0 .22 1.14.43.43 0 0 1-.37.64zm-8.66-3.79c-.33 0-.66 0-1-.06s-.48 0-.68 0a.44.44 0 0 1 0-.88c.23 0 .49 0 .76.05.76.07 1.62.16 2-.32a.44.44 0 0 1 .68.56 2.19 2.19 0 0 1-1.76.65zm3.54-12.48a1.86 1.86 0 0 1-.66-.12.44.44 0 0 1 .31-.83c.65.25 1.41-.32 1.63-.5s.37-.33.55-.5a5.85 5.85 0 0 1 1.25-1 3.22 3.22 0 0 1 2.36-.26.44.44 0 1 1-.24.85 2.33 2.33 0 0 0-1.71.19 5.11 5.11 0 0 0-1.05.85c-.19.19-.39.37-.6.54a3 3 0 0 1-1.84.78zm2.09-6.8a.47.47 0 0 1-.21-.87 12.15 12.15 0 0 0 2.31-1.71 2.89 2.89 0 0 0 .93-2.1.47.47 0 0 1 .94-.09A3.7 3.7 0 0 1 16 7.2a12.85 12.85 0 0 1-2.49 1.86.47.47 0 0 1-.3.07zm-1.79 12.49h-.26a.44.44 0 1 1 0-.88 3.24 3.24 0 0 0 2.5-.65 3.72 3.72 0 0 0 .51-1c.35-.87.84-2.05 2.28-1.81a.44.44 0 0 1-.15.87c-.67-.11-.91.28-1.31 1.27a4.35 4.35 0 0 1-.67 1.24 3.68 3.68 0 0 1-2.9.96zM8 14.23a.44.44 0 0 1-.34-.23 1.05 1.05 0 0 1-.11-.91 1.29 1.29 0 0 1 .84-.72l.3-.1a1.74 1.74 0 0 0 .5-.2.26.26 0 0 0 .12-.19.41.41 0 0 0-.11-.31.44.44 0 1 1 .66-.57 1.29 1.29 0 0 1 .33 1 1.15 1.15 0 0 1-.48.81 2.46 2.46 0 0 1-.76.33l-.25.08c-.22.08-.31.16-.32.2a.28.28 0 0 0 .05.17.44.44 0 0 1-.38.66zm0 11.72h-.15a.44.44 0 0 1-.26-.57 4.2 4.2 0 0 0 .16-1.28A3.85 3.85 0 0 1 8 22.28a.44.44 0 1 1 .77.43 3.2 3.2 0 0 0-.22 1.39 4.89 4.89 0 0 1-.21 1.56.44.44 0 0 1-.34.29z\"/>\n    <path d=\"M20.51 33.67a4.79 4.79 0 0 1-1.82-.35c-2.87-1.19-2.46-4.55-2.17-7 .06-.46.11-.89.14-1.28a102.83 102.83 0 0 0 .11-10.73c0-1.12-.08-2.23-.12-3.35L16.57 9c0-.38-.05-.77-.09-1.17a7.37 7.37 0 0 1 .22-3.54 2.47 2.47 0 0 1 .3-.51A1.81 1.81 0 0 1 18.26 3c1.55-.2 3.46 1.32 3.83 1.62l.23.21a1.7 1.7 0 0 0 .58.41 1.69 1.69 0 0 0 .55 0 3.93 3.93 0 0 1 .46 0c1.85.08 2.64 2.13 3.07 3.66a1.71 1.71 0 0 0 .27.64 1.79 1.79 0 0 0 .53.36l.26.14c1.36.78 2 2.26 2.54 3.84.64 1.81 1.17 3.57.59 5.26-.09.26-.19.5-.3.75a4.43 4.43 0 0 0-.43 1.42 6.75 6.75 0 0 0 0 .86 3.89 3.89 0 0 1-.25 2 4.26 4.26 0 0 1-1 1.19 2.67 2.67 0 0 0-.86 1.13 3 3 0 0 0 0 1.29 2.33 2.33 0 0 1-.73 2.54 6.24 6.24 0 0 1-2.05 1 10.12 10.12 0 0 0-1.07.42 4.86 4.86 0 0 0-.94.66 4.68 4.68 0 0 1-1.41.9 5 5 0 0 1-1.62.37zM17.69 4.3a1.6 1.6 0 0 0-.18.34 6.74 6.74 0 0 0-.16 3.12c0 .4.08.82.1 1.22l.07 1.95c0 1.12.09 2.23.12 3.35a103.53 103.53 0 0 1-.11 10.83c0 .41-.08.85-.14 1.32-.29 2.41-.62 5.14 1.63 6.08a4.11 4.11 0 0 0 2.88 0 4 4 0 0 0 1.1-.71 5.59 5.59 0 0 1 1.16-.8 10.91 10.91 0 0 1 1.17-.46 5.51 5.51 0 0 0 1.77-.86c.63-.53.55-1 .43-1.72a3.77 3.77 0 0 1 0-1.66 3.36 3.36 0 0 1 1.11-1.55 3.56 3.56 0 0 0 .8-.94 3.18 3.18 0 0 0 .16-1.59 7.63 7.63 0 0 1 0-1 5.18 5.18 0 0 1 .5-1.71c.1-.22.19-.45.27-.69.49-1.42 0-3-.59-4.67-.5-1.41-1-2.73-2.14-3.37l-.23-.12a2.49 2.49 0 0 1-.8-.57 2.49 2.49 0 0 1-.45-1c-.57-2-1.29-3-2.26-3h-.36a2.39 2.39 0 0 1-.86 0 2.4 2.4 0 0 1-.93-.6l-.21-.18a5.19 5.19 0 0 0-3.17-1.41.93.93 0 0 0-.68.4z\"/>\n    <path d=\"M21 11.92a.44.44 0 0 1-.2-.84A2.73 2.73 0 0 0 22.46 9a5.24 5.24 0 0 0 0-.71v-.7a3.84 3.84 0 0 1 1.38-2 .44.44 0 0 1 .52.72 3.36 3.36 0 0 0-1 1.34v.61a6 6 0 0 1 0 .83 3.59 3.59 0 0 1-2.13 2.81.44.44 0 0 1-.23.02zm3.35 6.17a3.49 3.49 0 0 1-1.12-.18.44.44 0 0 1 .28-.84 3.28 3.28 0 0 0 3.82-1.86c.1-.19.19-.4.28-.6a7 7 0 0 1 .53-1 1.69 1.69 0 0 1 1.79-.9.44.44 0 1 1-.23.85c-.27-.07-.61.21-.82.54a6.25 6.25 0 0 0-.46.91c-.1.22-.2.44-.31.65a4.38 4.38 0 0 1-3.76 2.43zM30 22.37h-.09c-.5-.1-.82-.63-1.21-1.31s-.88-1.5-1.47-1.58c-.36 0-.8.19-1.3.69a.44.44 0 0 1-.62-.62 2.5 2.5 0 0 1 2-.95c1 .13 1.62 1.17 2.12 2a3.39 3.39 0 0 0 .63.89.44.44 0 0 1 .34.52.44.44 0 0 1-.4.36zM17.64 32.2a.43.43 0 0 1-.37-.64 3.83 3.83 0 0 0 .22-1.14c.11-1.07.27-2.54 1.45-3.17a10.29 10.29 0 0 1 1-.47 5.78 5.78 0 0 0 1.36-.69 2.2 2.2 0 0 0 .74-1.49 6.27 6.27 0 0 1 .25-.93.43.43 0 0 1 .8.3 5.56 5.56 0 0 0-.22.81 3 3 0 0 1-1.06 2 6.51 6.51 0 0 1-1.56.81 9.45 9.45 0 0 0-1 .43c-.79.42-.9 1.53-1 2.51A4 4 0 0 1 18 32a.43.43 0 0 1-.36.2zm8.65-3.79a2.19 2.19 0 0 1-1.81-.7.44.44 0 0 1 .68-.56c.39.48 1.25.39 2 .32.27 0 .53-.05.76-.05a.44.44 0 0 1 0 .88h-.68c-.24 0-.62.11-.95.11zm-3.54-12.48a3 3 0 0 1-1.85-.76c-.21-.17-.4-.36-.6-.54a5.11 5.11 0 0 0-1.05-.84 2.32 2.32 0 0 0-1.71-.19.44.44 0 1 1-.24-.85 3.21 3.21 0 0 1 2.36.26 5.85 5.85 0 0 1 1.24 1c.18.17.36.34.55.5s1 .74 1.63.5a.44.44 0 1 1 .31.83 1.85 1.85 0 0 1-.64.09zm-2.09-6.8a.47.47 0 0 1-.25-.07 12.85 12.85 0 0 1-2.49-1.86 3.7 3.7 0 0 1-1.19-2.84.47.47 0 0 1 .51-.43.47.47 0 0 1 .43.51 2.89 2.89 0 0 0 .93 2.1 12.15 12.15 0 0 0 2.31 1.71.47.47 0 0 1-.25.87zm1.8 12.49a3.68 3.68 0 0 1-2.91-.95 4.34 4.34 0 0 1-.67-1.24c-.41-1-.64-1.39-1.31-1.27a.44.44 0 1 1-.15-.87c1.44-.24 1.92.94 2.28 1.81a3.7 3.7 0 0 0 .51 1 3.24 3.24 0 0 0 2.5.65.44.44 0 0 1 0 .88zm3.36-7.39a.44.44 0 0 1-.38-.66.28.28 0 0 0 .05-.17s-.09-.12-.32-.2l-.25-.08a2.46 2.46 0 0 1-.76-.33 1.15 1.15 0 0 1-.48-.81A1.29 1.29 0 0 1 24 11a.44.44 0 1 1 .66.59.41.41 0 0 0-.11.31.26.26 0 0 0 .12.19 1.75 1.75 0 0 0 .5.2l.3.1a1.3 1.3 0 0 1 .84.72 1.05 1.05 0 0 1-.11.91.44.44 0 0 1-.38.21zm.09 11.72a.44.44 0 0 1-.41-.29 4.89 4.89 0 0 1-.21-1.56 3.19 3.19 0 0 0-.22-1.39.44.44 0 1 1 .77-.43 3.84 3.84 0 0 1 .33 1.79 4.2 4.2 0 0 0 .16 1.28.44.44 0 0 1-.26.57z\"/>\n    <path  d=\"M36.68 34.83l-5.63-5.19a7.19 7.19 0 1 0-1.17 1.59l5.47 5a1 1 0 0 0 1.33-1.44zm-11.92-3a5.56 5.56 0 1 1 5.56-5.56 5.56 5.56 0 0 1-5.56 5.55z\"/>\n</svg>")
    private List<TargetExternalReference> viewer;

    //Publications
    @FieldInfo(layout = "Publications", fieldType = FieldInfo.FieldType.MARKDOWN, labelHidden = true, useForSuggestion = true)
    private List<Value<String>> publications;

    @FieldInfo(label = "Techniques", visible = false, facet = FieldInfo.Facet.LIST, isFilterableFacet = true, type = FieldInfo.Type.TEXT)
    private List<Value<String>> techniquesForFilter;


    @FieldInfo(label = "Keywords", useForSuggestion = true, facet = FieldInfo.Facet.LIST, order = 1, layout = "summary", isFilterableFacet = true, tagIcon = "<svg width=\"50\" height=\"50\" viewBox=\"0 0 1792 1792\" xmlns=\"http://www.w3.org/2000/svg\"><path d=\"M576 448q0-53-37.5-90.5t-90.5-37.5-90.5 37.5-37.5 90.5 37.5 90.5 90.5 37.5 90.5-37.5 37.5-90.5zm1067 576q0 53-37 90l-491 492q-39 37-91 37-53 0-90-37l-715-716q-38-37-64.5-101t-26.5-117v-416q0-52 38-90t90-38h416q53 0 117 26.5t102 64.5l715 714q37 39 37 91z\"/></svg>")
    private List<Value<String>> keywords;

    @FieldInfo(layout = "Specimens", labelHidden = true, fieldType = FieldInfo.FieldType.HIERARCHICAL)
    private BasicHierarchyElement<?> specimenBySubject;

    @FieldInfo(label = "Content types", facet = FieldInfo.Facet.LIST, visible = false, isFilterableFacet = true, type = FieldInfo.Type.TEXT)
    private List<Value<String>> contentTypes;

    @FieldInfo(label = "Used resources", layout = "Related resources")
    private List<TargetInternalReference> inputData;

    @FieldInfo(label = "External used resources", layout = "Related resources")
    private List<TargetExternalReference> externalInputData;

    @FieldInfo(label = "Used by", layout = "Related resources")
    private List<TargetInternalReference> outputData;

    @Getter
    @Setter
    @MetaInfo(name = "SubjectGroup")
    public static class DSVSubjectGroup {

        public DSVSubjectGroup() {
            this("Dataset.SubjectGroup");
        }

        protected DSVSubjectGroup(String type) {
            this.type = new Value<>(type);
        }

        @ElasticSearchInfo(type = "keyword")
        private Value<String> type;

        @FieldInfo(ignoreForSearch = true, visible = false)
        private String id;

        @FieldInfo
        private Value<String> title;

        @FieldInfo(label = "Subjects")
        private Value<String> numberOfSubjects;

        @FieldInfo(label = "Species", separator = ", ")
        private List<TargetInternalReference> species;

        @FieldInfo(label = "Sex", separator = ", ")
        private List<TargetInternalReference> sex;

        @FieldInfo(label = "Strain")
        private List<TargetInternalReference> strain;

        @FieldInfo(label = "Genetic strain type")
        private List<TargetInternalReference> geneticStrainType;

        @FieldInfo(label = "Age")
        private Value<String> age;

        @FieldInfo(label = "Age category")
        private List<TargetInternalReference> ageCategory;

        @FieldInfo(label = "Attributes", separator = ", ")
        private List<TargetInternalReference> attributes;

        @FieldInfo(label = "Handedness")
        private TargetInternalReference handedness;

        @FieldInfo(label = "Pathology")
        private List<TargetInternalReference> pathology;

        @FieldInfo(label = "Weight")
        private Value<String> weight;

        @FieldInfo(label = "Additional remarks", fieldType = FieldInfo.FieldType.MARKDOWN)
        private Value<String> additionalRemarks;

        @FieldInfo(labelHidden = true)
        private List<TargetExternalReference> serviceLinks = new ArrayList<>();

        @FieldInfo(label = "This subject group was also used in other data publications")
        private List<TargetInternalReference> otherPublications;
    }

    @Getter
    @Setter
    @MetaInfo(name = "SubjectGroupState")
    public static class DSVSubjectGroupState extends DSVSubjectGroup {

        public DSVSubjectGroupState() {
            super("Dataset.SubjectGroupState");
        }
    }

    @Getter
    @Setter
    @MetaInfo(name = "Subject")
    public static class DSVSubject {

        public DSVSubject() {
            this("Dataset.Subject");
        }

        protected DSVSubject(String type) {
            this.type = new Value<>(type);
        }

        @FieldInfo(ignoreForSearch = true, visible = false)
        private String id;

        @ElasticSearchInfo(type = "keyword")
        private Value<String> type;

        @FieldInfo
        private Value<String> title;

        @FieldInfo(label = "Species")
        private List<TargetInternalReference> species;

        @FieldInfo(label = "Sex", separator = ", ")
        private List<TargetInternalReference> sex;

        @FieldInfo(label = "Strain")
        private List<TargetInternalReference> strain;

        @FieldInfo(label = "Genetic strain type")
        private List<TargetInternalReference> geneticStrainType;

        @FieldInfo(label = "Age category")
        private List<TargetInternalReference> ageCategory;

        //These properties are only for merged subjects since these are properties of the state

        @FieldInfo(label = "Attributes", separator = ", ")
        private List<TargetInternalReference> attributes;

        @FieldInfo(label = "Handedness")
        private TargetInternalReference handedness;

        @FieldInfo(label = "Pathology")
        private List<TargetInternalReference> pathology;

        @FieldInfo(label = "Age")
        private Value<String> age;

        @FieldInfo(label = "Weight")
        private Value<String> weight;

        @FieldInfo(label = "Additional remarks", separator = ", ", fieldType = FieldInfo.FieldType.MARKDOWN)
        private Value<String> additionalRemarks;

        @FieldInfo(labelHidden = true)
        private List<TargetExternalReference> serviceLinks = new ArrayList<>();

        @FieldInfo(label = "This specimen was also used in other data publications")
        private List<TargetInternalReference> otherPublications;
    }

    @Getter
    @Setter
    @MetaInfo(name = "SubjectState")
    public static class DSVSubjectState extends DSVSubject {

        public DSVSubjectState() {
            super("Dataset.SubjectState");
        }
    }

    @Getter
    @Setter
    @MetaInfo(name = "TissueSample")
    public static class DSVTissueSample {
        public DSVTissueSample() {
            this("Dataset.TissueSample");
        }

        public DSVTissueSample(String type) {
            this.type = new Value<>(type);
        }

        @FieldInfo(ignoreForSearch = true, visible = false)
        private String id;

        @ElasticSearchInfo(type = "keyword")
        private Value<String> type;

        @FieldInfo
        private Value<String> title;

        @FieldInfo(label = "Sex", separator = ", ")
        private List<TargetInternalReference> sex;

        @FieldInfo(label = "Anatomical location")
        private List<TargetInternalReference> anatomicalLocation;

        @FieldInfo(label = "Origin")
        private TargetInternalReference origin;

        @FieldInfo(label = "Species")
        private List<TargetInternalReference> species;

        @FieldInfo(label = "Strain")
        private List<TargetInternalReference> strain;

        @FieldInfo(label = "Genetic strain type")
        private List<TargetInternalReference> geneticStrainType;

        @FieldInfo(label = "Laterality")
        private List<TargetInternalReference> laterality;

        @FieldInfo(label = "Attributes")
        private List<TargetInternalReference> attributes;

        @FieldInfo(label = "Type")
        private TargetInternalReference tissueSampleType;

        @FieldInfo(label = "Age")
        private Value<String> age;

        @FieldInfo(label = "Age category")
        private List<TargetInternalReference> ageCategory;

        @FieldInfo(label = "Pathology")
        private List<TargetInternalReference> pathology;

        @FieldInfo(label = "Weight")
        private Value<String> weight;

        @FieldInfo(label = "Additional remarks", fieldType = FieldInfo.FieldType.MARKDOWN)
        private Value<String> additionalRemarks;

        @FieldInfo(labelHidden = true)
        private List<TargetExternalReference> serviceLinks = new ArrayList<>();
    }

    @Getter
    @Setter
    @MetaInfo(name = "TissueSampleState")
    public static class DSVTissueSampleState extends DSVTissueSample {

        public DSVTissueSampleState() {
            super("Dataset.TissueSampleState");
        }

    }

    @Getter
    @Setter
    @MetaInfo(name = "TissueSampleCollection")
    public static class DSVTissueSampleCollection {

        public DSVTissueSampleCollection() {
            this("Dataset.TissueSampleCollection");
        }

        protected DSVTissueSampleCollection(String type) {
            this.type = new Value<>(type);
        }

        @FieldInfo(ignoreForSearch = true, visible = false)
        private String id;

        @ElasticSearchInfo(type = "keyword")
        private Value<String> type;

        @FieldInfo
        private Value<String> title;

        @FieldInfo(label = "Tissue samples")
        private Value<String> tissueSamples;

        @FieldInfo(label = "Sex", separator = ", ")
        private List<TargetInternalReference> sex;

        @FieldInfo(label = "Anatomical location")
        private List<TargetInternalReference> anatomicalLocation;

        @FieldInfo(label = "Origin")
        private TargetInternalReference origin;

        @FieldInfo(label = "Species")
        private List<TargetInternalReference> species;

        @FieldInfo(label = "Strain")
        private List<TargetInternalReference> strain;

        @FieldInfo(label = "Genetic strain type")
        private List<TargetInternalReference> geneticStrainType;

        @FieldInfo(label = "Laterality")
        private List<TargetInternalReference> laterality;

        @FieldInfo(label = "Attributes")
        private List<TargetInternalReference> attributes;

        @FieldInfo(label = "Type")
        private TargetInternalReference tissueSampleType;

        @FieldInfo(label = "Age")
        private Value<String> age;

        @FieldInfo(label = "Age category")
        private List<TargetInternalReference> ageCategory;

        @FieldInfo(label = "Pathology")
        private List<TargetInternalReference> pathology;

        @FieldInfo(label = "Weight")
        private Value<String> weight;

        @FieldInfo(label = "Additional remarks", fieldType = FieldInfo.FieldType.MARKDOWN)
        private Value<String> additionalRemarks;

        @FieldInfo(labelHidden = true)
        private List<TargetExternalReference> serviceLinks = new ArrayList<>();

    }

    @Getter
    @Setter
    @MetaInfo(name = "TissueSampleCollectionState")
    public static class DSVTissueSampleCollectionState extends DSVTissueSampleCollection {
        public DSVTissueSampleCollectionState() {
            super("Dataset.TissueSampleCollectionState");
        }
    }

    @Getter
    @Setter
    @MetaInfo(name = "SpecimenOverview")
    public static class DSVSpecimenOverview {

        @ElasticSearchInfo(type = "keyword")
        private Value<String> type = new Value<>("Dataset.SpecimenOverview");

        @FieldInfo
        private Value<String> title;

        @FieldInfo(label = "Subject groups")
        private Value<String> numberOfSubjectGroups;

        @JsonIgnore
        private transient Set<String> subjectGroupIds = new HashSet<>();

        @JsonIgnore
        private transient Set<TargetInternalReference> anatomicalLocationsOfTissueSamples = new HashSet<>();

        @FieldInfo(label = "Subjects")
        private Value<String> numberOfSubjects;

        @JsonIgnore
        private transient Set<String> subjectIds = new HashSet<>();

        @FieldInfo(label = "Tissue sample collections")
        private Value<String> numberOfTissueSampleCollections;

        @JsonIgnore
        private transient Set<String> tissueSampleCollectionIds = new HashSet<>();

        @FieldInfo(label = "Tissue samples")
        private Value<String> numberOfTissueSamples;

        @JsonIgnore
        private transient Set<String> tissueSampleIds = new HashSet<>();

        @FieldInfo(label = "Species")
        private List<TargetInternalReference> species = new ArrayList<>();

        @FieldInfo(label = "Sex")
        private List<TargetInternalReference> sex = new ArrayList<>();

        @FieldInfo(label = "Strains")
        private List<TargetInternalReference> strains = new ArrayList<>();

        @FieldInfo(label = "Genetic strain types")
        private List<TargetInternalReference> geneticStrainTypes = new ArrayList<>();

        @FieldInfo(label = "Pathology")
        private List<TargetInternalReference> pathology = new ArrayList<>();

        @JsonIgnore
        private transient Map<String, Map<TargetInternalReference, Map<String, Set<String>>>> collector = new HashMap<>();

        private String normalizeTypeName(String type, boolean plural) {
            String typeName = StringUtils.uncapitalize(type);
            if (plural && !typeName.endsWith("s")) {
                typeName = String.format("%ss", typeName);
            }
            return typeName;
        }

        private void collect(String sourceId, String key, List<TargetInternalReference> references, String type) {
            if (references != null && sourceId != null) {
                final Map<TargetInternalReference, Map<String, Set<String>>> map = collector.computeIfAbsent(key, k -> new HashMap<>());
                references.forEach(r -> {
                    final Map<String, Set<String>> countsPerType = map.computeIfAbsent(r, k -> new HashMap<>());
                    final Set<String> ids = countsPerType.computeIfAbsent(type, k -> new HashSet<>());
                    ids.add(sourceId);
                });
            }
        }

        private List<TargetInternalReference> flush(String key) {
            final Map<TargetInternalReference, Map<String, Set<String>>> map = collector.computeIfAbsent(key, k -> Collections.emptyMap());
            Stream<TargetInternalReference> stream = map.keySet().stream();
            if (!map.isEmpty()) {
                stream = stream.map(k -> {
                    final Map<String, Set<String>> count = map.get(k);
                    final TargetInternalReference targetInternalReference = new TargetInternalReference(k.getReference(), k.getValue());
                    targetInternalReference.setCount(count.keySet().stream().sorted().map(t -> String.format("%d %s", count.get(t).size(), normalizeTypeName(t, count.get(t).size() > 1))).collect(Collectors.toList()));
                    return targetInternalReference;
                });
            }
            final List<TargetInternalReference> result = stream.filter(Objects::nonNull).distinct().sorted().collect(Collectors.toList());
            return result.isEmpty() ? null : result;
        }


        public DSVSpecimenOverview flush() {
            if (subjectIds.size() > 0) {
                setNumberOfSubjects(new Value<>(String.valueOf(subjectIds.size())));
            }
            if (subjectGroupIds.size() > 0) {
                setNumberOfSubjectGroups(new Value<>(String.valueOf(subjectGroupIds.size())));
            }
            if (tissueSampleIds.size() > 0) {
                setNumberOfTissueSamples(new Value<>(String.valueOf(tissueSampleIds.size())));
            }
            if (tissueSampleCollectionIds.size() > 0) {
                setNumberOfTissueSampleCollections(new Value<>(String.valueOf(tissueSampleCollectionIds.size())));
            }

            setPathology(flush("pathology"));
            setSpecies(flush("species"));
            setSex(flush("sex"));
            setStrains(flush("strains"));
            setGeneticStrainTypes(flush("geneticStrainTypes"));
            return this;
        }

        public void collectPathology(String sourceId, List<TargetInternalReference> references, String type) {
            collect(sourceId, "pathology", references, type);
        }

        public void collectSpecies(String sourceId, List<TargetInternalReference> references, String type) {
            collect(sourceId, "species", references, type);
        }

        public void collectSex(String sourceId, List<TargetInternalReference> references, String type) {
            collect(sourceId, "sex", references, type);
        }

        public void collectStrains(String sourceId, List<TargetInternalReference> references, String type) {
            collect(sourceId, "strains", references, type);
        }

        public void collectGeneticStrainTypes(String sourceId, List<TargetInternalReference> references, String type) {
            collect(sourceId, "geneticStrainTypes", references, type);
        }

        @JsonIgnore
        public List<String> getAllSpecimenIds() {
            return Stream.concat(
                    Stream.concat(getSubjectIds().stream(), getSubjectGroupIds().stream()),
                    Stream.concat(getTissueSampleIds().stream(), getTissueSampleCollectionIds().stream())
            ).distinct().toList();
        }
    }

}
