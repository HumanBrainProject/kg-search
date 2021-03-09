package eu.ebrains.kg.search.model.source.openMINDSv1;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.List;

import eu.ebrains.kg.search.model.source.SourceInstance;
import eu.ebrains.kg.search.model.source.commons.*;

public class SampleV1 implements HasEmbargo, SourceInstance {
    private String id;
    private String identifier;
    private String editorId;
    private String title;
    @JsonProperty("container_url") // TODO: get rid of _
    private String containerUrl;
    private String weightPreFixation;
    private Date firstReleaseAt;
    private Date lastReleaseAt;
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

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getEditorId() {
        return editorId;
    }

    public void setEditorId(String editorId) {
        this.editorId = editorId;
    }

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

    public Date getFirstReleaseAt() {
        return firstReleaseAt;
    }

    public void setFirstReleaseAt(Date firstReleaseAt) {
        this.firstReleaseAt = firstReleaseAt;
    }

    public Date getLastReleaseAt() {
        return lastReleaseAt;
    }

    public void setLastReleaseAt(Date lastReleaseAt) {
        this.lastReleaseAt = lastReleaseAt;
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
