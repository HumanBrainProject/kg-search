package eu.ebrains.kg.search.model.target.elasticsearch.instances.commons;

import com.fasterxml.jackson.annotation.JsonInclude;
import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchInfo;

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


    @ElasticSearchInfo(ignoreAbove = 256)
    private String url;
    private String value;
    @ElasticSearchInfo(ignoreAbove = 256)
    private String fileSize;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private FileImage staticImageUrl;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private FileImage previewUrl;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private FileImage thumbnailUrl;

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public FileImage getStaticImageUrl() {
        return staticImageUrl;
    }

    public void setStaticImageUrl(FileImage staticImageUrl) {
        this.staticImageUrl = staticImageUrl;
    }

    public FileImage getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(FileImage previewUrl) {
        this.previewUrl = previewUrl;
    }

    public FileImage getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(FileImage thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public static class FileImage {
        public FileImage() {
        }

        public FileImage(boolean isAnimated, String url) {
            this.isAnimated = isAnimated;
            this.url = url;
        }

        private boolean isAnimated;

        @ElasticSearchInfo(ignoreAbove = 256)
        private String url;

        public boolean getIsAnimated() {
            return isAnimated;
        }

        public void setIsAnimated(boolean isAnimated) {
            this.isAnimated = isAnimated;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

    }
}
