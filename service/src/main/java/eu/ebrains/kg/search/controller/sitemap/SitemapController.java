package eu.ebrains.kg.search.controller.sitemap;

import eu.ebrains.kg.search.controller.Constants;
import eu.ebrains.kg.search.model.DatabaseScope;
import eu.ebrains.kg.search.model.Sitemap;
import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchResult;
import eu.ebrains.kg.search.services.ESServiceClient;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SitemapController {
    private final ESServiceClient esServiceClient;

    public SitemapController(ESServiceClient esServiceClient) {
        this.esServiceClient = esServiceClient;
    }

    @Cacheable("sitemap")
    public Sitemap getSitemap(){
        return fetchSitemap();
    }

    private Sitemap fetchSitemap(){
        //TODO check if we want to cache each type individually
        List<Sitemap.Url> urls = Constants.TARGET_MODELS_MAP.keySet().stream().map(type -> {
            String index = esServiceClient.getIndex(type, DatabaseScope.RELEASED);
            ElasticSearchResult documents = esServiceClient.getDocuments(index);
            return documents.getHits().getHits().stream().map(doc -> {
                Sitemap.Url url = new Sitemap.Url();
                //TODO read base path from environment variable
                url.setLoc(String.format("https://kg.ebrains.eu/search/instances/%s/%s", doc.getType(), doc.getId()));
                return url;
            }).collect(Collectors.toList());
        }).flatMap(Collection::stream).collect(Collectors.toList());
        Sitemap sitemap = new Sitemap();
        sitemap.setUrl(urls);
        return sitemap;
    }

    @CachePut(value="sitemap")
    public Sitemap updateSitemapCache() {
        return fetchSitemap();
    }

}
