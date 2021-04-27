package eu.ebrains.kg.search.controller.kg;

import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.ResultsOfKGv2;
import eu.ebrains.kg.search.model.source.SourceInstanceIdentifierV1andV2;
import eu.ebrains.kg.search.services.KGServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class KGv2 {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final KGServiceClient kgServiceClient;

    private final int PAGE_SIZE = 20;

    public KGv2(KGServiceClient kgServiceClient) {
        this.kgServiceClient = kgServiceClient;
    }

    public <T> T executeQueryForIndexing(Class<T> clazz, DataStage dataStage, String query, String authorization){
        return kgServiceClient.executeQueryForIndexing(query, dataStage, clazz, authorization);
    }

    private static class ResultsOfKGV2Source extends ResultsOfKGv2<SourceInstanceIdentifierV1andV2> {}

    public List<SourceInstanceIdentifierV1andV2> executeQueryForIndexing(DataStage dataStage, String query, String authorization){
        List<SourceInstanceIdentifierV1andV2> result = new ArrayList<>();
        boolean findMore = true;
        int from = 0;
        while (findMore) {
            logger.debug(String.format("Starting to query %d instances from %d for v1", PAGE_SIZE, from));
            ResultsOfKGV2Source page = kgServiceClient.executeQueryForIndexing(query, dataStage, ResultsOfKGV2Source.class, authorization, from, PAGE_SIZE);
            logger.debug(String.format("Successfully queried %d instances from %d of a total of %d for v1", page.getResults().size(), from, page.getTotal()));
            result.addAll(page.getResults());
            from = page.getStart() + page.getSize();
            findMore = from < page.getTotal();
        }
        return result;
    }

    public <T> T executeQuery(Class<T> clazz, DataStage dataStage, String query, String id, String authorization) {
        return  kgServiceClient.executeQuery(query, id, dataStage, clazz, authorization);
    }

    public <T> T executeQueryByIdentifier(Class<T> clazz, DataStage dataStage, String query, String identifier, String authorization) {
        return  kgServiceClient.executeQueryByIdentifier(query, identifier, dataStage, clazz, authorization);
    }

}
