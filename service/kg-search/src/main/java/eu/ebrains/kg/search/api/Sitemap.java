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

package eu.ebrains.kg.search.api;


import eu.ebrains.kg.search.controller.sitemap.SitemapController;
import eu.ebrains.kg.search.model.SitemapXML;
import eu.ebrains.kg.search.security.UserRoles;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.security.Principal;

@RequestMapping(value = "/sitemap", produces = MediaType.APPLICATION_XML_VALUE)
@RestController
public class Sitemap {
    private final SitemapController sitemapController;

    public Sitemap(SitemapController sitemapController) {
        this.sitemapController = sitemapController;
    }

    @GetMapping
    @SuppressWarnings("java:S1452") // we keep the generics intentionally
    public ResponseEntity<?> generateSitemap() {
        try {
            SitemapXML sitemapXML = sitemapController.getSitemap();
            if (sitemapXML == null) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            }
            return ResponseEntity.ok(sitemapXML);
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @PutMapping
    @UserRoles.MustBeAdmin
    public ResponseEntity updateSitemapCache(Principal principal) {
        sitemapController.updateSitemapCache();
        return ResponseEntity.ok().build();
    }

    @Scheduled(fixedRate = 24 * 60 * 60 * 1000)
    public void refreshSitemapRegularly() {
        sitemapController.updateSitemapCache();
    }

}
