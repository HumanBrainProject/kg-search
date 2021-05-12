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

    public TargetFile(String url, String value, String fileSize, String format) {
        this.url = url;
        this.value = value;
        this.fileSize = fileSize;
        this.format = format;
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

    private String format;

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

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
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
