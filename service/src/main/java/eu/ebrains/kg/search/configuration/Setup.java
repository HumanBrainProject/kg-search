package eu.ebrains.kg.search.configuration;

import eu.ebrains.kg.search.controller.indexing.IndexingController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class Setup {

    private IndexingController indexingController;

    public Setup(IndexingController indexingController) {
        this.indexingController = indexingController;
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void uploadQueries() {
        System.out.println("Upload the queries please");
    }

}
