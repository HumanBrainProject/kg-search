package eu.ebrains.kg.search.boundary;

import eu.ebrains.kg.search.controller.indexers.KGv2Indexer;
import eu.ebrains.kg.search.controller.indexers.KGv3Indexer;
import org.springframework.stereotype.Component;

@Component
public class IndexingOrchestration {

    private final KGv2Indexer kgv2Indexer;
    private final KGv3Indexer kgv3Indexer;

    public IndexingOrchestration(KGv2Indexer kgv2Indexer, KGv3Indexer kgv3Indexer) {
        this.kgv2Indexer = kgv2Indexer;
        this.kgv3Indexer = kgv3Indexer;
    }




}
