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

import java.util.List;

public class PersonOrOrganizationV3 extends SourceInstanceV3 {


    //Organization
    private String fullName;
    private List<DynamicContribution> otherContribution;
    private List<SimpleContribution> developer;
    private List<SimpleContribution> author;
    private List<SimpleContribution> custodian;


    //Person
    private String familyName;
    private String givenName;
    private List<CustodianOfModel> custodianOfModel;
    private List<CustodianOf> custodianOf;
    private List<RelatedPublication> publications;
    private List<ModelContribution> modelContributions;
    private List<Contribution> contributions;

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public List<CustodianOfModel> getCustodianOfModel() {
        return custodianOfModel;
    }

    public void setCustodianOfModel(List<CustodianOfModel> custodianOfModel) {
        this.custodianOfModel = custodianOfModel;
    }

    public List<CustodianOf> getCustodianOf() {
        return custodianOf;
    }

    public void setCustodianOf(List<CustodianOf> custodianOf) {
        this.custodianOf = custodianOf;
    }

    public List<RelatedPublication> getPublications() {
        return publications;
    }

    public void setPublications(List<RelatedPublication> publications) {
        this.publications = publications;
    }

    public List<ModelContribution> getModelContributions() {
        return modelContributions;
    }

    public void setModelContributions(List<ModelContribution> modelContributions) {
        this.modelContributions = modelContributions;
    }

    public List<Contribution> getContributions() {
        return contributions;
    }

    public void setContributions(List<Contribution> contributions) {
        this.contributions = contributions;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public List<DynamicContribution> getOtherContribution() {
        return otherContribution;
    }

    public void setOtherContribution(List<DynamicContribution> otherContribution) {
        this.otherContribution = otherContribution;
    }

    public List<SimpleContribution> getDeveloper() {
        return developer;
    }

    public void setDeveloper(List<SimpleContribution> developer) {
        this.developer = developer;
    }

    public List<SimpleContribution> getAuthor() {
        return author;
    }

    public void setAuthor(List<SimpleContribution> author) {
        this.author = author;
    }

    public List<SimpleContribution> getCustodian() {
        return custodian;
    }

    public void setCustodian(List<SimpleContribution> custodian) {
        this.custodian = custodian;
    }

    public static class CustodianOfModel {
        private String id;
        private List<String> identifier;
        private String name;
        private List<String> hasComponent;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

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

        public List<String> getHasComponent() {
            return hasComponent;
        }

        public void setHasComponent(List<String> hasComponent) {
            this.hasComponent = hasComponent;
        }
    }

    public static class CustodianOf {
        private String id;
        private List<String> identifier;
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

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

    public static class RelatedPublication {
        private String citation;
        private String doi;

        public String getCitation() {
            return citation;
        }

        public void setCitation(String citation) {
            this.citation = citation;
        }

        public String getDoi() {
            return doi;
        }

        public void setDoi(String doi) {
            this.doi = doi;
        }
    }

    public static class ModelContribution {
        private String id;
        private List<String> identifier;
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

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

    public static class Contribution {
        private String id;
        private List<String> identifier;
        private String name;
        List<String> datasetComponent;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

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

        public List<String> getDatasetComponent() {
            return datasetComponent;
        }

        public void setDatasetComponent(List<String> datasetComponent) {
            this.datasetComponent = datasetComponent;
        }
    }

    public static class DynamicContribution{
        private List<String> contributionTypes;
        private SimpleContribution otherContribution;

        public List<String> getContributionTypes() {
            return contributionTypes;
        }

        public void setContributionTypes(List<String> contributionTypes) {
            this.contributionTypes = contributionTypes;
        }

        public SimpleContribution getOtherContribution() {
            return otherContribution;
        }

        public void setOtherContribution(SimpleContribution otherContribution) {
            this.otherContribution = otherContribution;
        }
    }


    public static class SimpleContribution{
        private String id;
        private String fullName;
        private String fallbackName;
        private List<String> type;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getFallbackName() {
            return fallbackName;
        }

        public void setFallbackName(String fallbackName) {
            this.fallbackName = fallbackName;
        }

        public List<String> getType() {
            return type;
        }
    }

}
