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
        logger.info("Queries successfully uploaded!");
    }

    private void uploadQuery(String queryId, String path) throws IOException {
        String sourceJson = IOUtils.toString(this.getClass().getResourceAsStream(path), StandardCharsets.UTF_8);
        kgv3ServiceClient.uploadQuery(queryId, sourceJson);
        kgv3ServiceClient.releaseQuery(queryId);
    }

}
