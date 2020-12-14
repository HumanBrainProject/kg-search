package eu.ebrains.kg.search.model.source.commons;

public class SourceExternalReference {
        public SourceExternalReference() {}

        public SourceExternalReference(String url) {
            this.url = url;
        }

        public SourceExternalReference(String url, String name) {
            this.url = url;
            this.name = name;
        }

        private String url;
        private String name;

        public String getUrl() { return url; }

        public void setUrl(String url) { this.url = url; }

        public String getName() { return name; }

        public void setName(String name) { this.name = name; }
}
