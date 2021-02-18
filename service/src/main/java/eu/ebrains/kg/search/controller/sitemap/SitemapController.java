package eu.ebrains.kg.search.controller.sitemap;

import eu.ebrains.kg.search.controller.Constants;
import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.SitemapXML;
import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchResult;
import eu.ebrains.kg.search.services.ESServiceClient;
import eu.ebrains.kg.search.utils.ESHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class SitemapController {

    @Value("${kgebrains.endpoint}")
    String ebrainsUrl;

    private final ESServiceClient esServiceClient;

    public SitemapController(ESServiceClient esServiceClient) {
        this.esServiceClient = esServiceClient;
    }

    @Cacheable(value = "sitemap", unless = "#result == null")
    public SitemapXML getSitemap() {
        return fetchSitemap();
    }

    private SitemapXML fetchSitemap() {
        //TODO check if we want to cache each type individually
        List<SitemapXML.Url> urls = Constants.TARGET_MODELS_MAP.keySet().stream().map(type -> {
            String index = ESHelper.getIndex(type, DataStage.RELEASED);
            try {
                ElasticSearchResult documents = esServiceClient.getDocuments(index);
                return documents.getHits().getHits().stream().map(doc -> {
                    SitemapXML.Url url = new SitemapXML.Url();
                    url.setLoc(String.format("%s/instances/%s/%s", ebrainsUrl, type, doc.getId()));
                    return url;
                }).collect(Collectors.toList());
            } catch (WebClientResponseException e) {
                if (e.getStatusCode() != HttpStatus.NOT_FOUND) {
                    throw e;
                } else {
                    return null;
                }
            }
        }).filter(Objects::nonNull).flatMap(Collection::stream).collect(Collectors.toList());
        if (urls.isEmpty()) {
            return null;
        }
        SitemapXML sitemapXML = new SitemapXML();
        sitemapXML.setUrl(urls);
        return sitemapXML;
    }

    @CachePut(value = "sitemap")
    public SitemapXML updateSitemapCache(DataStage dataStage) {
        if (dataStage == DataStage.RELEASED) {
            return fetchSitemap();
        }
        return null;
    }

}
