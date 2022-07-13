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

package eu.ebrains.kg.search.controller.metrics;

import eu.ebrains.kg.common.model.DataStage;
import eu.ebrains.kg.common.model.elasticsearch.Result;
import eu.ebrains.kg.common.services.ESServiceClient;
import eu.ebrains.kg.common.utils.ESHelper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MetricsController {

    private final ESServiceClient esServiceClient;

    public MetricsController(
            ESServiceClient esServiceClient
    ) {
        this.esServiceClient = esServiceClient;
    }

    @Cacheable(value = "trendThresholdByType", unless = "#result == null || #type == null ", key = "#type")
    public Integer getTrendThreshold(Class<?> clazz, String type) { // type is use as key for @Cachable
        try {
            String index = ESHelper.getSearchableIndex(DataStage.RELEASED, clazz, false);
            Result result = esServiceClient.getMetrics(index);

            if (
                    result == null ||
                    CollectionUtils.isEmpty(result.getAggregations()) ||
                            !result.getAggregations().containsKey("last30DaysViews") ||
                            CollectionUtils.isEmpty(result.getAggregations().get("last30DaysViews").getBuckets())

            ) {
                return null;
            }
            List<Integer> list = result.getAggregations().get("last30DaysViews").getBuckets().stream().map(b -> {
                if (b.getKey() != null) {
                    try {
                        return Integer.parseInt(b.getKey());

                    } catch (NumberFormatException ignored) {
                    }
                }
                return 0;
            }).collect(Collectors.toList());

            if (list.size() >= 10) {
                return list.get(9);
            }
            return 0;
        } catch (WebClientResponseException e) {
            return null;
        }
    }

}
