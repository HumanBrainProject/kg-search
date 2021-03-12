package eu.ebrains.kg.search.controller.authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

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

    public static class UserInfo{
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
        return webClient.get().uri(this.userInfoUrl).header("Authorization", String.format("Bearer %s", token)).retrieve().bodyToMono(UserInfo.class).block();
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
