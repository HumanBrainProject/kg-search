package eu.ebrains.kg.search.controller.authentication;

import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;

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
                logger.debug(String.format("Token is still valid for %d seconds... ", (int)(keycloakSecurityContext.getToken().getExp()-Math.floor(new Date().getTime()/1000.0))));
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
