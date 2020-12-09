package eu.ebrains.kg.search.controller.authentication;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.AdapterDeploymentContext;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.common.util.KeycloakUriBuilder;
import org.keycloak.protocol.oidc.representations.OIDCConfigurationRepresentation;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.keycloak.util.JsonSerialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.security.Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UserInfoRoles {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final UserInfoRepository repository;

    public UserInfoRoles(UserInfoRepository repository) {
        this.repository = repository;
    }

    public boolean isInAnyOfRoles(KeycloakAuthenticationToken authenticationToken, String client, String... roles){
        if(authenticationToken!=null) {
            KeycloakSecurityContext keycloakSecurityContext = authenticationToken.getAccount().getKeycloakSecurityContext();
            if(keycloakSecurityContext.getToken().isExpired()){
                repository.clearUserInfoCache(keycloakSecurityContext.getTokenString());
            }
            else{
                logger.info(String.format("Token is still valid for %d seconds... ", (int)(keycloakSecurityContext.getToken().getExp()-Math.floor(new Date().getTime()/1000.0))));
            }
            UserInfoRepository.UserInfo userInfo = repository.fetchUserInfo(keycloakSecurityContext.getTokenString());
            if(userInfo!=null){
                for (String role : roles) {
                    if(userInfo.isInRole(client, role)){
                        return true;
                    }
                }
                return false;
            }
        }
        return false;
    }
}
