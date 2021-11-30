package eu.ebrains.kg.search.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.Scheduled;
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


    @Cacheable(value="doiCitation",  unless="#result == null")
    //TODO permanent caching
    public String getDOICitation(String doi){
        try{
            final String value = webClient.get().uri(doi).header("Accept", "text/x-bibliography; style=european-journal-of-neuroscience").retrieve().bodyToMono(String.class).block();
            return value != null ? value.trim() : null;
        }
        catch (WebClientException e){
            return null;
        }
    }

    @Scheduled(cron="@weekly")
    @CacheEvict("doiCitation")
    public void clearDOICitationCache(){
        logger.info("Clearing DOI citation cache");
    }

}
