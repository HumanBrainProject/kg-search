/*
 *  Copyright 2018 - 2021 Swiss Federal Institute of Technology Lausanne (EPFL)
 *  Copyright 2021 - 2023 EBRAINS AISBL
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0.
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  This open source software code was developed in part or in whole in the
 *  Human Brain Project, funded from the European Union's Horizon 2020
 *  Framework Programme for Research and Innovation under
 *  Specific Grant Agreements No. 720270, No. 785907, and No. 945539
 *  (Human Brain Project SGA1, SGA2 and SGA3).
 */

package eu.ebrains.kg.projects.ebrains.translators.utils;

import eu.ebrains.kg.projects.ebrains.EBRAINSTranslatorUtils;
import eu.ebrains.kg.projects.ebrains.source.DatasetVersionV3;
import eu.ebrains.kg.projects.ebrains.source.commons.PersonOrOrganizationRef;
import eu.ebrains.kg.projects.ebrains.target.schemaorg.SchemaOrgDataset;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SchemaOrgConverter {

    public static SchemaOrgDataset translateDatasetVersion(DatasetVersionV3 datasetVersion){
        SchemaOrgDataset schemaOrgDataset = new SchemaOrgDataset();
        List<PersonOrOrganizationRef> authors = datasetVersion.getAuthor();
        if(CollectionUtils.isEmpty(authors)){
            authors = datasetVersion.getDataset().getAuthor();
        }
        schemaOrgDataset.setName(StringUtils.isNotBlank(datasetVersion.getFullName()) ? datasetVersion.getFullName() : datasetVersion.getDataset().getFullName());
        schemaOrgDataset.setDescription(StringUtils.isNotBlank(datasetVersion.getDescription()) ? datasetVersion.getDescription() : datasetVersion.getDataset().getDescription());
            schemaOrgDataset.setVersion(datasetVersion.getVersion());
            List<String> identifiers = new ArrayList<>(datasetVersion.getIdentifier());
        if(StringUtils.isNotBlank(datasetVersion.getDoi())) {
            schemaOrgDataset.setIdentifier(identifiers);
            identifiers.add(datasetVersion.getDoi());
        }
        schemaOrgDataset.setCreator(authors.stream().map(a -> {
            if(a.getFamilyName()!=null){
                final SchemaOrgDataset.Person person = new SchemaOrgDataset.Person();
                person.setFamilyName(a.getFamilyName());
                person.setGivenName(a.getGivenName());
                person.setName(EBRAINSTranslatorUtils.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName()));
                return person;
            }
            else if(a.getFullName()!=null){
                SchemaOrgDataset.Organization organization = new SchemaOrgDataset.Organization();
                organization.setName(a.getFullName());
                return organization;
            }
            return null;
        }).collect(Collectors.toList()));
        return schemaOrgDataset;
    }

}
