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

package eu.ebrains.kg.search.configuration;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
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

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

@Configuration
public class OauthClient {

    private final ObjectMapper objectMapper;
    public static ThreadLocal<Map<Integer, List<String>>> ERROR_REPORTING_THREAD_LOCAL = new ThreadLocal<>();


    public OauthClient(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.addHandler(new DeserializationProblemHandler() {
            @Override
            public Object handleUnexpectedToken(DeserializationContext ctxt, JavaType targetType, JsonToken t, JsonParser p, String failureMsg) throws IOException {
                final int rootObjectIndex = p.getParsingContext().pathAsPointer().tail().getMatchingIndex();
                final String path = p.getParsingContext().pathAsPointer().tail().tail().toString();
                if(ERROR_REPORTING_THREAD_LOCAL.get()==null){
                    ERROR_REPORTING_THREAD_LOCAL.set(new HashMap<>());
                }
                final String error = String.format("Was not able to parse %s - target type is %s", path, targetType.getTypeName());
                logger.error(error);
                ERROR_REPORTING_THREAD_LOCAL.get().computeIfAbsent(rootObjectIndex, k -> new ArrayList<>());
                ERROR_REPORTING_THREAD_LOCAL.get().get(rootObjectIndex).add(error);
                return null;
            }
        });
        this.exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper));
                    configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper));
                    configurer.defaultCodecs().maxInMemorySize(1024 * 1000000);
                }).build();
    }


//    private final ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
//            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1000000)).build();
//
    private final ExchangeStrategies exchangeStrategies;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Bean
    @Primary
    WebClient standardWebClient(){
        return WebClient.builder().exchangeStrategies(exchangeStrategies).build();
    }

    @Bean
    @Qualifier("asServiceAccount")
    WebClient serviceAccountWebClient(ClientRegistrationRepository clientRegistrations, OAuth2AuthorizedClientService authorizedClientService) {
        AuthorizedClientServiceOAuth2AuthorizedClientManager clientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrations, authorizedClientService);
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2 =  new ServletOAuth2AuthorizedClientExchangeFilterFunction(clientManager);
        oauth2.setAuthorizationFailureHandler(new RemoveAuthorizedClientOAuth2AuthorizationFailureHandler(
                (clientRegistrationId, principal, attributes) -> {
                    logger.info("Resource server authorization failure for clientRegistrationId={}", clientRegistrationId);
                    authorizedClientService.removeAuthorizedClient(clientRegistrationId, principal.getName());
                })
        );
        oauth2.setDefaultClientRegistrationId("kg");
        return WebClient.builder().exchangeStrategies(exchangeStrategies).apply(oauth2.oauth2Configuration()).filter((clientRequest, nextFilter) ->{
            ClientRequest updatedHeaders = ClientRequest.from(clientRequest).headers(h -> {
                h.put("Client-Authorization", h.get("Authorization"));
            }).build();
            return nextFilter.exchange(updatedHeaders);
        }).build();
    }

    @Bean
    @Qualifier("asUser")
    WebClient userWebClient(ClientRegistrationRepository clientRegistrations, OAuth2AuthorizedClientService authorizedClientService, HttpServletRequest request) {
        AuthorizedClientServiceOAuth2AuthorizedClientManager clientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrations, authorizedClientService);
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2 =  new ServletOAuth2AuthorizedClientExchangeFilterFunction(clientManager);
        oauth2.setDefaultClientRegistrationId("kg");
        return WebClient.builder().exchangeStrategies(exchangeStrategies).apply(oauth2.oauth2Configuration()).filter((clientRequest, nextFilter) ->{
            ClientRequest updatedHeaders = ClientRequest.from(clientRequest).headers(h -> {
                //Spring adds the oauth2 bearer token to the standard "Authorization" header -> we want it to be sent as
                // "Client-Authorization" though to let the user token be handed in properly.
                h.put("Client-Authorization", h.get("Authorization"));
                List<String> userAuth = h.get("User-Authorization");
                h.put("Authorization", userAuth);
                h.remove("User-Authorization");
            }).build();
            return nextFilter.exchange(updatedHeaders);
        }).defaultRequest(r -> {
            /**
             *  We have to add the user access token to the request here, because this consumer is executed in the original
             *  thread and we therefore have access to the original request. We store it in a temporary header since otherwise
             *  it would be overwritten by the above exchange filter.
             */
            r.header("User-Authorization", request.getHeader("Authorization"));
        }).build();
    }

}
