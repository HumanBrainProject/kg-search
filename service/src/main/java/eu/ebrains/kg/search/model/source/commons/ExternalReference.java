package eu.ebrains.kg.search.model.source.commons;

public class ExternalReference {
        public ExternalReference() {}

        public ExternalReference(String url) {
            this.url = url;
        }

        public ExternalReference(String url, String name) {
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
