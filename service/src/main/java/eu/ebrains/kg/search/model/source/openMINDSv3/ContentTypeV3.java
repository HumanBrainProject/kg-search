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

import eu.ebrains.kg.search.model.source.openMINDSv3.commons.FullNameRef;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.FullNameRefForResearchProductVersion;

import java.util.List;

public class ContentTypeV3 extends SourceInstanceV3 {

    private String name;
    private String description;
    private List<String> fileExtension;
    private String relatedMediaType;
    private String specification;
    private List<String> synonym;
    private FullNameRef dataType;
    private List<FullNameRef> inputFormat;
    private List<FullNameRef> outputFormat;
    private List<ResearchProduct> researchProducts;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(List<String> fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getRelatedMediaType() {
        return relatedMediaType;
    }

    public void setRelatedMediaType(String relatedMediaType) {
        this.relatedMediaType = relatedMediaType;
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public List<String> getSynonym() {
        return synonym;
    }

    public void setSynonym(List<String> synonym) {
        this.synonym = synonym;
    }

    public FullNameRef getDataType() {
        return dataType;
    }

    public void setDataType(FullNameRef dataType) {
        this.dataType = dataType;
    }

    public List<FullNameRef> getInputFormat() {
        return inputFormat;
    }

    public void setInputFormat(List<FullNameRef> inputFormat) {
        this.inputFormat = inputFormat;
    }

    public List<FullNameRef> getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(List<FullNameRef> outputFormat) {
        this.outputFormat = outputFormat;
    }

    public List<ResearchProduct> getResearchProducts() {
        return researchProducts;
    }

    public void setResearchProducts(List<ResearchProduct> researchProducts) {
        this.researchProducts = researchProducts;
    }

    public static class ResearchProduct extends FullNameRefForResearchProductVersion {

        private List<String> type;

        public List<String> getType() {
            return type;
        }

        public void setType(List<String> type) {
            this.type = type;
        }
    }



}


