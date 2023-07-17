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

package eu.ebrains.kg.common.utils;

import eu.ebrains.kg.common.model.elasticsearch.Document;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.HasBadges;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.HasTrendingInformation;
import eu.ebrains.kg.common.services.DOICitationFormatter;
import eu.ebrains.kg.common.services.ESServiceClient;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.*;

@Getter
public class TranslatorUtils {

    private final DOICitationFormatter doiCitationFormatter;
    private final ESServiceClient esServiceClient;
    private final Integer trendingThreshold;

    private final List<String> errors;


    public TranslatorUtils(DOICitationFormatter doiCitationFormatter, ESServiceClient esServiceClient, Integer trendingThreshold, List<String> errors) {
        this.doiCitationFormatter = doiCitationFormatter;
        this.esServiceClient = esServiceClient;
        this.trendingThreshold = trendingThreshold;
        this.errors = errors != null ? errors : new ArrayList<>();
    }

    public <T extends HasBadges & HasTrendingInformation> void defineBadgesAndTrendingState(T target, Date firstRelease, Integer last30DaysViews) {
        List<String> badges = new ArrayList<>();
        if (isNew(firstRelease)) {
            badges.add("isNew");
        }
        if(last30DaysViews != null){
            target.setLast30DaysViews(last30DaysViews);
        }
        if (this.trendingThreshold != null && this.trendingThreshold > 0 && last30DaysViews != null) {
            if (last30DaysViews >= this.trendingThreshold) {
                badges.add("isTrending");
                target.setTrending(true);
            }
        }
        target.setBadges(badges);
    }

    public Document getResource(String  id){
        try {
            return this.esServiceClient.getDocumentByNativeId(ESHelper.getResourcesIndex(), id);
        }
        catch (WebClientResponseException e){
            if(e.getStatusCode() == HttpStatus.NOT_FOUND){
                return null;
            }
            throw e;
        }
    }

    private boolean isNew(Date firstRelease) {
        if(firstRelease !=null) {
            Calendar cal = new GregorianCalendar();
            cal.add(Calendar.DAY_OF_MONTH, -8);
            Date daysAgo = cal.getTime();
            return firstRelease.after(daysAgo);
        }
        else{
            return false;
        }
    }
}
