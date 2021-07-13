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
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Value;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@MetaInfo(name = "FileRepository", identifier = "https://openminds.ebrains.eu/core/FileRepository", order = 11)
public class FileRepository implements TargetInstance {
    @ElasticSearchInfo(type = "keyword")
    private final Value<String> type = new Value<>("FileRepository");

    @FieldInfo(ignoreForSearch = true, visible = false)
    private String id;

    @ElasticSearchInfo(type = "keyword")
    @FieldInfo(ignoreForSearch = true, visible = false)
    private List<String> identifier;

    @FieldInfo(label = "File repository", layout = FieldInfo.Layout.HEADER)
    private String IRI;

    @FieldInfo(label = "Dataset")
    private TargetInternalReference datasetVersion;

    @FieldInfo(label = "(Meta) Data Model Version")
    private TargetInternalReference metaDataModelVersion;

    @FieldInfo(label = "Model Version")
    private TargetInternalReference modelVersion;

    @FieldInfo(label = "Software Version")
    private TargetInternalReference softwareVersion;

    @FieldInfo(label = "Files")
    private Value<String> embargo;

    @FieldInfo(label = "Files", isHierarchicalFiles = true, isAsync=true)
    private String filesAsyncUrl;

    @FieldInfo(label= "Access Files", markdown = true, isButton = true, termsOfUse = true, icon="unlock")
    private Value<String> useHDG;

    @JsonProperty("first_release")
    @FieldInfo(label = "First release", ignoreForSearch = true, visible = false, type = FieldInfo.Type.DATE)
    private ISODateValue firstRelease;

    @JsonProperty("last_release")
    @FieldInfo(label = "Last release", ignoreForSearch = true, visible = false, type = FieldInfo.Type.DATE)
    private ISODateValue lastRelease;

    public Value<String> getType() { return type; }

    @Override
    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    @Override
    public List<String> getIdentifier() { return identifier; }

    public void setIdentifier(List<String> identifier) { this.identifier = identifier; }

    @Override
    @JsonIgnore
    public boolean isSearchable() { return false; }

    public Value<String> getEmbargo() {
        return embargo;
    }

    public void setEmbargo(Value<String> embargo) {
        this.embargo = embargo;
    }

    public void setEmbargo(String embargo) {
        setEmbargo(StringUtils.isBlank(embargo) ? null : new Value<>(embargo));
    }

    public Value<String> getUseHDG() {
        return useHDG;
    }

    public void setUseHDG(Value<String> useHDG) {
        this.useHDG = useHDG;
    }

    public void setUseHDG(String useHDG) {
        setUseHDG(StringUtils.isBlank(useHDG) ? null : new Value<>(useHDG));
    }

    public String getIRI() { return IRI; }

    public void setIRI(String IRI) { this.IRI = IRI; }

    public TargetInternalReference getDatasetVersion() {
        return datasetVersion;
    }

    public void setDatasetVersion(TargetInternalReference datasetVersion) {
        this.datasetVersion = datasetVersion;
    }

    public TargetInternalReference getMetaDataModelVersion() {
        return metaDataModelVersion;
    }

    public void setMetaDataModelVersion(TargetInternalReference metaDataModelVersion) {
        this.metaDataModelVersion = metaDataModelVersion;
    }

    public TargetInternalReference getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(TargetInternalReference modelVersion) {
        this.modelVersion = modelVersion;
    }

    public TargetInternalReference getSoftwareVersion() {
        return softwareVersion;
    }

    public void setSoftwareVersion(TargetInternalReference softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    public String getFilesAsyncUrl() {
        return filesAsyncUrl;
    }

    public void setFilesAsyncUrl(String filesAsyncUrl) {
        this.filesAsyncUrl = filesAsyncUrl;
    }

}
