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
import eu.ebrains.kg.common.model.source.openMINDSv3.tef.TefHealthInstitutionV3;
import eu.ebrains.kg.common.model.source.openMINDSv3.tef.TefHealthServiceV3;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.Value;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.tefHealth.TefHealthInstitution;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.tefHealth.TefHealthService;
import eu.ebrains.kg.common.utils.IdUtils;
import eu.ebrains.kg.common.utils.TranslationException;
import eu.ebrains.kg.common.utils.TranslatorUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TefHealthInstitutionV3Translator extends TranslatorV3<TefHealthInstitutionV3, TefHealthInstitution, TefHealthInstitutionV3Translator.Result> {
    private static final String QUERY_ID = "8614e3b4-a831-41ee-ad9e-b85dacc36439";

    @Override
    public List<String> getQueryIds() {
        return Collections.singletonList(QUERY_ID);
    }

    @Override
    public Class<TefHealthInstitutionV3Translator.Result> getResultType() {
        return TefHealthInstitutionV3Translator.Result.class;
    }

    @Override
    public Class<TefHealthInstitutionV3> getSourceType() {
        return TefHealthInstitutionV3.class;
    }

    @Override
    public Class<TefHealthInstitution> getTargetType() {
        return TefHealthInstitution.class;
    }

    @Override
    public List<String> semanticTypes() {
        return Collections.singletonList("https://ebrains.eu/tef-health/Institution");
    }

    public static class Result extends ResultsOfKGv3<TefHealthInstitutionV3> {
    }

    public TefHealthInstitution translate(TefHealthInstitutionV3 tefHealthInstitutionV3, DataStage dataStage, boolean liveMode, TranslatorUtils translatorUtils) throws TranslationException {
        TefHealthInstitution t = new TefHealthInstitution();
        t.setCategory(new Value<>("TEF Institution"));
        t.setDisclaimer(new Value<>("Please alert us at [curation-support@ebrains.eu](mailto:curation-support@ebrains.eu) for errors or quality concerns, so we can forward this information to the custodian responsible."));
        t.setId(IdUtils.getUUID(tefHealthInstitutionV3.getId()));
        t.setAllIdentifiers(tefHealthInstitutionV3.getIdentifier());
        t.setIdentifier(IdUtils.getUUID(tefHealthInstitutionV3.getIdentifier()).stream().distinct().collect(Collectors.toList()));
        t.setTitle(value(tefHealthInstitutionV3.getName()));
        t.setProvidedServiceCategories(value(tefHealthInstitutionV3.getProvidedServiceCategories().stream().sorted().toList()));
        t.setProvidedServices(ref(tefHealthInstitutionV3.getProvidedServices(), true));
        t.setCountry(ref(tefHealthInstitutionV3.getCountry()));
        t.setAssociations(tefHealthInstitutionV3.getAssociations().stream().map(a -> {
            if(a.getAssociatedTo()!=null){
                return new TargetInternalReference(IdUtils.getUUID(a.getAssociatedTo().getId()), a.getName());
            }
            else if(a.getMemberOf()!=null){
                return new TargetInternalReference(IdUtils.getUUID(a.getMemberOf().getId()), a.getName());
            }
            else{
                return null;
            }
        }).filter(Objects::nonNull).sorted().toList());
        return t;
    }
}
