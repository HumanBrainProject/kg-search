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
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.FullNameRef;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.PersonOrOrganizationRef;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.SourceInternalReference;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Versions;

import java.util.Date;
import java.util.List;

public class DatasetVersionV3 extends SourceInstanceV3 {
    private String doi;
    private String howToCite;
    private String description;
    private String fullName;
    private List<String> homepage;
    private List<String> keyword;
    private String version;
    private String versionInnovation;
    private Date releaseDate;
    private License license;
    private List<PersonOrOrganizationRef> author;
    private List<FullNameRef> projects;
    private List<PersonOrOrganizationRef> custodians;
    private List<Specimen> studiedSpecimen;
    private DatasetVersions dataset;
    private List<String> fullDocumentationUrl;
    private List<String> fullDocumentationDOI;
    private List<Protocol> protocols;
    private NameWithIdentifier accessibility;
    private FileRepository fileRepository;
    private List<String> experimentalApproach;

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

    public List<String> getHomepage() {
        return homepage;
    }

    public void setHomepage(List<String> homepage) {
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

    public List<PersonOrOrganizationRef> getAuthor() {
        return author;
    }

    public void setAuthor(List<PersonOrOrganizationRef> author) {
        this.author = author;
    }

    public DatasetVersions getDataset() {
        return dataset;
    }

    public void setDataset(DatasetVersions dataset) {
        this.dataset = dataset;
    }

    public List<FullNameRef> getProjects() {
        return projects;
    }

    public void setProjects(List<FullNameRef> projects) {
        this.projects = projects;
    }

    public License getLicense() {
        return license;
    }

    public void setLicense(License license) {
        this.license = license;
    }

    public List<PersonOrOrganizationRef> getCustodians() {
        return custodians;
    }

    public void setCustodians(List<PersonOrOrganizationRef> custodians) {
        this.custodians = custodians;
    }

    public List<Specimen> getStudiedSpecimen() {
        return studiedSpecimen;
    }

    public void setStudiedSpecimen(List<Specimen> studiedSpecimen) {
        this.studiedSpecimen = studiedSpecimen;
    }

    public List<String> getFullDocumentationUrl() {
        return fullDocumentationUrl;
    }

    public void setFullDocumentationUrl(List<String> fullDocumentationUrl) {
        this.fullDocumentationUrl = fullDocumentationUrl;
    }

    public List<String> getFullDocumentationDOI() {
        return fullDocumentationDOI;
    }

    public void setFullDocumentationDOI(List<String> fullDocumentationDOI) {
        this.fullDocumentationDOI = fullDocumentationDOI;
    }

    public List<Protocol> getProtocols() {
        return protocols;
    }

    public void setProtocols(List<Protocol> protocols) {
        this.protocols = protocols;
    }

    public NameWithIdentifier getAccessibility() {
        return accessibility;
    }

    public void setAccessibility(NameWithIdentifier accessibility) {
        this.accessibility = accessibility;
    }

    public void setFileRepository(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public FileRepository getFileRepository() {
        return fileRepository;
    }

    public List<String> getExperimentalApproach() {
        return experimentalApproach;
    }

    public void setExperimentalApproach(List<String> experimentalApproach) {
        this.experimentalApproach = experimentalApproach;
    }

    public static class Specimen {
        private String specimenId;
        private String species;
        private List<Age> ages;
        private List<String> biologicalSex;

        public String getSpecimenId() {
            return specimenId;
        }

        public void setSpecimenId(String specimenId) {
            this.specimenId = specimenId;
        }

        public String getSpecies() {
            return species;
        }

        public void setSpecies(String species) {
            this.species = species;
        }

        public List<Age> getAges() {
            return ages;
        }

        public void setAges(List<Age> ages) {
            this.ages = ages;
        }

        public List<String> getBiologicalSex() {
            return biologicalSex;
        }

        public void setBiologicalSex(List<String> biologicalSex) {
            this.biologicalSex = biologicalSex;
        }
    }

    public static class Age{
        private Double value;
        private Double maxValue;
        private Double minValue;
        private String unit;

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }

        public Double getMaxValue() {
            return maxValue;
        }

        public void setMaxValue(Double maxValue) {
            this.maxValue = maxValue;
        }

        public Double getMinValue() {
            return minValue;
        }

        public void setMinValue(Double minValue) {
            this.minValue = minValue;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }
    }

    public static class License {
        private String fullName;
        private String legalCode;

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getLegalCode() {
            return legalCode;
        }

        public void setLegalCode(String legalCode) {
            this.legalCode = legalCode;
        }
    }

    public static class NameWithIdentifier {
        private List<String> identifier;
        private String name;

        public List<String> getIdentifier() {
            return identifier;
        }

        public void setIdentifier(List<String> identifier) {
            this.identifier = identifier;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class OntologicalTerm {
        private String ontologyIdentifier;
        private String name;

        public String getOntologyIdentifier() {
            return ontologyIdentifier;
        }

        public void setOntologyIdentifier(String ontologyIdentifier) {
            this.ontologyIdentifier = ontologyIdentifier;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class FileRepository extends SourceInternalReference{
        private String iri;

        public String getIri() {
            return iri;
        }

        public void setIri(String iri) {
            this.iri = iri;
        }
    }

    public static class Protocol {
        private List<OntologicalTerm> technique;
        private List<OntologicalTerm> behavioralTask;
        private List<String> studyOption;

        public List<OntologicalTerm> getTechnique() {
            return technique;
        }

        public void setTechnique(List<OntologicalTerm> technique) {
            this.technique = technique;
        }

        public List<OntologicalTerm> getBehavioralTask() {
            return behavioralTask;
        }

        public void setBehavioralTask(List<OntologicalTerm> behavioralTask) {
            this.behavioralTask = behavioralTask;
        }

        public List<String> getStudyOption() {
            return studyOption;
        }

        public void setStudyOption(List<String> studyOption) {
            this.studyOption = studyOption;
        }
    }

    public static class DatasetVersions extends Versions {

        @JsonProperty("datasetAuthor")
        private List<PersonOrOrganizationRef> author;

        @JsonProperty("datasetCustodian")
        private List<PersonOrOrganizationRef> custodians;

        public List<PersonOrOrganizationRef> getAuthor() {
            return author;
        }

        public void setAuthor(List<PersonOrOrganizationRef> author) {
            this.author = author;
        }

        public List<PersonOrOrganizationRef> getCustodians() {
            return custodians;
        }

        public void setCustodians(List<PersonOrOrganizationRef> custodians) {
            this.custodians = custodians;
        }
    }

}


