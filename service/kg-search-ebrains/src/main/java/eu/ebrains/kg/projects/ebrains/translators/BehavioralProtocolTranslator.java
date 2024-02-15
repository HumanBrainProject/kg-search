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

package eu.ebrains.kg.projects.ebrains.translators;

import eu.ebrains.kg.common.model.DataStage;
import eu.ebrains.kg.common.model.source.RelatedPublication;
import eu.ebrains.kg.common.model.source.ResultsOfKG;
import eu.ebrains.kg.common.model.target.TargetExternalReference;
import eu.ebrains.kg.common.model.target.Value;
import eu.ebrains.kg.common.utils.IdUtils;
import eu.ebrains.kg.common.utils.TranslationException;
import eu.ebrains.kg.common.utils.TranslatorUtils;
import eu.ebrains.kg.projects.ebrains.EBRAINSTranslatorUtils;
import eu.ebrains.kg.projects.ebrains.source.BehavioralProtocolV3;
import eu.ebrains.kg.projects.ebrains.target.BehavioralProtocol;
import eu.ebrains.kg.projects.ebrains.translators.commons.EBRAINSTranslator;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BehavioralProtocolTranslator extends EBRAINSTranslator<BehavioralProtocolV3, BehavioralProtocol, BehavioralProtocolTranslator.Result> {
    private static final String QUERY_ID = "7aa02826-65f3-472b-94bc-46e1288d6f47";

    @Override
    public List<String> getQueryIds() {
        return Collections.singletonList(QUERY_ID);
    }

    @Override
    public Class<Result> getResultType() {
        return Result.class;
    }

    @Override
    public Class<BehavioralProtocolV3> getSourceType() {
        return BehavioralProtocolV3.class;
    }

    @Override
    public Class<BehavioralProtocol> getTargetType() {
        return BehavioralProtocol.class;
    }

    @Override
    public List<String> semanticTypes() {
        return Collections.singletonList("https://openminds.ebrains.eu/core/BehavioralProtocol");
    }

    public static class Result extends ResultsOfKG<BehavioralProtocolV3> {
    }

    public BehavioralProtocol translate(BehavioralProtocolV3 behavioralProtocolV3, DataStage dataStage, boolean liveMode, TranslatorUtils translatorUtils) throws TranslationException {
       BehavioralProtocol b = new BehavioralProtocol();

        b.setCategory(new Value<>("Behavioral Protocol"));
        b.setDisclaimer(new Value<>("Please alert us at [curation-support@ebrains.eu](mailto:curation-support@ebrains.eu) for errors or quality concerns, so we can forward this information to the custodian responsible."));

        b.setId(IdUtils.getUUID(behavioralProtocolV3.getId()));
       b.setAllIdentifiers(behavioralProtocolV3.getIdentifier());
       b.setIdentifier(IdUtils.getUUID(behavioralProtocolV3.getIdentifier()).stream().distinct().collect(Collectors.toList()));
       b.setOfficialAbbreviation(value(behavioralProtocolV3.getInternalIdentifier()));
       b.setTitle(value(behavioralProtocolV3.getName()));
       b.setDescription(value(behavioralProtocolV3.getDescription()));
       if(behavioralProtocolV3.getDescribedInDOI()!=null){
           b.setDescribedIn(value(EBRAINSTranslatorUtils.getFormattedDigitalIdentifier(translatorUtils.getDoiCitationFormatter(), behavioralProtocolV3.getDescribedInDOI(), RelatedPublication.PublicationType.DOI)));
       }
       else if(behavioralProtocolV3.getDescribedInFile()!=null && behavioralProtocolV3.getDescribedInFile().getName()!=null && behavioralProtocolV3.getDescribedInFile().getIri()!=null){
           b.setDescribedInLink(new TargetExternalReference(behavioralProtocolV3.getDescribedInFile().getIri(), behavioralProtocolV3.getDescribedInFile().getName()));
       }
       else if(behavioralProtocolV3.getDescribedInUrl()!=null){
           b.setDescribedInLink(new TargetExternalReference(behavioralProtocolV3.getDescribedInUrl(), behavioralProtocolV3.getDescribedInUrl()));
       }
        b.setQueryBuilderText(value(TranslatorUtils.createQueryBuilderText(behavioralProtocolV3.getPrimaryType(), b.getId())));

        return b;
    }
}
