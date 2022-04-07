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

package eu.ebrains.kg.search.controller.translators.kgv3;

import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.ResultsOfKGv3;
import eu.ebrains.kg.search.model.source.openMINDSv3.ContentTypeV3;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.ContentType;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Value;
import eu.ebrains.kg.search.services.DOICitationFormatter;
import eu.ebrains.kg.search.utils.IdUtils;
import eu.ebrains.kg.search.utils.TranslationException;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ContentTypeV3Translator extends TranslatorV3<ContentTypeV3, ContentType, ContentTypeV3Translator.Result> {
    private static final String QUERY_ID = "fba5b6dc-b047-4535-b3d2-8901fce8574b";

    @Override
    public List<String> getQueryIds() {
        return Collections.singletonList(QUERY_ID);
    }

    @Override
    public Class<ContentTypeV3Translator.Result> getResultType() {
        return ContentTypeV3Translator.Result.class;
    }

    @Override
    public Class<ContentTypeV3> getSourceType() {
        return ContentTypeV3.class;
    }

    @Override
    public Class<ContentType> getTargetType() {
        return ContentType.class;
    }

    @Override
    public List<String> semanticTypes() {
        return Collections.singletonList("https://openminds.ebrains.eu/core/ContentType");
    }

    public static class Result extends ResultsOfKGv3<ContentTypeV3> {
    }

    public ContentType translate(ContentTypeV3 contentTypeV3, DataStage dataStage, boolean liveMode, DOICitationFormatter doiCitationFormatter) throws TranslationException {
       ContentType c = new ContentType();

        c.setCategory(new Value<>("ContentType"));
        c.setDisclaimer(new Value<>("Please alert us at [curation-support@ebrains.eu](mailto:curation-support@ebrains.eu) for errors or quality concerns regarding the dataset, so we can forward this information to the Data Custodian responsible."));

        c.setId(IdUtils.getUUID(contentTypeV3.getId()));
       c.setAllIdentifiers(contentTypeV3.getIdentifier());
       c.setIdentifier(IdUtils.getUUID(contentTypeV3.getIdentifier()).stream().distinct().collect(Collectors.toList()));
       c.setTitle(value(contentTypeV3.getName()));
       c.setDescription(value(contentTypeV3.getDescription()));
       c.setFileExtensions(value(contentTypeV3.getFileExtension()));
       c.setRelatedMediaType(link(contentTypeV3.getRelatedMediaType()));
       c.setSpecification(value(contentTypeV3.getSpecification()));
       c.setSynonyms(value(contentTypeV3.getSynonym()));
       c.setDataType(ref(contentTypeV3.getDataType()));
       c.setInputFormatForSoftware(refVersion(contentTypeV3.getInputFormat(), true));
       if(c.getInputFormatForSoftware()!=null){
           Collections.sort(c.getInputFormatForSoftware());
       }
       c.setOutputFormatForSoftware(refVersion(contentTypeV3.getOutputFormat(), true));
       if(c.getOutputFormatForSoftware()!=null){
           Collections.sort(c.getOutputFormatForSoftware());
       }
       if(!CollectionUtils.isEmpty(contentTypeV3.getResearchProducts())){
           final List<TargetInternalReference> datasets = contentTypeV3.getResearchProducts().stream().filter(r -> r.getType().contains("https://openminds.ebrains.eu/core/DatasetVersion")).map(this::ref).sorted(Comparator.comparing(TargetInternalReference::getValue)).collect(Collectors.toList());
           if(!datasets.isEmpty()){
               c.setDatasets(datasets);
           }
           final List<TargetInternalReference> models = contentTypeV3.getResearchProducts().stream().filter(r -> r.getType().contains("https://openminds.ebrains.eu/core/ModelVersion")).map(this::ref).sorted(Comparator.comparing(TargetInternalReference::getValue)).collect(Collectors.toList());
           if(!models.isEmpty()){
               c.setModels(models);
           }
       }
       return c;
    }
}
