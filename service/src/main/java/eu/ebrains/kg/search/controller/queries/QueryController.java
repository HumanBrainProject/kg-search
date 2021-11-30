package eu.ebrains.kg.search.controller.queries;

import eu.ebrains.kg.search.model.TranslatorModel;
import eu.ebrains.kg.search.services.KGV3ServiceClient;
import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class QueryController {


    private final KGV3ServiceClient kgv3ServiceClient;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public QueryController(KGV3ServiceClient kgv3ServiceClient) {
        this.kgv3ServiceClient = kgv3ServiceClient;
    }

    @Async
    public void uploadQueries(){
        logger.info("Now uploading queries for search...");
        TranslatorModel.MODELS.parallelStream().map(TranslatorModel::getV3translator).filter(Objects::nonNull).forEach(t -> {
            try{
                for (String semanticType : t.semanticTypes()) {
                    String filename = t.getQueryFileName(semanticType);
                    String payload = loadQuery(filename);
                    final String queryId = t.getQueryIdByType(semanticType);
                    logger.info(String.format("Uploading query %s from file %s for type %s", queryId, filename, semanticType));
                    Map<String, Object> properties = new HashMap<>();
                    properties.put("type", semanticType);
                    kgv3ServiceClient.uploadQuery(queryId, StringSubstitutor.replace(payload, properties));
                }
            }
            catch (IOException e){
                throw new RuntimeException(e);
            }
        });
        logger.info("Queries successfully uploaded!");
    }


    private String loadQuery(String fileName) throws IOException {
        return IOUtils.toString(Objects.requireNonNull(this.getClass().getResourceAsStream(String.format("/queries/%s.json", fileName))), StandardCharsets.UTF_8);
    }

}
