package eu.ebrains.kg.search.api;


import eu.ebrains.kg.search.controller.translators.TranslationController;
import eu.ebrains.kg.search.model.DatabaseScope;
import eu.ebrains.kg.search.model.target.elasticsearch.TargetInstance;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

@RequestMapping("/translate")
@RestController
public class Translator {

    private final TranslationController translationController;

    public Translator(TranslationController translationController) {
        this.translationController = translationController;
    }

    @GetMapping("/{org}/{domain}/{schema}/{version}/{id}")
    public ResponseEntity<TargetInstance> translate(@RequestParam(value = "databaseScope", required = false) DatabaseScope databaseScope,
                                                    @PathVariable("org") String org,
                                                    @PathVariable("domain") String domain,
                                                    @PathVariable("schema") String schema,
                                                    @PathVariable("version") String version,
                                                    @PathVariable("id") String id,
                                                    @RequestHeader("X-Legacy-Authorization") String authorization) {
        try {
            return ResponseEntity.ok(translationController.createInstance(databaseScope, false, org, domain, schema, version, id, authorization));
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }
}
