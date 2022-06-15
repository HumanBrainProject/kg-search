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

package eu.ebrains.kg.common.model.source.openMINDSv1;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ebrains.kg.common.model.source.commonsV1andV2.HasEmbargo;
import eu.ebrains.kg.common.model.source.commonsV1andV2.ParcellationRegion;
import eu.ebrains.kg.common.model.source.commonsV1andV2.SpecimenGroup;
import eu.ebrains.kg.common.model.source.commonsV1andV2.Subject;
import eu.ebrains.kg.common.model.source.SourceInstanceV1andV2;

import java.util.List;

public class SampleV1 extends SourceInstanceV1andV2 implements HasEmbargo {
    private String title;
    @JsonProperty("container_url") // TODO: get rid of _
    private String containerUrl;
    private String weightPreFixation;
    private List<SpecimenGroup> specimenGroups;
    private List<Subject> subjects;
    private List<String> methods;
    private List<String> brainViewer;
    private List<String> datasetExists;
    private List<SpecimenGroup> datasets;
    private List<ParcellationRegion> parcellationRegion;
    private List<String> parcellationAtlas;
    private List<SourceFile> files;
    private List<String> embargo;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContainerUrl() {
        return containerUrl;
    }

    public void setContainerUrl(String containerUrl) {
        this.containerUrl = containerUrl;
    }

    public String getWeightPreFixation() {
        return weightPreFixation;
    }

    public void setWeightPreFixation(String weightPreFixation) {
        this.weightPreFixation = weightPreFixation;
    }

    public List<SpecimenGroup> getSpecimenGroups() {
        return specimenGroups;
    }

    public void setSpecimenGroups(List<SpecimenGroup> specimenGroups) {
        this.specimenGroups = specimenGroups;
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<Subject> subjects) {
        this.subjects = subjects;
    }

    public List<String> getMethods() {
        return methods;
    }

    public void setMethods(List<String> methods) {
        this.methods = methods;
    }

    public List<String> getBrainViewer() {
        return brainViewer;
    }

    public void setBrainViewer(List<String> brainViewer) {
        this.brainViewer = brainViewer;
    }

    public List<String> getDatasetExists() {
        return datasetExists;
    }

    public void setDatasetExists(List<String> datasetExists) {
        this.datasetExists = datasetExists;
    }

    public List<SpecimenGroup> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<SpecimenGroup> datasets) {
        this.datasets = datasets;
    }

    public List<ParcellationRegion> getParcellationRegion() {
        return parcellationRegion;
    }

    public void setParcellationRegion(List<ParcellationRegion> parcellationRegion) {
        this.parcellationRegion = parcellationRegion;
    }

    public List<String> getParcellationAtlas() {
        return parcellationAtlas;
    }

    public void setParcellationAtlas(List<String> parcellationAtlas) {
        this.parcellationAtlas = parcellationAtlas;
    }

    public List<SourceFile> getFiles() {
        return files;
    }

    public void setFiles(List<SourceFile> files) {
        this.files = files;
    }

    @Override
    public List<String> getEmbargo() { return embargo; }

    public void setEmbargo(List<String> embargo) { this.embargo = embargo; }

    public static class SourceFile {
        private String name;

        private String absolutePath;

        @JsonProperty("human_readable_size")
        private String humanReadableSize;

        @JsonProperty("preview_url")
        private List<String> previewUrl;

        @JsonProperty("private_access")
        private boolean privateAccess;

        @JsonProperty("is_preview_animated")
        private List<Boolean> isPreviewAnimated;

        public String getAbsolutePath() { return absolutePath; }

        public void setAbsolutePath(String absolutePath) { this.absolutePath = absolutePath; }

        public String getHumanReadableSize() { return humanReadableSize; }

        public List<Boolean> getPreviewAnimated() { return isPreviewAnimated; }

        public void setPreviewAnimated(List<Boolean> previewAnimated) { isPreviewAnimated = previewAnimated; }


        public void setHumanReadableSize(String humanReadableSize) { this.humanReadableSize = humanReadableSize; }

        public String getName() { return name; }

        public void setName(String name) { this.name = name; }

        public List<String> getPreviewUrl() { return previewUrl; }

        public void setPreviewUrl(List<String> previewUrl) { this.previewUrl = previewUrl; }

        public boolean getPrivateAccess() { return privateAccess; }

        public void setPrivateAccess(Boolean privateAccess) { this.privateAccess = privateAccess; }

    }


}
