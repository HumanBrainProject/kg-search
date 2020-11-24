package eu.ebrains.kg.search.model.target.elasticsearch.instances.commons;

import com.fasterxml.jackson.annotation.JsonInclude;

public class TargetFile {
    public TargetFile() {
    }

    public TargetFile(String url, String value, String fileSize) {
        this.url = url;
        this.value = value;
        this.fileSize = fileSize;
    }

    public TargetFile(String url, String value, String fileSize, FileImage staticImageUrl, FileImage previewUrl, FileImage thumbnailUrl) {
        this.url = url;
        this.value = value;
        this.fileSize = fileSize;
        this.staticImageUrl = staticImageUrl;
        this.previewUrl = previewUrl;
        this.thumbnailUrl = thumbnailUrl;
    }

    private String url;
    private String value;
    private String fileSize;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private FileImage staticImageUrl;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private FileImage previewUrl;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private FileImage thumbnailUrl;

    public String getFileSize() { return fileSize; }

    public void setFileSize(String fileSize) { this.fileSize = fileSize; }

    public String getUrl() { return url; }

    public void setUrl(String url) { this.url = url; }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public FileImage getStaticImageUrl() { return staticImageUrl; }

    public void setStaticImageUrl(FileImage staticImageUrl) { this.staticImageUrl = staticImageUrl; }

    public FileImage getPreviewUrl() { return previewUrl; }

    public void setPreviewUrl(FileImage previewUrl) { this.previewUrl = previewUrl; }

    public FileImage getThumbnailUrl() { return thumbnailUrl; }

    public void setThumbnailUrl(FileImage thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public static class FileImage {
        public FileImage(){}

        public FileImage(String url, Boolean isAnimated) {
            this.url = url;
            this.isAnimated = isAnimated;
        }
        private Boolean isAnimated;
        private String url;

        public Boolean getAnimated() { return isAnimated; }

        public void setAnimated(Boolean animated) { isAnimated = animated; }

        public String getUrl() { return url; }

        public void setUrl(String url) { this.url = url; }
    }
}
