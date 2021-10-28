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

package eu.ebrains.kg.search.model.target.elasticsearch.instances;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.MetaInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.TargetInstance;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;

// To ensure backwards compatibility for the URLs, we can't just rename this - since it's a technical key only anyhow this has no direct impact though
@MetaInfo(name="Software", order=6, searchable=true)
public class SoftwareVersion implements TargetInstance, VersionedInstance {

    @ElasticSearchInfo(type = "keyword")
    private final Value<String> type = new Value<>("Software");

    @FieldInfo(ignoreForSearch = true, visible = false)
    private String id;

    @ElasticSearchInfo(type = "keyword")
    @FieldInfo(visible = false, ignoreForSearch = true)
    private List<String> identifier;

    @FieldInfo(label = "Name", boost = 20, sort = true)
    private Value<String> title;

    /**
     * @deprecated  This is not needed for the new KG anymore since the id is consistent across search/editor
     */
    @FieldInfo(layout = FieldInfo.Layout.HEADER)
    @Deprecated
    private Value<String> editorId;

    @FieldInfo(label = "Developers", separator = "; ", layout = FieldInfo.Layout.HEADER, type = FieldInfo.Type.TEXT, boost = 10, labelHidden = true)
    private List<TargetInternalReference> developers;

    @FieldInfo(label = "Cite software", isButton = true, markdown = true, icon="quote-left")
    private Value<String> citation;

    /**
     * @deprecated
     */
    @Deprecated
    @FieldInfo(label = "License", type = FieldInfo.Type.TEXT, facetOrder = FieldInfo.FacetOrder.BYVALUE, facet = FieldInfo.Facet.LIST)
    private List<Value<String>> licenseOld;

    @FieldInfo(label = "License", type = FieldInfo.Type.TEXT)
    private List<TargetExternalReference> license;


    @FieldInfo(label = "Copyright", type = FieldInfo.Type.TEXT)
    private Value<String> copyright;

    @FieldInfo(label = "Project", boost = 10, order = 3)
    private List<TargetInternalReference> projects;

    @FieldInfo(label = "Custodians", separator = "; ", hint = "A custodian is the person responsible for the data bundle.", boost = 10)
    private List<TargetInternalReference> custodians;

    /**
     * @deprecated use homepage for openMINDS instead
     */
    @Deprecated
    @FieldInfo(label = "Homepage")
    private List<TargetExternalReference> homepageOld;

    @FieldInfo(label = "Homepage")
    private TargetExternalReference homepage;

    /**
     * @deprecated use sourceCode for openMINDS instead
     */
    @Deprecated
    @FieldInfo(label = "Source code")
    private List<TargetExternalReference> sourceCodeOld;

    @FieldInfo(label = "Source code")
    private TargetExternalReference sourceCode;

    @FieldInfo(label = "Documentation")
    private List<TargetExternalReference> documentation;

    @FieldInfo(label = "Support")
    private List<TargetExternalReference> support;

    @FieldInfo(labelHidden = true, markdown = true, boost = 2)
    private Value<String> description;


    @FieldInfo(label = "Related publications", markdown = true, layout = FieldInfo.Layout.GROUP)
    private List<Value<String>> publications;

    /**
     * @deprecated This is no longer in use for openMINDS
     */
    @Deprecated
    @FieldInfo(label = "Latest Version", layout = FieldInfo.Layout.SUMMARY)
    private Value<String> versionOld;


    /**
     * @deprecated use appCategory for openMINDS instead
     */
    @Deprecated
    @FieldInfo(label = "Application Category", layout = FieldInfo.Layout.SUMMARY, separator = ", ", facet = FieldInfo.Facet.LIST)
    private List<Value<String>> appCategoryOld;

    @FieldInfo(label = "Application Category", layout = FieldInfo.Layout.SUMMARY, separator = ", ", facet = FieldInfo.Facet.LIST)
    private List<TargetInternalReference> appCategory;


    @FieldInfo(label = "License", type = FieldInfo.Type.TEXT, visible = false, facetOrder = FieldInfo.FacetOrder.BYCOUNT, facet = FieldInfo.Facet.LIST)
    private List<Value<String>> licenseForFilter;

