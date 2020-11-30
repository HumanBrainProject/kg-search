package eu.ebrains.kg.search.api;


import eu.ebrains.kg.search.controller.translators.TranslationController;
import eu.ebrains.kg.search.model.DatabaseScope;
import eu.ebrains.kg.search.model.target.elasticsearch.TargetInstance;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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

    @GetMapping("/{org}/{domain}/{schema}/{version}/{id}/live")
    public ResponseEntity<TargetInstance> translate(@PathVariable("org") String org,
                                                    @PathVariable("domain") String domain,
                                                    @PathVariable("schema") String schema,
                                                    @PathVariable("version") String version,
                                                    @PathVariable("id") String id,
                                                    @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        try {
            return ResponseEntity.ok(translateInstance(DatabaseScope.INFERRED, true, org, domain, schema, version, id, authorization));
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @GetMapping("/{org}/{domain}/{schema}/{version}/{id}")
    public ResponseEntity<TargetInstance> translate(@RequestParam(value = "databaseScope", required = false) DatabaseScope databaseScope,
                                                    @PathVariable("org") String org,
                                                    @PathVariable("domain") String domain,
                                                    @PathVariable("schema") String schema,
                                                    @PathVariable("version") String version,
                                                    @PathVariable("id") String id,
                                                    @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        try {
            return ResponseEntity.ok(translateInstance(databaseScope, false, org, domain, schema, version, id, authorization));
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }


    private TargetInstance translateInstance(DatabaseScope databaseScope, boolean liveMode, String org, String domain, String schema, String version, String id, String authorization) {
        String type = String.format("%s/%s/%s/%s", org, domain, schema, version);
        String query = String.format("query/%s/search", type);
        switch (type) {
            case "minds/core/dataset/v1.0.0":
                return translationController.createDataset(databaseScope, liveMode, query, id, authorization);
            case "minds/core/person/v1.0.0":
                return translationController.createContributor(databaseScope, liveMode, query, id, authorization);
            case "uniminds/core/person/v1.0.0":
                return translationController.createContributor(databaseScope, liveMode, query, id, authorization);
            case "minds/core/placomponent/v1.0.0":
                return translationController.createProject(databaseScope, liveMode, query, id, authorization);
            case "uniminds/core/modelinstance/v1.0.0":
                return translationController.createModel(databaseScope, liveMode, query, id, authorization);
            case "softwarecatalog/software/softwareproject/v1.0.0":
                return translationController.createSoftware(databaseScope, liveMode, type, id, authorization);
            case "minds/experiment/subject/v1.0.0":
                return translationController.createSubject(databaseScope, liveMode, query, id, authorization);
        }
        throw new HttpClientErrorException(HttpStatus.NOT_FOUND, String.format("Type %s is not recognized as a valid search resource!", type));
    }
}
