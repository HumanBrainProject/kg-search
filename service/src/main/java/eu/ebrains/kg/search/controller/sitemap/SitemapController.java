package eu.ebrains.kg.search.controller.sitemap;

import eu.ebrains.kg.search.controller.Constants;
import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.SitemapXML;
import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchDocument;
import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchResult;
import eu.ebrains.kg.search.services.ESServiceClient;
import eu.ebrains.kg.search.utils.ESHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
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
        List<SitemapXML.Url> urls = new ArrayList<>();
        String index = ESHelper.getIdentifierIndex(DataStage.RELEASED);
        try {
            List<ElasticSearchDocument> documents = esServiceClient.getDocuments(index);
            documents.forEach(doc -> {
                SitemapXML.Url url = new SitemapXML.Url();
                url.setLoc(String.format("%s/instances/%s", ebrainsUrl, doc.getId()));
                urls.add(url);
            });
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw e;
            }
        }
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
