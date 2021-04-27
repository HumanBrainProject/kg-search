package eu.ebrains.kg.search.controller.kg;

import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.ResultsOfKGv3;
import eu.ebrains.kg.search.model.source.openMINDSv3.SourceInstanceV3;
import eu.ebrains.kg.search.services.KGV3ServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Component
public class KGv3 {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final KGV3ServiceClient kgServiceClient;

    private final int PAGE_SIZE = 20;

    public KGv3(KGV3ServiceClient kgServiceClient) {
        this.kgServiceClient = kgServiceClient;
    }

    public <T> T executeQueryForIndexing(Class<T> clazz, DataStage dataStage, String queryId, int from, int size){
        return kgServiceClient.executeQueryForIndexing(clazz, dataStage, queryId, from, size);
    }

    private static class ResultsOfKGV3Source extends ResultsOfKGv3<SourceInstanceV3> {}

    public List<SourceInstanceV3> executeQueryForIndexing(DataStage dataStage, String queryId){
        List<SourceInstanceV3> result = new ArrayList<>();
        boolean findMore = true;
        int from = 0;
        while (findMore) {
            logger.debug(String.format("Starting to query %d instances from %d for v3", PAGE_SIZE, from));
            ResultsOfKGV3Source page = kgServiceClient.executeQueryForIndexing(ResultsOfKGV3Source.class, dataStage, queryId, from, PAGE_SIZE);
            logger.debug(String.format("Successfully queried %d instances from %d of a total of %d for v3", page.getData().size(), from, page.getTotal()));
            result.addAll(page.getData());
            from = page.getFrom() + page.getSize();
            findMore = from < page.getTotal();
        }
        return result;
    }

    public <T> T executeQueryForIndexing(Class<T> clazz, DataStage dataStage, String queryId, String id) {
        return  kgServiceClient.executeQueryForIndexing(clazz, dataStage, queryId, id);
    }

    public <T> T executeQuery(Class<T> clazz, DataStage dataStage, String queryId, String id) {
        return  kgServiceClient.executeQuery(clazz, dataStage, queryId, id);
    }

    public Map fetchInstance(String id, DataStage dataStage) {
        return  kgServiceClient.getInstance(id, dataStage);
    }

}
