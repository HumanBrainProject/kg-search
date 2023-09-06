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
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.*;

import static eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.ISODateValue.ISO_DATE_PATTERN;

@Getter
public class TranslatorUtils {

    private final DOICitationFormatter doiCitationFormatter;
    private final ESServiceClient esServiceClient;
    private final Integer trendingThreshold;

    private final Map<String, Object> translationContext;

    private final List<String> errors;


    public TranslatorUtils(DOICitationFormatter doiCitationFormatter, ESServiceClient esServiceClient, Integer trendingThreshold, Map<String, Object> translationContext, List<String> errors) {
        this.doiCitationFormatter = doiCitationFormatter;
        this.esServiceClient = esServiceClient;
        this.trendingThreshold = trendingThreshold;
        this.translationContext = translationContext;
        this.errors = errors != null ? errors : new ArrayList<>();
    }

    public <T extends HasBadges & HasTrendingInformation> void defineBadgesAndTrendingState(T target, String issueDate, Date firstRelease, Integer last30DaysViews, List<String> metaBadges) {
        List<String> badges = new ArrayList<>();
        if (isNew(issueDate, firstRelease)) {
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
        badges.addAll(metaBadges);
        target.setBadges(badges);
    }

    private Date getIssueDate(String issueDate) {
        if (StringUtils.isNotBlank(issueDate)) {
            try {
                LocalDate localDate = LocalDate.parse(issueDate);
                ZoneId defaultZoneId = ZoneId.systemDefault();
                return Date.from(localDate.atStartOfDay(defaultZoneId).toInstant());
            } catch (DateTimeParseException e) {
                return null;
            }
        }
        return null;
    }

    public String getReleasedDateForSorting(String issueDate, Date releaseDate) {
        if (getIssueDate(issueDate) != null) {
            return issueDate;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(ISO_DATE_PATTERN);
        return dateFormat.format(releaseDate);
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

    private static final int IS_NEW_THRESHOLD = 8;
    private boolean isDateAfterThreshold(Date date) {
        Calendar cal = new GregorianCalendar();
        cal.add(Calendar.DAY_OF_MONTH, -TranslatorUtils.IS_NEW_THRESHOLD);
        Date daysAgo = cal.getTime();
        return date.after(daysAgo);
    }

    private boolean isNew(String issueDate, Date firstRelease) {
        Date date = getIssueDate(issueDate);
        if (date != null) {
            return isDateAfterThreshold(date);
        } else if(firstRelease !=null) {
            return isDateAfterThreshold(firstRelease);
        }
        return false;
    }
}