    /**
     * @deprecated use operatingSystem for openMINDS instead
     */
    @Deprecated
    @FieldInfo(label = "Operating System", layout = FieldInfo.Layout.SUMMARY, facet = FieldInfo.Facet.LIST, tagIcon = "<svg width=\"50\" height=\"50\" viewBox=\"0 0 11.377083 13.05244\" xmlns=\"http://www.w3.org/2000/svg\"><path d=\"M 5.6585847,-3.1036376e-7 2.8334327,1.5730297 0.0088,3.1455497 0.0047,6.4719597 0,9.7983697 2.8323857,11.42515 l 2.831867,1.62729 1.070218,-0.60358 c 0.588756,-0.33201 1.874409,-1.06813 2.856675,-1.63608 L 11.377083,9.7797697 v -3.24735 -3.24786 l -0.992187,-0.62477 C 9.8391917,2.3160397 8.5525477,1.5769697 7.5256387,1.0175097 Z M 5.6580697,3.7398297 a 2.7061041,2.7144562 0 0 1 2.706293,2.71456 2.7061041,2.7144562 0 0 1 -2.706293,2.71456 2.7061041,2.7144562 0 0 1 -2.70578,-2.71456 2.7061041,2.7144562 0 0 1 2.70578,-2.71456 z\"/></svg>")
    private List<Value<String>> operatingSystemOld;

    @FieldInfo(label = "Operating System", layout = FieldInfo.Layout.SUMMARY, facet = FieldInfo.Facet.LIST)
    private List<TargetInternalReference> operatingSystem;

    @FieldInfo(label = "Devices", layout = FieldInfo.Layout.SUMMARY, facet = FieldInfo.Facet.LIST)
    private List<TargetInternalReference> devices;

    @FieldInfo(label = "Programming languages", layout = FieldInfo.Layout.SUMMARY, facet = FieldInfo.Facet.LIST)
    private List<TargetInternalReference> programmingLanguages;

    @FieldInfo(label = "Requirements")
    private List<Value<String>> requirements;

    @FieldInfo(label = "Features", layout = FieldInfo.Layout.SUMMARY, tagIcon = "<svg width=\"50\" height=\"50\" viewBox=\"0 0 1792 1792\" xmlns=\"http://www.w3.org/2000/svg\"><path d=\"M576 448q0-53-37.5-90.5t-90.5-37.5-90.5 37.5-37.5 90.5 37.5 90.5 90.5 37.5 90.5-37.5 37.5-90.5zm1067 576q0 53-37 90l-491 492q-39 37-91 37-53 0-90-37l-715-716q-38-37-64.5-101t-26.5-117v-416q0-52 38-90t90-38h416q53 0 117 26.5t102 64.5l715 714q37 39 37 91z\"/></svg>")
    @Deprecated
    /**
     * @deprecated features are now references to controlled terms
     */
    private List<Value<String>> featuresOld;

    @FieldInfo(label = "Features", layout = FieldInfo.Layout.SUMMARY, facet = FieldInfo.Facet.LIST, isFilterableFacet = true)
    private List<TargetInternalReference> features;

    @FieldInfo(label = "Languages", layout = FieldInfo.Layout.SUMMARY)
    private List<TargetInternalReference> languages;

    @FieldInfo(label = "Keywords", facet = FieldInfo.Facet.LIST, order = 1, overviewMaxDisplay = 3, layout = FieldInfo.Layout.SUMMARY, overview = true, isFilterableFacet = true, tagIcon = "<svg width=\"50\" height=\"50\" viewBox=\"0 0 1792 1792\" xmlns=\"http://www.w3.org/2000/svg\"><path d=\"M576 448q0-53-37.5-90.5t-90.5-37.5-90.5 37.5-37.5 90.5 37.5 90.5 90.5 37.5 90.5-37.5 37.5-90.5zm1067 576q0 53-37 90l-491 492q-39 37-91 37-53 0-90-37l-715-716q-38-37-64.5-101t-26.5-117v-416q0-52 38-90t90-38h416q53 0 117 26.5t102 64.5l715 714q37 39 37 91z\"/></svg>")
    @Deprecated
    /**
     * @deprecated keywords are - although existing in openMINDS - not very suitable for Software. Additionally, they would be TargetInternalReferences if there would be any. Therefore, this field is used for the old structure only.
     */
    private List<Value<String>> keywords;

    @FieldInfo(label = "Input formats", visible = false, facet = FieldInfo.Facet.LIST, isFilterableFacet = true)
    private List<Value<String>> inputFormatsForFilter;

    @FieldInfo(label = "Output formats", visible = false, facet = FieldInfo.Facet.LIST, isFilterableFacet = true)
    private List<Value<String>> outputFormatsForFilter;

    @FieldInfo(label = "Input formats", layout = FieldInfo.Layout.GROUP, isTable = true)
    private List<Children<FileFormat>> inputFormat;

    @FieldInfo(label = "Output formats", layout = FieldInfo.Layout.GROUP, isTable = true)
    private List<Children<FileFormat>> outputFormats;

