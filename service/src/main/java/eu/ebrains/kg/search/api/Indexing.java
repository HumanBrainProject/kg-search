package eu.ebrains.kg.search.api;

import eu.ebrains.kg.search.boundary.IndexingOrchestration;
import eu.ebrains.kg.search.model.DatabaseScope;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/indexing")
@RestController
public class Indexing {

    private final IndexingOrchestration indexingOrchestration;

    public Indexing(IndexingOrchestration indexingOrchestration) {
        this.indexingOrchestration = indexingOrchestration;
    }

    @PostMapping("/full")
    public void fullReplacement(@RequestParam("databaseScope") DatabaseScope databaseScope) {

    }

    @PostMapping("/full/{type}")
    public void fullReplacementByType(@RequestParam("databaseScope") DatabaseScope databaseScope, @PathVariable("type") String type) {

    }

    @PostMapping("/incremental")
    public void incrementalUpdate(@RequestParam("databaseScope") DatabaseScope databaseScope) {

    }

    @PostMapping("/incremental/{id}")
    public void incrementalUpdateOfInstance(@RequestParam("databaseScope") DatabaseScope databaseScope, @PathVariable("id") String id) {

    }


}
