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

package eu.ebrains.kg.search.model.source.openMINDSv3;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Author;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.SourceInternalReference;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Versions;

import java.util.Date;
import java.util.List;

public class DatasetVersionV3 extends SourceInstanceV3 {
    private String doi;
    private String howToCite;
    private String description;
    private String fullName;
    private String homepage;
    private List<String> keyword;
    private Date releaseDate;
    private String version;
    private String versionInnovation;
    private List<Author> author;
    private DatasetVersions dataset;
    private SourceInternalReference fileRepository;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public String getHowToCite() {
        return howToCite;
    }

    public void setHowToCite(String howToCite) {
        this.howToCite = howToCite;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public List<String> getKeyword() {
        return keyword;
    }

    public void setKeyword(List<String> keyword) {
        this.keyword = keyword;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersionInnovation() {
        return versionInnovation;
    }

    public void setVersionInnovation(String versionInnovation) {
        this.versionInnovation = versionInnovation;
    }

    public List<Author> getAuthor() {
        return author;
    }

    public void setAuthor(List<Author> author) {
        this.author = author;
    }

    public DatasetVersions getDataset() {
        return dataset;
    }

    public void setDataset(DatasetVersions dataset) {
        this.dataset = dataset;
    }

    public SourceInternalReference getFileRepository() {
        return fileRepository;
    }

    public void setFileRepository(SourceInternalReference fileRepository) {
        this.fileRepository = fileRepository;
    }

    public static class DatasetVersions extends Versions {

        @JsonProperty("datasetAuthor")
        private List<Author> author;

        public List<Author> getAuthor() {
            return author;
        }

        public void setAuthor(List<Author> author) {
            this.author = author;
        }
    }

}


