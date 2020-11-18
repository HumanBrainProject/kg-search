package eu.ebrains.kg.search.model.source.openMINDSv1;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ebrains.kg.search.model.source.commons.Publication;
import eu.ebrains.kg.search.model.source.commons.Reference;
import eu.ebrains.kg.search.model.source.commons.Subject;

import java.util.Date;
import java.util.List;

public class DatasetV1 {
    private List<String> methods;
    private Date lastReleaseAt;
    private List<Reference> component;
    private List<String> embargoRestrictedAccess;
    @JsonProperty("external_datalink") //TODO: Capitalize the property
    private String externalDatalink;
    private List<String> embargoForFilter;
    private List<String> doi;
    private List<File> files;
    private List<String> parcellationAtlas;
    private List<ExternalReference> neuroglancer;
    private List<Publication> publications;
    private List<Subject> subjects;
    private List<Reference> contributors;
    private String identifier;
    private List<String> speciesFilter;
    private Date firstReleaseAt;
    private List<String> citation;
    private List<ExternalReference> brainViewer;
    private List<String> preparation;
    private String title;
    private String editorId;
    private List<Reference> owners;
    private Boolean containerUrlAsZIP;
    private List<String> protocols;
    @JsonProperty("container_url")
    private String containerUrl;
    private String dataDescriptorURL;
    private List<ParcellationRegion> parcellationRegion;
    private String description;
    private List<ExternalReference> license;
    private List<String> modalityForFilter;
    private List<String> embargo;

    public static class File {
        @JsonProperty("absolute_path")
        private String absolutePath;
        @JsonProperty("human_readable_size")
        private String humanReadableSize;
        @JsonProperty("static_image_url")
        private List<String> staticImageUrl;
        @JsonProperty("is_preview_animated")
        private Boolean isPreviewAnimated;
        private String name;
        @JsonProperty("preview_url")
        private List<String> previewUrl;
        @JsonProperty("private_access")
        private Boolean privateAccess;
        @JsonProperty("thumbnail_url")
        private List<String> thumbnailUrl;

    }

    public static class ParcellationRegion {
        private String name;
        private String alias;
        private String url;

        public String getName() { return name; }

        public void setName(String name) { this.name = name; }

        public String getAlias() { return alias; }

        public void setAlias(String alias) { this.alias = alias; }

        public String getUrl() { return url; }

        public void setUrl(String url) { this.url = url; }
    }

    public static class ExternalReference {
        private String url;
        private String name;

        public String getUrl() { return url; }

        public void setUrl(String url) { this.url = url; }

        public String getName() { return name; }

        public void setName(String name) { this.name = name; }

    }
}
