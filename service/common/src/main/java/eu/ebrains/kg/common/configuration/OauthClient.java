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

package eu.ebrains.kg.common.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.RemoveAuthorizedClientOAuth2AuthorizationFailureHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class OauthClient {

    private final ObjectMapper objectMapper;

    public OauthClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.addHandler(new GracefulDeserializationProblemHandler());
        this.exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper));
                    configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper));
                    configurer.defaultCodecs().maxInMemorySize(1024 * 1000000);
                }).build();
    }

    protected final ExchangeStrategies exchangeStrategies;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Bean
    @Primary
    WebClient standardWebClient() {
        return WebClient.builder().exchangeStrategies(exchangeStrategies).build();
    }

    @Bean
    @Qualifier("asServiceAccount")
    WebClient serviceAccountWebClient(ClientRegistrationRepository clientRegistrations, OAuth2AuthorizedClientService authorizedClientService) {
        AuthorizedClientServiceOAuth2AuthorizedClientManager clientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrations, authorizedClientService);
        final ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2 = getServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientService, clientManager);
        return WebClient.builder().exchangeStrategies(exchangeStrategies).apply(oauth2.oauth2Configuration()).filter((clientRequest, nextFilter) -> {
            ClientRequest updatedHeaders = ClientRequest.from(clientRequest).headers(h -> {
                h.put("Client-Authorization", h.get(Constants.AUTHORIZATION));
            }).build();
            return nextFilter.exchange(updatedHeaders);
        }).build();
    }

    private ServletOAuth2AuthorizedClientExchangeFilterFunction getServletOAuth2AuthorizedClientExchangeFilterFunction(OAuth2AuthorizedClientService authorizedClientService, AuthorizedClientServiceOAuth2AuthorizedClientManager clientManager) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2 = new ServletOAuth2AuthorizedClientExchangeFilterFunction(clientManager);
        oauth2.setAuthorizationFailureHandler(new RemoveAuthorizedClientOAuth2AuthorizationFailureHandler(
                (clientRegistrationId, principal, attributes) -> {
                    logger.info("Resource server authorization failure for clientRegistrationId={}", clientRegistrationId);
                    authorizedClientService.removeAuthorizedClient(clientRegistrationId, principal.getName());
                })
        );
        oauth2.setDefaultClientRegistrationId("kg");
        return oauth2;
    }

    @Bean
    @Qualifier("asUser")
    WebClient userWebClient(HttpServletRequest request) {
        return WebClient.builder().exchangeStrategies(exchangeStrategies).defaultRequest(r -> {
            /**
             * We just reuse the original authorization header for the given request and we
             * explicitly don't want a client authorization to make the mechanism work with private
             * spaces too.
             */
            r.header(Constants.AUTHORIZATION, request.getHeader(Constants.AUTHORIZATION));
        }).build();
    }

}