    @FieldInfo(label = "Sub-components", layout = FieldInfo.Layout.GROUP)
    private List<TargetInternalReference> components;

    @JsonProperty("first_release")
    @FieldInfo(label = "First release", ignoreForSearch = true, visible = false, type=FieldInfo.Type.DATE)
    private ISODateValue firstRelease;

    @JsonProperty("last_release")
    @FieldInfo(label = "Last release", ignoreForSearch = true, visible = false, type=FieldInfo.Type.DATE)
    private ISODateValue lastRelease;

    private String version;

    private List<TargetInternalReference> versions;

    public static class FileFormat {
        @FieldInfo(label = "Name", sort = true)
        private TargetInternalReference name;

        @FieldInfo(label = "File extensions", sort = true)
        private List<Value<String>> fileExtensions;

        @FieldInfo(label = "Media type", sort = true)
        private Value<String> relatedMediaType;

        public TargetInternalReference getName() {
            return name;
        }

        public void setName(TargetInternalReference name) {
            this.name = name;
        }

        public List<Value<String>> getFileExtensions() {
            return fileExtensions;
        }

        public void setFileExtensions(List<Value<String>> fileExtensions) {
            this.fileExtensions = fileExtensions;
        }

        public Value<String> getRelatedMediaType() {
            return relatedMediaType;
        }

        public void setRelatedMediaType(Value<String> relatedMediaType) {
            this.relatedMediaType = relatedMediaType;
        }
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

    @Override
    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public void setIdentifier(List<String> identifier) {
        this.identifier = identifier;
    }

    public void setTitle(String title){
        setTitle(StringUtils.isBlank(title) ? null : new Value<>(title));
    }

    public void setDescription(String description){
        setDescription(StringUtils.isBlank(description) ? null : new Value<>(description));
    }

    public Value<String> getEditorId() {
        return editorId;
    }

    @Deprecated
    public void setEditorId(Value<String> editorId) {
        this.editorId = editorId;
    }

    public List<Value<String>> getAppCategoryOld() {
        return appCategoryOld;
    }

    public List<TargetInternalReference> getCustodians() {
        return custodians;
    }

    public void setCustodians(List<TargetInternalReference> custodians) {
        this.custodians = custodians;
    }

    public List<TargetInternalReference> getDevelopers() {
        return developers;
    }

    public void setDevelopers(List<TargetInternalReference> developers) {
        this.developers = developers;
    }

    @Override
    public List<String> getIdentifier() {
        return identifier;
    }

    public Value<String> getDescription() {
        return description;
    }

    public void setDescription(Value<String> description) {
        this.description = description;
    }

    public List<TargetExternalReference> getSourceCodeOld() {
        return sourceCodeOld;
    }

    public void setSourceCodeOld(List<TargetExternalReference> sourceCodeOld) {
        this.sourceCodeOld = sourceCodeOld;
    }

    public List<Value<String>> getFeaturesOld() {
        return featuresOld;
    }

    public List<TargetExternalReference> getDocumentation() {
        return documentation;
    }

    public void setDocumentation(List<TargetExternalReference> documentation) {
        this.documentation =  documentation;
    }

    public List<Value<String>> getLicenseOld() {
        return licenseOld;
    }

    public List<Value<String>> getOperatingSystemOld() {
        return operatingSystemOld;
    }

    public Value<String> getVersionOld() {
        return versionOld;
    }

    @Deprecated
    public void setVersionOld(Value<String> versionOld) {
        this.versionOld = versionOld;
    }

    public List<TargetExternalReference> getHomepageOld() {
        return homepageOld;
    }

    public void setHomepageOld(List<TargetExternalReference> homepageOld) {
        this.homepageOld = homepageOld;
    }

    public Value<String> getTitle() { return title; }

    public void setTitle(Value<String> title) {
        this.title = title;
    }
    public ISODateValue getFirstRelease() {
        return firstRelease;
    }

    public void setFirstRelease(ISODateValue firstRelease) {
        this.firstRelease = firstRelease;
    }

    public void setFirstRelease(Date firstRelease) {
        this.setFirstRelease(firstRelease != null ? new ISODateValue(firstRelease) : null);
    }

    public ISODateValue getLastRelease() {
        return lastRelease;
    }

    public void setLastRelease(ISODateValue lastRelease) {
        this.lastRelease = lastRelease;
    }

    public void setLastRelease(Date lastRelease) {
        this.setLastRelease(lastRelease != null ? new ISODateValue(lastRelease) : null);
    }

    public Value<String> getType() {
        return type;
    }

    public List<TargetInternalReference> getVersions() { return versions; }

