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

package eu.ebrains.kg.projects.tefHealth.translators;

import eu.ebrains.kg.common.controller.translation.models.Translator;
import eu.ebrains.kg.common.model.DataStage;
import eu.ebrains.kg.common.model.source.ResultsOfKG;
import eu.ebrains.kg.common.model.target.Value;
import eu.ebrains.kg.common.utils.IdUtils;
import eu.ebrains.kg.common.utils.TranslatorUtils;
import eu.ebrains.kg.projects.tefHealth.target.Country;
import eu.ebrains.kg.projects.tefHealth.source.CountryFromKG;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CountryTranslator extends Translator<CountryFromKG, Country, CountryTranslator.Result> {
    private static final String QUERY_ID = "d4f6cffd-f94c-442f-b76d-25633ea1b819";

    @Override
    public List<String> getQueryIds() {
        return Collections.singletonList(QUERY_ID);
    }

    @Override
    public Class<Result> getResultType() {
        return Result.class;
    }

    @Override
    public Class<CountryFromKG> getSourceType() {
        return CountryFromKG.class;
    }

    @Override
    public Class<Country> getTargetType() {
        return Country.class;
    }

    @Override
    public List<String> semanticTypes() {
        return Collections.singletonList("https://tefhealth.eu/serviceCatalogue/types/Country");
    }

    public static class Result extends ResultsOfKG<CountryFromKG> {
    }

    public Country translate(CountryFromKG tefHealthCountryV3, DataStage dataStage, boolean liveMode, TranslatorUtils translatorUtils) {
        Country t = new Country();
        t.setCategory(new Value<>("Country"));
        t.setDisclaimer(new Value<>("Please alert us at [info@tefhealth.eu](mailto:info@tefhealth.eu) for errors or quality concerns."));
        t.setId(IdUtils.getUUID(tefHealthCountryV3.getId()));
        t.setAllIdentifiers(tefHealthCountryV3.getIdentifier());
        t.setIdentifier(IdUtils.getUUID(tefHealthCountryV3.getIdentifier()).stream().distinct().collect(Collectors.toList()));
        t.setTitle(value(tefHealthCountryV3.getName()));
        t.setOrganizations(ref(tefHealthCountryV3.getOrganizations(), true));
        t.setServices(ref(tefHealthCountryV3.getServices(), true));
        return t;
    }
}
