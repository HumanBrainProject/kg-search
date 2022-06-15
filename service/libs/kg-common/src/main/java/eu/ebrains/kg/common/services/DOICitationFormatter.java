package eu.ebrains.kg.common.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

    private final boolean resolveDOIs;

    public DOICitationFormatter(@Value("${RESOLVE_DOIS:true}") boolean resolveDOIs) {
        this.resolveDOIs = resolveDOIs;
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final WebClient webClient = WebClient.builder().clientConnector(new ReactorClientHttpConnector(
            HttpClient.create().followRedirect(true)
    )).build();

    @Cacheable(value = "doiCitation", unless = "#result == null", key = "#doi.concat('-').concat(#style).concat(#contentType)")
    public String getDOICitation(String doi, String style, String contentType) {
        return doGetDOICitation(doi, style, contentType);
    }

    @CachePut(value = "doiCitation", unless = "#result == null", key = "#doi.concat('-').concat(#style).concat(#contentType)")
    public String refreshDOICitation(String doi, String style, String contentType) {
        return doGetDOICitation(doi, style, contentType);
    }

    @CacheEvict(value = "doiCitation", allEntries = true)
    public void evictAll() {
        logger.info("Wiping all citation cache");
    }

    private boolean isEbrainsDOI(String doi) {
        return doi.startsWith("https://doi.org/10.25493/");
    }

    private String getDOICitationViaDataciteAPI(String doi, String style, String contentType) {
        String doiOnly = doi.replace("https://doi.org/", "");
        logger.info("Doi not present in the cache - fetching from datacite.");
        return webClient.get().uri(String.format("https://api.datacite.org/dois/%s?style=%s", doiOnly, style)).header("Accept", String.format("%s", contentType)).retrieve().bodyToMono(String.class).block();
    }

    private String doGetDOICitation(String doi, String style, String contentType) {
        if (resolveDOIs) {
            String value = null;
            if (isEbrainsDOI(doi)) {
                //Workaround to fix the datacite issues about citation formatting
                try {
                    value = getDOICitationViaDataciteAPI(doi, style, contentType);
                } catch (WebClientException e) {
                    logger.warn("Wasn't able to resolve DOI %s via datacite - trying by contentType negotiation");
                }
            }
            if (value == null) {
                try {
                    logger.info("Doi not present in the cache - fetching from doi.org.");
                    value = webClient.get().uri(doi).header("Accept", String.format("%s; style=%s", contentType, style)).retrieve().bodyToMono(String.class).block();
                } catch (WebClientException e) {
                    return null;
                }
            }
            return value != null ? value.trim() : null;
        } else {
            logger.info("Skipping DOI resolution for {}", doi);
            return null;
        }
    }

}

