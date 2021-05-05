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

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.MetaInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.TargetInstance;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.ISODateValue;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Value;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@MetaInfo(name="Contributor", identifier = "uniminds/core/person/v1.0.0/search", order=7)
public class Contributor implements TargetInstance {
    @ElasticSearchInfo(type = "keyword")
    private Value<String> type = new Value<>("Contributor");

    @FieldInfo(ignoreForSearch = true, visible = false)
    private String id;

    @FieldInfo(visible = false)
    private List<String> identifier;

    @FieldInfo(sort = true, label = "Name", boost = 20)
    private Value<String> title;

    @FieldInfo(layout = FieldInfo.Layout.HEADER)
    private Value<String> editorId;

    @FieldInfo(label = "Custodian of", layout = FieldInfo.Layout.GROUP, overview = true)
    private List<TargetInternalReference> custodianOf;

    @FieldInfo(label = "Custodian of models", layout = FieldInfo.Layout.GROUP, overview = true)
    private List<TargetInternalReference> custodianOfModel;

    @FieldInfo(label = "Publications", markdown = true, facet = FieldInfo.Facet.EXISTS, layout = FieldInfo.Layout.GROUP)
    private List<Value<String>> publications;

    @FieldInfo(label = "Contributions", facet = FieldInfo.Facet.EXISTS, layout = FieldInfo.Layout.GROUP, type = FieldInfo.Type.TEXT, overview = true)
    private List<TargetInternalReference> contributions;

    @FieldInfo(label = "Model contributions", facet = FieldInfo.Facet.EXISTS, layout = FieldInfo.Layout.GROUP, type = FieldInfo.Type.TEXT, overview = true)
    private List<TargetInternalReference> modelContributions;

    @JsonProperty("first_release")
    @FieldInfo(label = "First release", ignoreForSearch = true, visible = false, type=FieldInfo.Type.DATE)
    private ISODateValue firstRelease;

    @JsonProperty("last_release")
    @FieldInfo(label = "Last release", ignoreForSearch = true, visible = false, type=FieldInfo.Type.DATE)
    private ISODateValue lastRelease;

    @Override
    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public void setType(String type) {
        setType(StringUtils.isBlank(type) ? null : new Value<>(type));
    }

    public void setIdentifier(List<String> identifier) {
        this.identifier = identifier;
    }

    public void setEditorId(String editorId){
        setEditorId(StringUtils.isBlank(editorId) ? null : new Value<>(editorId));
    }

    public void setTitle(String title){
        setTitle(StringUtils.isBlank(title) ? null : new Value<>(title));
    }

    @Override
    public List<String> getIdentifier() {
        return identifier;
    }

    public Value<String> getEditorId() {
        return editorId;
    }

    public void setEditorId(Value<String> editorId) {
        this.editorId = editorId;
    }

    public List<TargetInternalReference> getCustodianOfModel() {
        return custodianOfModel;
    }

    public void setCustodianOfModel(List<TargetInternalReference> custodianOfModel) {
        this.custodianOfModel = custodianOfModel;
    }

    public List<Value<String>> getPublications() {
        return publications;
    }

    public void setPublications(List<String> publications) {
        this.publications = publications != null ? publications.stream().map(Value::new).collect(Collectors.toList()) : null;
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

    public List<TargetInternalReference> getCustodianOf() {
        return custodianOf;
    }

    public void setCustodianOf(List<TargetInternalReference> custodianOf) {
        this.custodianOf = custodianOf;
    }

    public List<TargetInternalReference> getContributions() {
        return contributions;
    }

    public void setContributions(List<TargetInternalReference> contributions) {
        this.contributions = contributions;
    }

    public List<TargetInternalReference> getModelContributions() {
        return modelContributions;
    }

    public void setModelContributions(List<TargetInternalReference> modelContributions) {
        this.modelContributions = modelContributions;
    }

    public Value<String> getTitle() {
        return title;
    }

    public void setTitle(Value<String> title) {
        this.title = title;
    }

    public Value<String> getType() {
        return type;
    }

    public void setType(Value<String> type) {
        this.type = type;
    }
}
