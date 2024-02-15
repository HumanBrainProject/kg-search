/*
 *  Copyright 2018 - 2021 Swiss Federal Institute of Technology Lausanne (EPFL)
 *  Copyright 2021 - 2023 EBRAINS AISBL
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0.
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  This open source software code was developed in part or in whole in the
 *  Human Brain Project, funded from the European Union's Horizon 2020
 *  Framework Programme for Research and Innovation under
 *  Specific Grant Agreements No. 720270, No. 785907, and No. 945539
 *  (Human Brain Project SGA1, SGA2 and SGA3).
 */

package eu.ebrains.kg.indexing.controller.queries;

import eu.ebrains.kg.common.controller.translation.TranslatorRegistry;
import eu.ebrains.kg.common.controller.translation.models.TranslatorModel;
import eu.ebrains.kg.common.services.KGServiceClient;
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


    private final KGServiceClient kgv3ServiceClient;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final TranslatorRegistry translatorRegistry;

    public QueryController(KGServiceClient kgv3ServiceClient, TranslatorRegistry translatorRegistry) {
        this.kgv3ServiceClient = kgv3ServiceClient;
        this.translatorRegistry = translatorRegistry;
    }

    @Async
    public void uploadQueries(){
        logger.info("Now uploading queries for search...");
        translatorRegistry.getTranslators().parallelStream().map(TranslatorModel::getTranslator).filter(Objects::nonNull).forEach(t -> {
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
