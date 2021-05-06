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

import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.MetaInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.TargetInstance;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetExternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Value;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@MetaInfo(name = "ModelVersions", identifier = "https://openminds.ebrains.eu/core/Model", order = 2)
public class Model implements TargetInstance {
    @ElasticSearchInfo(type = "keyword")
    private final Value<String> type = new Value<>("ModelVersions");

    @FieldInfo(ignoreForSearch = true, visible = false)
    private String id;

    @ElasticSearchInfo(type = "keyword")
    @FieldInfo(ignoreForSearch = true, visible = false)
    private List<String> identifier;

    @FieldInfo(label = "Name", sort = true, boost = 20)
    private Value<String> title;

    @FieldInfo(label = "Description", markdown = true, boost = 2, labelHidden = true)
    private Value<String> description;

    @FieldInfo(label = "Cite model", isButton = true, markdown = true, icon="quote-left")
    private Value<String> citation;

    @FieldInfo(label = "DOI", hint = "This is the model DOI representing all the underlying model's versions you must cite if you reuse this data in a way that leads to a publication")
    private Value<String> doi;

    @FieldInfo(label = "Homepage")
    private TargetExternalReference homepage;

    @FieldInfo(label = "Study target", layout = FieldInfo.Layout.SUMMARY)
    private List<Value<String>> studyTarget;

    @FieldInfo(label = "Scope", layout = FieldInfo.Layout.SUMMARY, facet = FieldInfo.Facet.LIST)
    private List<Value<String>> scope;

    @FieldInfo(label = "Abstraction level", layout = FieldInfo.Layout.SUMMARY, facet = FieldInfo.Facet.LIST)
    private List<Value<String>> abstractionLevel;

    @FieldInfo(label = "Contributors", layout = FieldInfo.Layout.HEADER, separator = "; ", type = FieldInfo.Type.TEXT, labelHidden = true, boost = 10)
    private List<TargetInternalReference> contributors;

    @FieldInfo(label = "Custodian", layout = FieldInfo.Layout.SUMMARY, separator = "; ", type = FieldInfo.Type.TEXT, hint = "A custodian is the person responsible for the data bundle.")
    private List<TargetInternalReference> owners;

    @FieldInfo(label = "Versions")
    private List<TargetInternalReference> versions;

    @Override
    public boolean isSearchable() {
        return false;
    }

    public void setTitle(String title) {
        setTitle(StringUtils.isBlank(title) ? null : new Value<>(title));
    }

    public void setDescription(String description) {
        setDescription(StringUtils.isBlank(description) ? null : new Value<>(description));
    }

    public void setCitation(String citation) {
        setCitation(StringUtils.isBlank(citation) ? null : new Value<>(citation));
    }

    public void setDoi(String doi) {
        setDoi(StringUtils.isBlank(doi) ? null : new Value<>(doi));
    }

    public Value<String> getType() {
        return type;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public List<String> getIdentifier() {
        return identifier;
    }

    public void setIdentifier(List<String> identifier) {
        this.identifier = identifier;
    }

    public Value<String> getTitle() {
        return title;
    }

    public void setTitle(Value<String> title) {
        this.title = title;
    }

    public Value<String> getDescription() {
        return description;
    }

    public void setDescription(Value<String> description) {
        this.description = description;
    }

    public Value<String> getCitation() {
        return citation;
    }

    public void setCitation(Value<String> citation) {
        this.citation = citation;
    }

    public Value<String> getDoi() {
        return doi;
    }

    public void setDoi(Value<String> doi) {
        this.doi = doi;
    }

    public TargetExternalReference getHomepage() {
        return homepage;
    }

    public void setHomepage(TargetExternalReference homepage) {
        this.homepage = homepage;
    }

    public List<Value<String>> getStudyTarget() {
        return studyTarget;
    }

    public void setStudyTarget(List<String> studyTarget) {
        this.studyTarget = studyTarget == null ? null : studyTarget.stream().map(Value::new).collect(Collectors.toList());
    }

    public List<Value<String>> getScope() {
        return scope;
    }

    public void setScope(List<String> scope) {
        this.scope = scope == null ? null : scope.stream().map(Value::new).collect(Collectors.toList());
    }

    public List<Value<String>> getAbstractionLevel() {
        return abstractionLevel;
    }

    public void setAbstractionLevel(List<String> abstractionLevel) {
        this.abstractionLevel = abstractionLevel == null ? null : abstractionLevel.stream().map(Value::new).collect(Collectors.toList());
    }

    public List<TargetInternalReference> getContributors() {
        return contributors;
    }

    public void setContributors(List<TargetInternalReference> contributors) {
        this.contributors = contributors;
    }

    public List<TargetInternalReference> getOwners() {
        return owners;
    }

    public void setOwners(List<TargetInternalReference> owners) {
        this.owners = owners;
    }

    public List<TargetInternalReference> getVersions() {
        return versions;
    }

    public void setVersions(List<TargetInternalReference> versions) {
        this.versions = versions;
    }
}
