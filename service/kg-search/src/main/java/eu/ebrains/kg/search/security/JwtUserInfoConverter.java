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

package eu.ebrains.kg.search.security;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Component
public class JwtUserInfoConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserInfo userInfo;

    public JwtUserInfoConverter(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        return new JwtAuthenticationToken(jwt, userInfo.getAuthorities(new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, jwt.getTokenValue(), jwt.getIssuedAt(), jwt.getExpiresAt())));
    }


    @Component
    public static class UserInfo {

        private final ClientRegistrationRepository clients;
        private final OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService = new DefaultOAuth2UserService();
        private final UserRoles roleMapper;

        public UserInfo(ClientRegistrationRepository clients, UserRoles roleMapper) {
            this.clients = clients;
            this.roleMapper = roleMapper;
        }

        @Cacheable(value="userInfoCache", key="#token")
        public Set<? extends GrantedAuthority> getAuthorities(OAuth2AccessToken token) {
            ClientRegistration clientRegistration = this.clients.findByRegistrationId("kg");
            OAuth2UserRequest oauth2UserRequest = new OAuth2UserRequest(clientRegistration, token);
            final OAuth2User oAuth2User = oauth2UserService.loadUser(oauth2UserRequest);
            final Object roles = oAuth2User.getAttribute("roles");
            if (roles instanceof Map<?, ?> roleMap) {
                return Stream.concat(getStream(roleMap, "team"),  getStream(roleMap, "group")).collect(Collectors.toSet());
            }
            return Collections.emptySet();
        }

        private Stream<GrantedAuthority> getStream(Map<?,?> roleMap, String key){
            final Object claim = roleMap.get(key);
            if(claim instanceof List<?>){
               return ((List<?>) claim).stream().filter(t -> t instanceof String).map(t -> (String) t).map(roleMapper::convert).filter(Objects::nonNull).map(SimpleGrantedAuthority::new);
            }
            return Stream.empty();
        }

    }



}
