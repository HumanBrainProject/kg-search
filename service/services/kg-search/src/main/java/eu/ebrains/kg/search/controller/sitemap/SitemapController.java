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

package eu.ebrains.kg.search.controller.sitemap;

import eu.ebrains.kg.common.model.DataStage;
import eu.ebrains.kg.search.controller.authentication.UserInfoRoles;
import eu.ebrains.kg.search.model.SitemapXML;
import eu.ebrains.kg.common.model.target.elasticsearch.ElasticSearchDocument;
import eu.ebrains.kg.common.services.ESServiceClient;
import eu.ebrains.kg.common.utils.ESHelper;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Component
public class SitemapController {

    @Value("${kgebrains.endpoint}")
    String ebrainsUrl;

    private final ESServiceClient esServiceClient;
    private final UserInfoRoles userInfoRoles;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public SitemapController(ESServiceClient esServiceClient, UserInfoRoles userInfoRoles) {
        this.esServiceClient = esServiceClient;
        this.userInfoRoles = userInfoRoles;
    }

    public boolean isInAdminRole(Principal principal) {
        return userInfoRoles.isInAnyOfRoles((KeycloakAuthenticationToken) principal, "kg-cron", "admin");
    }

    @Cacheable(value = "sitemap", unless = "#result == null")
    public SitemapXML getSitemap() {
        return fetchSitemap();
    }


    private SitemapXML fetchSitemap() {
        List<SitemapXML.Url> urls = new ArrayList<>();
        String index = ESHelper.getIndexesForDocument(DataStage.RELEASED);
        try {
            List<ElasticSearchDocument> documents = esServiceClient.getDocuments(index);
            documents.forEach(doc -> {
                SitemapXML.Url url = new SitemapXML.Url();
                url.setLoc(String.format("%s/instances/%s", ebrainsUrl, doc.getId()));
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
