package eu.ebrains.kg.search.api;

import eu.ebrains.kg.search.boundary.IndexingOrchestration;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/indexing")
public class Indexing {

    private final IndexingOrchestration indexingOrchestration;

    public Indexing(IndexingOrchestration indexingOrchestration) {
        this.indexingOrchestration = indexingOrchestration;
    }

    @PostMapping("/full")
    public void fullReplacement(){

    }

    @PostMapping("/incremental")
    public void incrementalUpdate(){

    }

    @PostMapping("/full/{id}")
    public void fullReplacementOfInstance(@PathVariable("id") String id){

    }

    @PostMapping("/incremental/{id}")
    public void incrementalUpdateOfInstance(@PathVariable("id") String id){

    }




}
