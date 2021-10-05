/*
 * Copyright 2018 - 2021 Swiss Federal Institute of Technology Lausanne (EPFL)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This open source software code was developed in part or in whole in the
 * Human Brain Project, funded from the European Union's Horizon 2020
 * Framework Programme for Research and Innovation under
 * Specific Grant Agreements No. 720270, No. 785907, and No. 945539
 * (Human Brain Project SGA1, SGA2 and SGA3).
 *
 */

package eu.ebrains.kg.search.configuration;

import eu.ebrains.kg.search.model.TranslatorModel;
import eu.ebrains.kg.search.services.KGV3ServiceClient;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class Setup {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final KGV3ServiceClient kgv3ServiceClient;
    private final boolean uploadQueries;

    public Setup(KGV3ServiceClient kgv3ServiceClient, @Value("${UPLOAD_QUERIES:true}") boolean uploadQueries) {
        this.kgv3ServiceClient = kgv3ServiceClient;
        this.uploadQueries = uploadQueries;
    }

    @PostConstruct
    public void uploadQueries() {
        if(uploadQueries) {
            logger.info("Now uploading queries for search...");
            TranslatorModel.MODELS.parallelStream().map(TranslatorModel::getV3translator).filter(Objects::nonNull).forEach(t -> {
                final String simpleName = t.getClass().getSimpleName();
                final String filename = StringUtils.uncapitalize(simpleName.substring(0, simpleName.indexOf("V3")));
                try{
                    String payload = loadQuery(filename);
                    for (String semanticType : t.semanticTypes()) {
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
        else{
            logger.info("Uploading queries has been skipped by property");
        }
    }

    private String loadQuery(String fileName) throws IOException {
        return IOUtils.toString(Objects.requireNonNull(this.getClass().getResourceAsStream(String.format("/queries/%s.json", fileName))), StandardCharsets.UTF_8);
    }


    private void uploadQuery(String queryId, String path) throws IOException {

        kgv3ServiceClient.uploadQuery(queryId, loadQuery(path));
    }

}
