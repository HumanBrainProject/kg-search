package eu.ebrains.kg.search.configuration;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableCaching
@EnableScheduling
public class Config {


    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("0_public")
                .pathsToMatch("/api/**", "/sitemap/**")
                .build();
    }
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("1_internal")
                .pathsToMatch("/indexing/**", "/translate/**")
                .build();
    }
}
