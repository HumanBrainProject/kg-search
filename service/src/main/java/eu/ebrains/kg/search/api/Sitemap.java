package eu.ebrains.kg.search.api;


import eu.ebrains.kg.search.model.SitemapXML;
import eu.ebrains.kg.search.controller.sitemap.SitemapController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@RequestMapping(value="/sitemap", produces = MediaType.APPLICATION_XML_VALUE)
@RestController
public class Sitemap {
    private final SitemapController sitemapController;

    public Sitemap(SitemapController sitemapController) {
        this.sitemapController = sitemapController;
    }

    @GetMapping
    public ResponseEntity<?> generateSitemap() {
        try {
            SitemapXML sitemapXML = sitemapController.getSitemap();
            if(sitemapXML == null) {
                return  ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            }
            return ResponseEntity.ok(sitemapXML);
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

}
