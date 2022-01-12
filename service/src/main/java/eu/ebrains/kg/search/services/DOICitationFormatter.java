package eu.ebrains.kg.search.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.netty.http.client.HttpClient;

@Component
public class DOICitationFormatter {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final WebClient webClient = WebClient.builder().clientConnector(new ReactorClientHttpConnector(
                    HttpClient.create().followRedirect(true)
            )).build();;


    @Cacheable(value="doiCitation",  unless="#result == null", key = "#doi.concat('-').concat(#style)")
    public String getDOICitation(String doi, String style){
       return doGetDOICitation(doi, style);
    }

    @CachePut(value="doiCitation",  unless="#result == null", key = "#doi.concat('-').concat(#style)")
    public String refreshDOICitation(String doi, String style){
        return doGetDOICitation(doi, style);
    }

    @CacheEvict(value="doiCitation", allEntries = true)
    public void evictAll() {
        logger.info("Wiping all citation cache");
    }

    private String doGetDOICitation(String doi, String style){
        try{
            final String value = webClient.get().uri(doi).header("Accept", String.format("text/x-bibliography; style=%s", style)).retrieve().bodyToMono(String.class).block();
            return value != null ? value.trim() : null;
        }
        catch (WebClientException e){
            return null;
        }
    }

}
