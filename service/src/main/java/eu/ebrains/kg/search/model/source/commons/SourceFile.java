package eu.ebrains.kg.search.model.source.commons;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SourceFile {

    public SourceFile() {}

    public SourceFile(String name, String absolutePath, String humanReadableSize ) {
        this.name = name;
        this.absolutePath = absolutePath;
        this.humanReadableSize = humanReadableSize;
    }
    public SourceFile(String name, String absolutePath, String humanReadableSize, List<String> staticImageUrl, Boolean isPreviewAnimated, List<String> previewUrl, Boolean privateAccess, List<String> thumbnailUrl) {
        this.name = name;
        this.absolutePath = absolutePath;
        this.humanReadableSize = humanReadableSize;
        this.staticImageUrl = staticImageUrl;
        this.isPreviewAnimated = isPreviewAnimated;
        this.previewUrl = previewUrl;
        this.privateAccess = privateAccess;
        this.thumbnailUrl = thumbnailUrl;
    }

    private String name;

    @JsonProperty("absolute_path")
    private String absolutePath;

    @JsonProperty("human_readable_size")
    private String humanReadableSize;

    @JsonProperty("static_image_url")
    private List<String> staticImageUrl;

    @JsonProperty("is_preview_animated")
    private Boolean isPreviewAnimated;

    @JsonProperty("preview_url")
    private List<String> previewUrl;

    @JsonProperty("private_access")
    private Boolean privateAccess;

    @JsonProperty("thumbnail_url")
    private List<String> thumbnailUrl;

    public String getAbsolutePath() { return absolutePath; }

    public void setAbsolutePath(String absolutePath) { this.absolutePath = absolutePath; }

    public String getHumanReadableSize() { return humanReadableSize; }

    public void setHumanReadableSize(String humanReadableSize) { this.humanReadableSize = humanReadableSize; }

    public List<String> getStaticImageUrl() { return staticImageUrl; }

    public void setStaticImageUrl(List<String> staticImageUrl) { this.staticImageUrl = staticImageUrl; }

    public Boolean getPreviewAnimated() { return isPreviewAnimated; }

    public void setPreviewAnimated(Boolean previewAnimated) { isPreviewAnimated = previewAnimated; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public List<String> getPreviewUrl() { return previewUrl; }

    public void setPreviewUrl(List<String> previewUrl) { this.previewUrl = previewUrl; }

    public Boolean getPrivateAccess() { return privateAccess; }

    public void setPrivateAccess(Boolean privateAccess) { this.privateAccess = privateAccess; }

    public List<String> getThumbnailUrl() { return thumbnailUrl; }

    public void setThumbnailUrl(List<String> thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

}
