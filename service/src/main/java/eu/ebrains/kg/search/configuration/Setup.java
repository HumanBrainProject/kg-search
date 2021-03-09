package eu.ebrains.kg.search.configuration;

import eu.ebrains.kg.search.constants.Queries;
import eu.ebrains.kg.search.services.KGV3ServiceClient;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class Setup {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private KGV3ServiceClient kgv3ServiceClient;

    public Setup(KGV3ServiceClient kgv3ServiceClient) {
        this.kgv3ServiceClient = kgv3ServiceClient;
    }

    @PostConstruct
    public void uploadQueries() throws IOException {
        uploadQuery(Queries.DATASET_QUERY_ID, Queries.DATASET_QUERY_RESOURCE);
        uploadQuery(Queries.CONTRIBUTOR_QUERY_ID, Queries.CONTRIBUTOR_QUERY_RESOURCE);
        logger.info("Queries successfully uploaded!");
    }

    private void uploadQuery(String queryId, String path) throws IOException {
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream(path), StandardCharsets.UTF_8);
        kgv3ServiceClient.uploadQuery(queryId, sourceJson);
    }

}
