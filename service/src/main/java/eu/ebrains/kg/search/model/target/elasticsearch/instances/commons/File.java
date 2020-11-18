package eu.ebrains.kg.search.model.target.elasticsearch.instances.commons;

public class File {
        public File() {}

        public File(String url, String value, String fileSize) {
            this.url = url;
            this.value = value;
            this.fileSize = fileSize;
        }

        private String url;
        private String value;
        private String fileSize;

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
}
