package eu.ebrains.kg.search.configuration;

import eu.ebrains.kg.search.controller.indexing.IndexingController;
import eu.ebrains.kg.search.model.DatabaseScope;
import eu.ebrains.kg.search.model.source.SourceInstance;
import eu.ebrains.kg.search.model.source.openMINDSv1.DatasetV1;
import eu.ebrains.kg.search.model.source.openMINDSv1.PersonV1;
import eu.ebrains.kg.search.model.source.openMINDSv1.ProjectV1;
import eu.ebrains.kg.search.model.source.openMINDSv1.SubjectV1;
import eu.ebrains.kg.search.model.source.openMINDSv2.ModelV2;
import eu.ebrains.kg.search.model.source.openMINDSv2.PersonV2;
import eu.ebrains.kg.search.model.source.openMINDSv2.SoftwareV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
