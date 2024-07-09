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
import eu.ebrains.kg.common.model.target.Tags;
import eu.ebrains.kg.common.model.target.TargetExternalReference;
import eu.ebrains.kg.common.model.target.Value;
import eu.ebrains.kg.common.utils.IdUtils;
import eu.ebrains.kg.common.utils.TranslationException;
import eu.ebrains.kg.common.utils.TranslatorUtils;
import eu.ebrains.kg.projects.tefHealth.source.ServiceFromKG;
import eu.ebrains.kg.projects.tefHealth.source.models.NameRef;
import eu.ebrains.kg.projects.tefHealth.target.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ServiceTranslator extends Translator<ServiceFromKG, Service, ServiceTranslator.Result> {
    private static final String QUERY_ID = "a5e41c52-f2b7-4cd0-944e-fe4fbd7293a1";

    @Override
    public List<String> getQueryIds() {
        return Collections.singletonList(QUERY_ID);
    }

    @Override
    public Class<Result> getResultType() {
        return Result.class;
    }

    @Override
    public Class<ServiceFromKG> getSourceType() {
        return ServiceFromKG.class;
    }

    @Override
    public Class<Service> getTargetType() {
        return Service.class;
    }

    @Override
    public List<String> semanticTypes() {
        return Collections.singletonList("https://tefhealth.eu/serviceCatalogue/types/Service");
    }

    public static class Result extends ResultsOfKG<ServiceFromKG> {
    }

    public Service translate(ServiceFromKG tefHealthServiceV3, DataStage dataStage, boolean liveMode, TranslatorUtils translatorUtils) throws TranslationException {
        Value<String> title = value(tefHealthServiceV3.getName());
        if(title == null){
            title = new Value<>("");
        }
        Service t = new Service();
        t.setDisclaimer(new Value<>("Please alert us at [info@tefhealth.eu](mailto:info@tefhealth.eu) for errors or quality concerns."));
        t.setId(IdUtils.getUUID(tefHealthServiceV3.getId()));
        t.setContact(new TargetExternalReference("mailto:info@tefhealth.eu", "info@tefhealth.eu"));
        t.setAllIdentifiers(tefHealthServiceV3.getIdentifier());
        List<String> categoryNames = tefHealthServiceV3.getCategory().stream().map(c -> String.format("%s service", c.getName())).sorted().toList();
        if(!categoryNames.isEmpty()){
            t.setTags(new Tags(categoryNames, categoryNames.size(), categoryNames.size(), 0));
            t.setServiceCategories(value(categoryNames));
            t.setCategory(value(categoryNames.getFirst()));
        }
        else{
            t.setCategory(value("Service"));
        }
        if (tefHealthServiceV3.getPricing() != null) {
//            t.setHasPriceExample(true);
            t.setPricing(value(pricing(tefHealthServiceV3.getPricing())));
            t.setPricingDetails(value(tefHealthServiceV3.getPricing().getPricingDetails()));
        }
        if(!CollectionUtils.isEmpty(tefHealthServiceV3.getCalls())){
            t.setCalls(tefHealthServiceV3.getCalls().stream().map(c -> value(c.getName())).sorted().toList());
        }
        t.setIdentifier(IdUtils.getUUID(tefHealthServiceV3.getIdentifier()).stream().distinct().collect(Collectors.toList()));
        t.setTitle(title);
        t.setDescription(value(tefHealthServiceV3.getDescription()));
        t.setServiceInput(value(tefHealthServiceV3.getServiceInput()));
        t.setServiceOutput(value(tefHealthServiceV3.getServiceOutput()));
        t.setServiceStandards(value(tefHealthServiceV3.getServiceStandards().stream().map(NameRef::getName).toList()));
        t.setCertificationSupport(value(tefHealthServiceV3.getCertificationSupport().stream().map(NameRef::getName).toList()));
        t.setDependenciesAndRestrictions(value(tefHealthServiceV3.getDependenciesAndRestrictions().stream().map(NameRef::getName).toList()));
        t.setOfferings(value(tefHealthServiceV3.getOfferings().stream().map(NameRef::getName).toList()));
        if(tefHealthServiceV3.getProvider()!=null){
            t.setProvidedBy(ref(tefHealthServiceV3.getProvider().getOrganization()));
        }
        return t;
    }

    private String pricing(ServiceFromKG.PricingInformation pricingInformation) {
        String template = """
                Pricing van vary.
                          
                **Non-binding example:**
                %s""";
        String pricing = "";
        if (pricingInformation.getFullPriceInEuro() != null) {
            pricing += String.format("%s€%s %s\n", pricingInformation.getReducedPriceInEuro() != null ? "*Full price:* " : "", pricingInformation.getFullPriceInEuro(), Objects.toString(pricingInformation.getBilling(), ""));
        }

        if (pricingInformation.getReducedPriceInEuro() != null) {
            pricing += String.format("*Reduced price:* €%s %s", pricingInformation.getReducedPriceInEuro(), Objects.toString(pricingInformation.getBilling(), "")).trim();
        }
        return String.format(template, pricing);


    }
}
