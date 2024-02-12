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

package eu.ebrains.kg.common.controller.translators.kgv3.tefHealth;

import eu.ebrains.kg.common.controller.translators.kgv3.TranslatorV3;
import eu.ebrains.kg.common.model.DataStage;
import eu.ebrains.kg.common.model.source.ResultsOfKGv3;
import eu.ebrains.kg.common.model.source.openMINDSv3.tef.TefHealthServiceV3;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.Value;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.tefHealth.TefHealthService;
import eu.ebrains.kg.common.utils.IdUtils;
import eu.ebrains.kg.common.utils.TranslationException;
import eu.ebrains.kg.common.utils.TranslatorUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TefHealthServiceV3Translator extends TranslatorV3<TefHealthServiceV3, TefHealthService, TefHealthServiceV3Translator.Result> {
    private static final String QUERY_ID = "a5e41c52-f2b7-4cd0-944e-fe4fbd7293a1";

    @Override
    public List<String> getQueryIds() {
        return Collections.singletonList(QUERY_ID);
    }

    @Override
    public Class<TefHealthServiceV3Translator.Result> getResultType() {
        return TefHealthServiceV3Translator.Result.class;
    }

    @Override
    public Class<TefHealthServiceV3> getSourceType() {
        return TefHealthServiceV3.class;
    }

    @Override
    public Class<TefHealthService> getTargetType() {
        return TefHealthService.class;
    }

    @Override
    public List<String> semanticTypes() {
        return Collections.singletonList("https://ebrains.eu/tef-health/Service");
    }

    public static class Result extends ResultsOfKGv3<TefHealthServiceV3> {
    }

    public TefHealthService translate(TefHealthServiceV3 tefHealthServiceV3, DataStage dataStage, boolean liveMode, TranslatorUtils translatorUtils) throws TranslationException {
        TefHealthService t = new TefHealthService();
        t.setCategory(new Value<>("TEF Service"));
        t.setDisclaimer(new Value<>("Please alert us at [curation-support@ebrains.eu](mailto:curation-support@ebrains.eu) for errors or quality concerns, so we can forward this information to the custodian responsible."));
        t.setId(IdUtils.getUUID(tefHealthServiceV3.getId()));
        t.setAllIdentifiers(tefHealthServiceV3.getIdentifier());
        t.setIdentifier(IdUtils.getUUID(tefHealthServiceV3.getIdentifier()).stream().distinct().collect(Collectors.toList()));
        t.setTitle(value(tefHealthServiceV3.getName()));
        t.setDescription(value(tefHealthServiceV3.getDescription()));
        t.setProvidedBy(ref(tefHealthServiceV3.getProvidedBy(), true));
        t.setCountry(ref(tefHealthServiceV3.getCountry()));
        t.setServiceCategory(value(tefHealthServiceV3.getServiceCategory()));
        t.setUseCaseCategories(value(tefHealthServiceV3.getUseCaseCategories().stream().sorted().toList()));
        t.setUseCaseDomains(value(tefHealthServiceV3.getUseCaseDomains().stream().sorted().toList()));
        t.setUseCaseDomainOtherDescription(value(tefHealthServiceV3.getUseCaseDomainOtherDescription()));
        return t;
    }
}
