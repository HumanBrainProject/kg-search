package eu.ebrains.kg.search.controller;

import eu.ebrains.kg.search.boundary.IndexingOrchestration;
import eu.ebrains.kg.search.controller.indexers.KGv2Indexer;
import eu.ebrains.kg.search.controller.indexers.KGv3Indexer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class IndexingOrchestrationTest {

    @Test
    public void testIndexingOrchestration(){
        IndexingOrchestration indexingOrchestration = new IndexingOrchestration(Mockito.mock(KGv2Indexer.class), Mockito.mock(KGv3Indexer.class));



    }


}