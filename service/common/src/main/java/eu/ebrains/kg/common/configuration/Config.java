/*
 *  Copyright 2018 - 2021 Swiss Federal Institute of Technology Lausanne (EPFL)
 *  Copyright 2021 - 2024 EBRAINS AISBL
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

import eu.ebrains.kg.common.controller.translation.TranslatorRegistry;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Collections;


@Configuration
@EnableCaching
@EnableScheduling
public class Config {
    @Bean
    public OpenAPI customOpenAPI(@Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}") String issuerUri, @Value("${eu.ebrains.kg.commit}") String commit, TranslatorRegistry translatorRegistry) {
        OAuthFlow oAuthFlow = new OAuthFlow();
        oAuthFlow.authorizationUrl(String.format("%s/protocol/openid-connect/auth", issuerUri));
        oAuthFlow.tokenUrl(String.format("%s/protocol/openid-connect/token", issuerUri));
        SecurityScheme userToken = new SecurityScheme().name(Constants.AUTHORIZATION).type(SecurityScheme.Type.OAUTH2).flows(new OAuthFlows().authorizationCode(oAuthFlow)).description("The user authentication");
        SecurityRequirement userWithoutClientReq = new SecurityRequirement().addList(Constants.AUTHORIZATION);

        OpenAPI openapi = new OpenAPI().openapi("3.0.3");
        String description = String.format("This is the API of the EBRAINS Knowledge Graph (commit %s)", commit);

        return openapi.info(new Info().version("v3.0.0").title(String.format("This is the KG Search API for %s", translatorRegistry.getName())).description(description).license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0.html")).termsOfService("https://kg.ebrains.eu/search-terms-of-use.html"))
                .components(new Components()).schemaRequirement(Constants.AUTHORIZATION, userToken)
                .security(Collections.singletonList(userWithoutClientReq));
    }


}
