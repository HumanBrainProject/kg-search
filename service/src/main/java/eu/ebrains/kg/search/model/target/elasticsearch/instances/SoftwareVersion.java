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
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.ISODateValue;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetExternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Value;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@MetaInfo(name="Software", order=6, searchable=true)
public class SoftwareVersion implements TargetInstance {
    @ElasticSearchInfo(type = "keyword")
    private final Value<String> type = new Value<>("Software");

    @FieldInfo(ignoreForSearch = true, visible = false)
    private String id;

    @ElasticSearchInfo(type = "keyword")
    @FieldInfo(visible = false, ignoreForSearch = true)
    private List<String> identifier;

    @FieldInfo(label = "Name", boost = 20, sort = true)
    private Value<String> title;

    @FieldInfo(layout = FieldInfo.Layout.HEADER)
    private Value<String> editorId;

    @FieldInfo(label = "Software Versions")
    private TargetInternalReference software;

    @FieldInfo(labelHidden = true, markdown = true, boost = 2)
    private Value<String> description;

    @FieldInfo(label = "License")
    private List<Value<String>> license;

    @FieldInfo(label = "Latest Version", layout = FieldInfo.Layout.SUMMARY)
    private Value<String> version;

    @FieldInfo(label = "Application Category", layout = FieldInfo.Layout.SUMMARY, separator = ", ")
    private List<Value<String>> appCategory;

    @FieldInfo(label = "Custodians", separator = "; ", hint = "A custodian is the person responsible for the data bundle.", boost = 10)
    private List<TargetInternalReference> custodians;

    @FieldInfo(label = "Developers", separator = "; ", boost = 10)
    private List<TargetInternalReference> developers;

    @FieldInfo(label = "Operating System", layout = FieldInfo.Layout.SUMMARY, facet = FieldInfo.Facet.LIST, tagIcon = "<svg width=\"50\" height=\"50\" viewBox=\"0 0 11.377083 13.05244\" xmlns=\"http://www.w3.org/2000/svg\"><path d=\"M 5.6585847,-3.1036376e-7 2.8334327,1.5730297 0.0088,3.1455497 0.0047,6.4719597 0,9.7983697 2.8323857,11.42515 l 2.831867,1.62729 1.070218,-0.60358 c 0.588756,-0.33201 1.874409,-1.06813 2.856675,-1.63608 L 11.377083,9.7797697 v -3.24735 -3.24786 l -0.992187,-0.62477 C 9.8391917,2.3160397 8.5525477,1.5769697 7.5256387,1.0175097 Z M 5.6580697,3.7398297 a 2.7061041,2.7144562 0 0 1 2.706293,2.71456 2.7061041,2.7144562 0 0 1 -2.706293,2.71456 2.7061041,2.7144562 0 0 1 -2.70578,-2.71456 2.7061041,2.7144562 0 0 1 2.70578,-2.71456 z\"/></svg>")
    private List<Value<String>> operatingSystem;

    @FieldInfo(label = "Homepage", layout = FieldInfo.Layout.SUMMARY)
    private List<TargetExternalReference> homepage;

    @FieldInfo(label = "Source code", layout = FieldInfo.Layout.SUMMARY)
    private List<TargetExternalReference> sourceCode;

    @FieldInfo(label = "Documentation", layout = FieldInfo.Layout.SUMMARY)
    private List<TargetExternalReference> documentation;

    @FieldInfo(label = "Features", layout = FieldInfo.Layout.SUMMARY, tagIcon = "<svg width=\"50\" height=\"50\" viewBox=\"0 0 1792 1792\" xmlns=\"http://www.w3.org/2000/svg\"><path d=\"M576 448q0-53-37.5-90.5t-90.5-37.5-90.5 37.5-37.5 90.5 37.5 90.5 90.5 37.5 90.5-37.5 37.5-90.5zm1067 576q0 53-37 90l-491 492q-39 37-91 37-53 0-90-37l-715-716q-38-37-64.5-101t-26.5-117v-416q0-52 38-90t90-38h416q53 0 117 26.5t102 64.5l715 714q37 39 37 91z\"/></svg>")
    private List<Value<String>> features;

    @JsonProperty("first_release")
    @FieldInfo(label = "First release", ignoreForSearch = true, visible = false, type=FieldInfo.Type.DATE)
    private ISODateValue firstRelease;

    @JsonProperty("last_release")
    @FieldInfo(label = "Last release", ignoreForSearch = true, visible = false, type=FieldInfo.Type.DATE)
    private ISODateValue lastRelease;

    private List<TargetInternalReference> versions;

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

    public void setEditorId(String editorId){
        setEditorId(StringUtils.isBlank(editorId) ? null : new Value<>(editorId));
    }

    public TargetInternalReference getSoftware() { return software; }

    public void setSoftware(TargetInternalReference software) { this.software = software;  }

    public void setTitle(String title){
        setTitle(StringUtils.isBlank(title) ? null : new Value<>(title));
    }

    public void setDescription(String description){
        setDescription(StringUtils.isBlank(description) ? null : new Value<>(description));
    }

    public Value<String> getEditorId() {
        return editorId;
    }

    public void setEditorId(Value<String> editorId) {
        this.editorId = editorId;
    }

    public List<Value<String>> getAppCategory() {
        return appCategory;
    }

    public void setAppCategory(List<String> appCategory) {
        this.appCategory = appCategory == null ? null : appCategory.stream().map(Value::new).collect(Collectors.toList());
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

    public List<TargetExternalReference> getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(List<TargetExternalReference> sourceCode) {
        this.sourceCode = sourceCode;
    }

    public List<Value<String>> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features == null ? null : features.stream().map(Value::new).collect(Collectors.toList());
    }

    public List<TargetExternalReference> getDocumentation() {
        return documentation;
    }

    public void setDocumentation(List<TargetExternalReference> documentation) {
        this.documentation =  documentation;
    }

    public List<Value<String>> getLicense() {
        return license;
    }

    public void setLicense(List<String> license) {
        this.license = license == null ? null : license.stream().map(Value::new).collect(Collectors.toList());
    }

    public List<Value<String>> getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(List<String> operatingSystem) {

        this.operatingSystem = operatingSystem == null ? null : operatingSystem.stream().map(Value::new).collect(Collectors.toList());
    }

    public Value<String> getVersion() {
        return version;
    }

    public void setVersion(String version) {
        setVersion(StringUtils.isBlank(version) ? null : new Value<>(version));
    }

    public void setVersion(Value<String> version) {
        this.version = version;
    }

    public List<TargetExternalReference> getHomepage() {
        return homepage;
    }

    public void setHomepage(List<TargetExternalReference> homepage) {
        this.homepage = homepage;
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

    public void setVersions(List<TargetInternalReference> versions) { this.versions = versions; }
}
