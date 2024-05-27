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
import eu.ebrains.kg.common.model.target.TargetInternalReference;
import eu.ebrains.kg.common.model.target.Value;
import eu.ebrains.kg.common.utils.IdUtils;
import eu.ebrains.kg.common.utils.TranslationException;
import eu.ebrains.kg.common.utils.TranslatorUtils;
import eu.ebrains.kg.projects.tefHealth.source.OrganizationFromKG;
import eu.ebrains.kg.projects.tefHealth.source.models.ServiceRef;
import eu.ebrains.kg.projects.tefHealth.target.Organization;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class OrganizationTranslator extends Translator<OrganizationFromKG, Organization, OrganizationTranslator.Result> {
    private static final String QUERY_ID = "8614e3b4-a831-41ee-ad9e-b85dacc36439";

    @Override
    public List<String> getQueryIds() {
        return Collections.singletonList(QUERY_ID);
    }

    @Override
    public Class<Result> getResultType() {
        return Result.class;
    }

    @Override
    public Class<OrganizationFromKG> getSourceType() {
        return OrganizationFromKG.class;
    }

    @Override
    public Class<Organization> getTargetType() {
        return Organization.class;
    }

    @Override
    public List<String> semanticTypes() {
        return Collections.singletonList("https://tefhealth.eu/serviceCatalogue/types/Organization");
    }

    public static class Result extends ResultsOfKG<OrganizationFromKG> {
    }

    public Organization translate(OrganizationFromKG tefHealthInstitutionV3, DataStage dataStage, boolean liveMode, TranslatorUtils translatorUtils) throws TranslationException {
        Organization t = new Organization();
        t.setCategory(new Value<>("Organization"));
        t.setDisclaimer(new Value<>("Please alert us at [info@tefhealth.eu](mailto:info@tefhealth.eu) for errors or quality concerns."));
        t.setId(IdUtils.getUUID(tefHealthInstitutionV3.getId()));
        t.setAllIdentifiers(tefHealthInstitutionV3.getIdentifier());
        t.setIdentifier(IdUtils.getUUID(tefHealthInstitutionV3.getIdentifier()).stream().distinct().collect(Collectors.toList()));
        String title = StringUtils.isNotBlank(tefHealthInstitutionV3.getName()) ? StringUtils.isNotBlank(tefHealthInstitutionV3.getAbbreviation()) ? String.format("%s (%s)", tefHealthInstitutionV3.getName(), tefHealthInstitutionV3.getAbbreviation()) : tefHealthInstitutionV3.getName() : null;
        t.setTitle(value(title));
        if(tefHealthInstitutionV3.getServices()!=null) {
            t.setProvidedServices(ref(tefHealthInstitutionV3.getServices(), true));
        }
        t.setCountry(ref(tefHealthInstitutionV3.getCountry()));
        return t;
    }
}
