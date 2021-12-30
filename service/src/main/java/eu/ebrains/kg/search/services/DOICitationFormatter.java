package eu.ebrains.kg.search.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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


    @Cacheable(value="doiCitation",  unless="#result == null")
    public String getDOICitation(String doi){
       return doGetDOICitation(doi);
    }

    public String getDOICitationWithStyle(String doi, String style) {
        return getCitation(doi, style);
    }

    @CachePut(value="doiCitation",  unless="#result == null")
    public String refreshDOICitation(String doi){
        return doGetDOICitation(doi);
    }

    private String doGetDOICitation(String doi){
        return getCitation(doi, "european-journal-of-neuroscience");
    }

    private String getCitation(String doi, String style) {
        try{
            final String value = webClient.get().uri(doi).header("Accept", String.format("text/x-bibliography; style=%s", style)).retrieve().bodyToMono(String.class).block();
            return value != null ? value.trim() : null;
        }
        catch (WebClientException e){
            return null;
        }
    }

}
