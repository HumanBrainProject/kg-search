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

package eu.ebrains.kg.search.controller.authentication;

import eu.ebrains.kg.search.configuration.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

@Component
public class UserInfoRepository {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String userInfoUrl;
    private final WebClient webClient = WebClient.builder().build();

    public UserInfoRepository(@Value("${oauth2.user-info-url}") String userInfoUrl) {
        this.userInfoUrl = userInfoUrl;
    }

    public static class UserInfo implements Serializable {
        private RolesByClient roles;
        private String sub;

        public RolesByClient getRoles() {
            return roles;
        }

        public void setRoles(RolesByClient roles) {
            this.roles = roles;
        }

        public String getSub() {
            return sub;
        }

        public void setSub(String sub) {
            this.sub = sub;
        }

        public boolean isInRole(String client, String role){
            if(getRoles()!=null && getRoles().get(client)!=null){
                return getRoles().get(client).contains(role);
            }
            else {
                return false;
            }

        }
    }

    public static class RolesByClient extends HashMap<String, List<String>>{}


    @Cacheable(value="userInfo", key="#token")
    public UserInfo fetchUserInfo(String token){
        logger.info("Fetching user information from endpoint - no cache available");
        return webClient.get().uri(this.userInfoUrl).header(Constants.AUTHORIZATION, String.format("Bearer %s", token)).retrieve().bodyToMono(UserInfo.class).block();
    }

    @CacheEvict(value="userInfo", key="#token")
    public void clearUserInfoCache(String token){
        logger.info("Wipe individual user info entry for timed out token");

    }

    @Scheduled(fixedRate = 60*60*1000)
    @CacheEvict(value="userInfo", allEntries = true)
    public void evictAllcachesAtIntervals() {
        logger.info("Wiping all userinfo cache");
    }
}
