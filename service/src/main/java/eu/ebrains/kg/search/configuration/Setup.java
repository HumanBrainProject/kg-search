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

import eu.ebrains.kg.search.constants.Queries;
import eu.ebrains.kg.search.services.KGV3ServiceClient;
import org.apache.commons.io.IOUtils;
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
import java.util.UUID;

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
    public void uploadQueries() throws IOException {
        if(uploadQueries) {
            logger.info("Now uploading queries for search...");
            uploadQuery(Queries.DATASET_QUERY_ID, Queries.DATASET_QUERY_RESOURCE);
            uploadQuery(Queries.DATASET_VERSION_QUERY_ID, Queries.DATASET_VERSION_QUERY_RESOURCE);
            uploadQuery(Queries.DATASET_VERSION_IDENTIFIER_QUERY_ID, Queries.DATASET_VERSION_IDENTIFIER_QUERY_RESOURCE);
            uploadQuery(Queries.CONTRIBUTOR_QUERY_ID, Queries.CONTRIBUTOR_QUERY_RESOURCE);
            uploadQuery(Queries.CONTRIBUTOR_IDENTIFIER_QUERY_ID, Queries.CONTRIBUTOR_IDENTIFIER_QUERY_RESOURCE);
            uploadQuery(Queries.SOFTWARE_QUERY_ID, Queries.SOFTWARE_QUERY_RESOURCE);
            uploadQuery(Queries.SOFTWARE_VERSION_QUERY_ID, Queries.SOFTWARE_VERSION_QUERY_RESOURCE);
            uploadQuery(Queries.SOFTWARE_VERSION_IDENTIFIER_QUERY_ID, Queries.SOFTWARE_VERSION_IDENTIFIER_QUERY_RESOURCE);
            uploadQuery(Queries.MODEL_QUERY_ID, Queries.MODEL_QUERY_RESOURCE);
            uploadQuery(Queries.MODEL_VERSION_QUERY_ID, Queries.MODEL_VERSION_QUERY_RESOURCE);
            uploadQuery(Queries.MODEL_VERSION_IDENTIFIER_QUERY_ID, Queries.MODEL_VERSION_IDENTIFIER_QUERY_RESOURCE);
            uploadQuery(Queries.PROJECT_QUERY_ID, Queries.PROJECT_QUERY_RESOURCE);
            uploadQuery(Queries.PROJECT_IDENTIFIER_QUERY_ID, Queries.PROJECT_IDENTIFIER_QUERY_RESOURCE);
            uploadQuery(Queries.SAMPLE_QUERY_ID, Queries.SAMPLE_QUERY_RESOURCE);
            uploadQuery(Queries.SAMPLE_IDENTIFIER_QUERY_ID, Queries.SAMPLE_IDENTIFIER_QUERY_RESOURCE);
            uploadQuery(Queries.SUBJECT_QUERY_ID, Queries.SUBJECT_QUERY_RESOURCE);
            uploadQuery(Queries.SUBJECT_IDENTIFIER_QUERY_ID, Queries.SUBJECT_IDENTIFIER_QUERY_RESOURCE);
            uploadQuery(Queries.FILE_REPOSITORY_QUERY_ID, Queries.FILE_REPOSITORY_QUERY_RESOURCE);
            uploadQuery(Queries.FILE_QUERY_ID, Queries.FILE_QUERY_RESOURCE);
            for (String controlledTermType : Queries.CONTROLLED_TERM_TYPES) {
                uploadSharedQueryTemplate(Queries.CONTROLLED_TERM_QUERY_ID, controlledTermType, Queries.CONTROLLED_TERM_QUERY_RESOURCE);
            }
            logger.info("Queries successfully uploaded!");
        }
        else{
            logger.info("Uploading queries has been skipped by property");
        }
    }


    private void uploadSharedQueryTemplate(String queryId, String type, String path) throws IOException {
        String specificQueryId = Queries.getTemplateQueryId(queryId, type);
        String query = loadQuery(path);
        Map<String, Object> properties = new HashMap<>();
        properties.put("type", type);
        logger.info(String.format("Uploading template query %s for type %s with id %s...", path, type, specificQueryId));
        kgv3ServiceClient.uploadQuery(specificQueryId, StringSubstitutor.replace(query, properties));
        kgv3ServiceClient.releaseQuery(specificQueryId);
    }

    private String loadQuery(String path) throws IOException {
        return IOUtils.toString(Objects.requireNonNull(this.getClass().getResourceAsStream(path)), StandardCharsets.UTF_8);
    }


    private void uploadQuery(String queryId, String path) throws IOException {
        logger.info(String.format("Uploading %s with id %s...", path, queryId));
        kgv3ServiceClient.uploadQuery(queryId, loadQuery(path));
        kgv3ServiceClient.releaseQuery(queryId);
    }

}