    public Value<String> getCitation() {
        return citation;
    }

    public void setCitation(Value<String> citation) {
        this.citation = citation;
    }

    @Deprecated
    public void setLicenseOld(List<Value<String>> licenseOld) {
        this.licenseOld = licenseOld;
    }

    public Value<String> getCopyright() {
        return copyright;
    }

    public void setCopyright(Value<String> copyright) {
        this.copyright = copyright;
    }

    public List<TargetInternalReference> getProjects() {
        return projects;
    }

    public void setProjects(List<TargetInternalReference> projects) {
        this.projects = projects;
    }

    public List<Value<String>> getPublications() {
        return publications;
    }

    public void setPublications(List<Value<String>> publications) {
        this.publications = publications;
    }

    @Deprecated
    public void setAppCategoryOld(List<Value<String>> appCategoryOld) {
        this.appCategoryOld = appCategoryOld;
    }

    @Deprecated
    public void setOperatingSystemOld(List<Value<String>> operatingSystemOld) {
        this.operatingSystemOld = operatingSystemOld;
    }



    public List<Value<String>> getRequirements() {
        return requirements;
    }

    public void setRequirements(List<Value<String>> requirements) {
        this.requirements = requirements;
    }

    @Deprecated
    public void setFeaturesOld(List<Value<String>> featuresOld) {
        this.featuresOld = featuresOld;
    }


    public List<Value<String>> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<Value<String>> keywords) {
        this.keywords = keywords;
    }

    public List<TargetExternalReference> getSupport() {
        return support;
    }

    public void setSupport(List<TargetExternalReference> support) {
        this.support = support;
    }

    public List<Children<FileFormat>> getInputFormat() {
        return inputFormat;
    }

    public void setInputFormat(List<Children<FileFormat>> inputFormat) {
        this.inputFormat = inputFormat;
    }

    public List<Children<FileFormat>> getOutputFormats() {
        return outputFormats;
    }

    public void setOutputFormats(List<Children<FileFormat>> outputFormats) {
        this.outputFormats = outputFormats;
    }

    public List<TargetExternalReference> getLicense() {
        return license;
    }

    public void setLicense(List<TargetExternalReference> license) {
        this.license = license;
    }

    public List<TargetInternalReference> getAppCategory() {
        return appCategory;
    }

    public void setAppCategory(List<TargetInternalReference> appCategory) {
        this.appCategory = appCategory;
    }

    public List<TargetInternalReference> getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(List<TargetInternalReference> operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public List<TargetInternalReference> getDevices() {
        return devices;
    }

    public void setDevices(List<TargetInternalReference> devices) {
        this.devices = devices;
    }

    public List<TargetInternalReference> getProgrammingLanguages() {
        return programmingLanguages;
    }

    public void setProgrammingLanguages(List<TargetInternalReference> programmingLanguages) {
        this.programmingLanguages = programmingLanguages;
    }

    public List<TargetInternalReference> getFeatures() {
        return features;
    }

    public void setFeatures(List<TargetInternalReference> features) {
        this.features = features;
    }

    public List<TargetInternalReference> getLanguages() {
        return languages;
    }

    public void setLanguages(List<TargetInternalReference> languages) {
        this.languages = languages;
    }

    public List<TargetInternalReference> getComponents() {
        return components;
    }

    public void setComponents(List<TargetInternalReference> components) {
        this.components = components;
    }

    public boolean isSearchable() {
        return isSearchable;
    }

    public TargetExternalReference getHomepage() {
        return homepage;
    }

    public TargetExternalReference getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(TargetExternalReference sourceCode) {
        this.sourceCode = sourceCode;
    }

    public void setHomepage(TargetExternalReference homepage) {
        this.homepage = homepage;
    }

    public List<Value<String>> getLicenseForFilter() {
        return licenseForFilter;
    }

    public void setLicenseForFilter(List<Value<String>> licenseForFilter) {
        this.licenseForFilter = licenseForFilter;
    }

    public List<Value<String>> getInputFormatsForFilter() {
        return inputFormatsForFilter;
    }

    public void setInputFormatsForFilter(List<Value<String>> inputFormatsForFilter) {
        this.inputFormatsForFilter = inputFormatsForFilter;
    }

    public List<Value<String>> getOutputFormatsForFilter() {
        return outputFormatsForFilter;
    }

    public void setOutputFormatsForFilter(List<Value<String>> outputFormatsForFilter) {
        this.outputFormatsForFilter = outputFormatsForFilter;
    }

    @Override
    public void setVersions(List<TargetInternalReference> versions) { this.versions = versions; }

    @Override
    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }
}
