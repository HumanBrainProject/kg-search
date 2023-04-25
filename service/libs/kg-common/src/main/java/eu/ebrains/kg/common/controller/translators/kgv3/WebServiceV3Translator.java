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
import eu.ebrains.kg.common.model.source.openMINDSv3.SoftwareV3;
import eu.ebrains.kg.common.model.source.openMINDSv3.WebServiceV3;
import eu.ebrains.kg.common.model.source.openMINDSv3.commons.Version;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.Software;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.WebService;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.Children;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.Value;
import eu.ebrains.kg.common.utils.IdUtils;
import eu.ebrains.kg.common.utils.TranslationException;
import eu.ebrains.kg.common.utils.TranslatorUtils;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class WebServiceV3Translator extends TranslatorV3<WebServiceV3, WebService, WebServiceV3Translator.Result> {
    public static class Result extends ResultsOfKGv3<WebServiceV3> {
    }

    @Override
    public Class<WebServiceV3> getSourceType() {
        return WebServiceV3.class;
    }

    @Override
    public Class<WebService> getTargetType() {
        return WebService.class;
    }

    @Override
    public Class<Result> getResultType() {
        return Result.class;
    }

    @Override
    public List<String> getQueryIds() {
        return Collections.singletonList("b5a588d0-ddf1-4e49-9fc6-043546cfa002");
    }

    @Override
    public List<String> semanticTypes() {
        return Collections.singletonList("https://openminds.ebrains.eu/core/WebService");
    }

    public WebService translate(WebServiceV3 webservice, DataStage dataStage, boolean liveMode, TranslatorUtils translatorUtils) throws TranslationException {
        WebService w = new WebService();

        w.setCategory(new Value<>("Webservice Overview"));
        w.setDisclaimer(new Value<>("Please alert us at [curation-support@ebrains.eu](mailto:curation-support@ebrains.eu) for errors or quality concerns regarding the web service, so we can forward this information to the custodian responsible."));

        List<Version> sortedVersions = Helpers.sort(webservice.getVersions(), translatorUtils.getErrors());
        List<Children<WebService.Version>> webServiceVersions = sortedVersions.stream().map(v -> {
            WebService.Version version = new WebService.Version();
            version.setVersion(new TargetInternalReference(IdUtils.getUUID(v.getId()), v.getVersionIdentifier()));
            version.setInnovation(v.getVersionInnovation() != null ? new Value<>(v.getVersionInnovation()) : null);
            return new Children<>(version);
        }).collect(Collectors.toList());
        w.setWebServiceVersions(webServiceVersions);
        w.setId(IdUtils.getUUID(webservice.getId()));
        w.setAllIdentifiers(webservice.getIdentifier());
        w.setIdentifier(IdUtils.getIdentifiersWithPrefix("Software", webservice.getIdentifier()).stream().distinct().collect(Collectors.toList()));
        w.setDescription(value(webservice.getDescription()));
        w.setTitle(value(webservice.getTitle()));
        if (!CollectionUtils.isEmpty(webservice.getDeveloper())) {
            w.setDevelopers(webservice.getDeveloper().stream()
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        }
        if (!CollectionUtils.isEmpty(webservice.getCustodian())) {
            w.setCustodians(webservice.getCustodian().stream()
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        }
        w.setCustomCitation(value(webservice.getHowToCite()));
        return w;
    }
}
