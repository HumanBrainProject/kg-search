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

package eu.ebrains.kg.search.services;

import eu.ebrains.kg.search.model.DataStage;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LegacySearchServiceClient {
    private static final ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1000000)).build();
    public static final WebClient webClient = WebClient.builder().exchangeStrategies(exchangeStrategies).build();

    private static final String token = "";
    private static final String ebrainsUrl = "https://kg.ebrains.eu";

    private static final Pattern editorIdPattern = Pattern.compile("(.+)/(.+)/(.+)/(.+)/(.+)");

    public static <T> T getDocument(DataStage dataStage, String type, String id, Class<T> clazz) {
        String group = dataStage.equals(DataStage.RELEASED)?"public":"curated";
        return getDocument(String.format("/search/api/groups/%s/types/%s/documents/%s", group, type, id), clazz);
    }

    public static <T> T getLiveDocument(String editorId, Class<T> clazz) {
        Matcher m = editorIdPattern.matcher(editorId);
        if (!m.find( )) {
            return null;
        }
        String org = m.group(1);
        String domain = m.group(2);
        String schema = m.group(3);
        String version = m.group(4);
        String id= m.group(5);
        return getDocument(String.format("/search/api/types/%s/%s/%s/%s/documents/%s/preview", org, domain, schema, version, id), clazz);
    }

    private static <T> T getDocument(String uri, Class<T> clazz) {
        return webClient.get()
                .uri(String.format("%s%s", ebrainsUrl, uri))
                .headers(h ->
                {
                    h.add("Authorization", "Bearer " + token);
                    h.add("Accept", "application/json");
                })
                .retrieve()
                .bodyToMono(clazz)
                .block();
    }
}
