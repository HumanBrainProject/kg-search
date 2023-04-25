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

package eu.ebrains.kg.indexing.controller.metrics;

import eu.ebrains.kg.common.model.DataStage;
import eu.ebrains.kg.common.model.elasticsearch.Result;
import eu.ebrains.kg.common.services.ESServiceClient;
import eu.ebrains.kg.common.utils.ESHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class MetricsController {

    public final int MAX_NUMBER_OF_TRENDING_INSTANCES = 5;
    public final int MINIMAL_NUMBER_OF_VISITS_TO_BE_REGARDED_TRENDING = 10;
    private final ESServiceClient esServiceClient;

    public MetricsController(
            ESServiceClient esServiceClient
    ) {
        this.esServiceClient = esServiceClient;
    }

    public Integer getTrendThreshold(Class<?> clazz, DataStage stage) {
        try {
            String index = ESHelper.getSearchableIndex(stage, clazz, false);
            Result result = esServiceClient.getMetrics(index, MAX_NUMBER_OF_TRENDING_INSTANCES+1); //We need one instance more to check if it's exactly the limit or less
            if (result == null || result.getHits() == null || CollectionUtils.isEmpty(result.getHits().getHits())) {
                return null;
            }
            final List<Integer> last30dayViews = result.getHits().getHits().stream().map(d -> {
                final Map<String, Object> fields = d.getFields();
                if(fields!=null && fields.get("last30DaysViews") instanceof List){
                    final List<?> last30DaysViews = (List<?>) fields.get("last30DaysViews");
                    if(!last30DaysViews.isEmpty() && last30DaysViews.get(0) instanceof Integer){
                        return (Integer)last30DaysViews.get(0);
                    }
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
            if(last30dayViews.size()==MAX_NUMBER_OF_TRENDING_INSTANCES+1){
                //The result is complete (we have equal or more instances than the set limit
                if(last30dayViews.get(MAX_NUMBER_OF_TRENDING_INSTANCES).equals(last30dayViews.get(MAX_NUMBER_OF_TRENDING_INSTANCES-1))){
                    //We can ignore the last item
                    last30dayViews.remove(MAX_NUMBER_OF_TRENDING_INSTANCES);
                }
            }
            final List<Integer> distinctSortedList = last30dayViews.stream().sorted().distinct().collect(Collectors.toList());
            if(distinctSortedList.size()>1){
                //We're interested in the second smallest number of the list.
                final Integer threshold = distinctSortedList.get(1);
                return threshold>=MINIMAL_NUMBER_OF_VISITS_TO_BE_REGARDED_TRENDING ? threshold : MINIMAL_NUMBER_OF_VISITS_TO_BE_REGARDED_TRENDING;
            }
            return null;
        } catch (WebClientResponseException e) {
            return null;
        }
    }

}
