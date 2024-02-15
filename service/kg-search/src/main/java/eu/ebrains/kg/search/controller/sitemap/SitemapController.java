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

package eu.ebrains.kg.search.controller.sitemap;

import eu.ebrains.kg.common.controller.translation.TranslatorRegistry;
import eu.ebrains.kg.common.controller.translation.models.TranslatorModel;
import eu.ebrains.kg.common.model.DataStage;
import eu.ebrains.kg.common.model.elasticsearch.Document;
import eu.ebrains.kg.common.services.ESServiceClient;
import eu.ebrains.kg.common.utils.ESHelper;
import eu.ebrains.kg.search.model.SitemapXML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class SitemapController {

    @Value("${kgebrains.endpoint}")
    String ebrainsUrl;

    private final ESServiceClient esServiceClient;
    private final ESHelper esHelper;
    private final TranslatorRegistry translatorRegistry;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public SitemapController(ESServiceClient esServiceClient, ESHelper esHelper, TranslatorRegistry translatorRegistry) {
        this.esServiceClient = esServiceClient;
        this.esHelper = esHelper;
        this.translatorRegistry = translatorRegistry;
    }

    public boolean isInAdminRole() {
        return false;

//        return userInfoRoles.isInAnyOfRoles("kg-cron", "admin");
    }

    @Cacheable(value = "sitemap", unless = "#result == null")
    public SitemapXML getSitemap() {
        return fetchSitemap();
    }


    private SitemapXML fetchSitemap() {
        List<SitemapXML.Url> urls = new ArrayList<>();
        String index = esHelper.getIndexesForDocument(DataStage.RELEASED);
        final Set<String> relevantTypes = translatorRegistry.getTranslators().stream().filter(TranslatorModel::isAddToSitemap).map(t -> {
            try {
                return t.getTargetClass().getConstructor().newInstance().getType().getValue();
            } catch (InstantiationException | IllegalAccessException| InvocationTargetException | NoSuchMethodException e) {
                logger.error("Was not able to find type for sitemap generation", e);
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toSet());
        try {
            List<Document> documents = esServiceClient.getDocumentsForSitemap(index, relevantTypes);
            documents.forEach(doc -> {
                SitemapXML.Url url = new SitemapXML.Url();
                url.setLoc(String.format("%s/instances/%s?noSilentSSO=true", ebrainsUrl, doc.getId()));
                urls.add(url);
            });
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw e;
            }
        }
        if (urls.isEmpty()) {
            return null;
        }
        SitemapXML sitemapXML = new SitemapXML();
        sitemapXML.setUrl(urls);
        return sitemapXML;
    }

    @CachePut(value = "sitemap", unless = "#result == null")
    public SitemapXML updateSitemapCache() {
        return fetchSitemap();
    }

}
