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

package eu.ebrains.kg.common.controller.translators.kgv3;

import eu.ebrains.kg.common.controller.translators.Helpers;
import eu.ebrains.kg.common.model.DataStage;
import eu.ebrains.kg.common.model.source.ResultsOfKGv3;
import eu.ebrains.kg.common.model.source.openMINDSv3.BehavioralProtocolV3;
import eu.ebrains.kg.common.model.source.openMINDSv3.ProtocolV3;
import eu.ebrains.kg.common.model.source.openMINDSv3.commons.FullNameRefForResearchProductVersion;
import eu.ebrains.kg.common.model.source.openMINDSv3.commons.RelatedPublication;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.BehavioralProtocol;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.Protocol;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.TargetExternalReference;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.Value;
import eu.ebrains.kg.common.utils.IdUtils;
import eu.ebrains.kg.common.utils.TranslationException;
import eu.ebrains.kg.common.utils.TranslatorUtils;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ProtocolV3Translator extends TranslatorV3<ProtocolV3, Protocol, ProtocolV3Translator.Result> {
    private static final String QUERY_ID = "a850a038-6861-4f46-a8e9-ef1ca1198efb";

    @Override
    public List<String> getQueryIds() {
        return Collections.singletonList(QUERY_ID);
    }

    @Override
    public Class<ProtocolV3Translator.Result> getResultType() {
        return ProtocolV3Translator.Result.class;
    }

    @Override
    public Class<ProtocolV3> getSourceType() {
        return ProtocolV3.class;
    }

    @Override
    public Class<Protocol> getTargetType() {
        return Protocol.class;
    }

    @Override
    public List<String> semanticTypes() {
        return Collections.singletonList("https://openminds.ebrains.eu/core/Protocol");
    }

    public static class Result extends ResultsOfKGv3<ProtocolV3> {
    }

    public Protocol translate(ProtocolV3 protocolV3, DataStage dataStage, boolean liveMode, TranslatorUtils translatorUtils) throws TranslationException {
       Protocol p = new Protocol();

       p.setCategory(new Value<>("Protocol"));
       p.setDisclaimer(new Value<>("Please alert us at [curation-support@ebrains.eu](mailto:curation-support@ebrains.eu) for errors or quality concerns, so we can forward this information to the custodian responsible."));
       p.setId(IdUtils.getUUID(protocolV3.getId()));
       p.setAllIdentifiers(protocolV3.getIdentifier());
       p.setIdentifier(IdUtils.getUUID(protocolV3.getIdentifier()).stream().distinct().collect(Collectors.toList()));
       p.setTitle(value(protocolV3.getName()));
       List<FullNameRefForResearchProductVersion> datasets = protocolV3.getDatasetsDirect();
       if(CollectionUtils.isEmpty(datasets)){
           datasets = protocolV3.getDatasetsByExecution();
       }
       p.setDatasets(refVersion(datasets, true));
       p.setDescription(value(protocolV3.getDescription()));
       if(protocolV3.getDescribedInDOI()!=null){
           p.setDescribedIn(value(Helpers.getFormattedDigitalIdentifier(translatorUtils.getDoiCitationFormatter(), protocolV3.getDescribedInDOI(), RelatedPublication.PublicationType.DOI)));
       }
       else if(protocolV3.getDescribedInFile()!=null && protocolV3.getDescribedInFile().getName()!=null && protocolV3.getDescribedInFile().getIri()!=null){
           p.setDescribedInLink(new TargetExternalReference(protocolV3.getDescribedInFile().getIri(), protocolV3.getDescribedInFile().getName()));
       }
       else if(protocolV3.getDescribedInUrl()!=null){
           p.setDescribedInLink(new TargetExternalReference(protocolV3.getDescribedInUrl(), protocolV3.getDescribedInUrl()));
       }
       p.setTechnique(ref(protocolV3.getTechnique()));
       p.setStimulusType(ref(protocolV3.getStimulusType()));
       p.setQueryBuilderText(value(TranslatorUtils.createQueryBuilderText(protocolV3.getPrimaryType(), p.getId())));
       return p;
    }
}
