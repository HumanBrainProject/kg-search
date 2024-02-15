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
import eu.ebrains.kg.common.model.target.HasBadges;
import eu.ebrains.kg.common.model.target.HasTrendingInformation;
import eu.ebrains.kg.common.services.DOICitationFormatter;
import eu.ebrains.kg.common.services.ESServiceClient;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.*;

import static eu.ebrains.kg.common.model.target.ISODateValue.ISO_DATE_PATTERN;

@Getter
public class TranslatorUtils {

    public static final String IS_NEW_BADGE = "isNew";
    public static final String IS_TRENDING_BADGE = "isTrending";

    private final DOICitationFormatter doiCitationFormatter;
    private final ESServiceClient esServiceClient;
    private final Integer trendingThreshold;
    private final ESHelper esHelper;

    private final Map<String, Object> translationContext;

    private final List<String> errors;


    public TranslatorUtils(DOICitationFormatter doiCitationFormatter, ESServiceClient esServiceClient, Integer trendingThreshold, Map<String, Object> translationContext, List<String> errors, ESHelper esHelper) {
        this.doiCitationFormatter = doiCitationFormatter;
        this.esServiceClient = esServiceClient;
        this.trendingThreshold = trendingThreshold;
        this.translationContext = translationContext;
        this.esHelper = esHelper;
        this.errors = errors != null ? errors : new ArrayList<>();
    }



    public <T extends HasBadges & HasTrendingInformation> void defineBadgesAndTrendingState(T target, String issueDate, Date firstRelease, Integer last30DaysViews, List<String> metaBadges) {
        List<String> badges = new ArrayList<>();
        if (isNew(issueDate, firstRelease)) {
            badges.add(IS_NEW_BADGE);
        }
        if(last30DaysViews != null){
            target.setLast30DaysViews(last30DaysViews);
        }
        if (this.trendingThreshold != null && this.trendingThreshold > 0 && last30DaysViews != null) {
            if (last30DaysViews >= this.trendingThreshold) {
                badges.add(IS_TRENDING_BADGE);
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
        if(releaseDate == null) {
            return null;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(ISO_DATE_PATTERN);
        return dateFormat.format(releaseDate);
    }

    public Document getResource(String  id){
        try {
            return this.esServiceClient.getDocumentByNativeId(esHelper.getResourcesIndex(), id);
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

    public static String createQueryBuilderText(String type, String id){
        try {
            return String.format("""
                        To make programmatic use of the (meta-)data of EBRAINS, you have different options to interact with the <a href="https://kg.ebrains.eu" target="_blank">EBRAINS Knowledge Graph (KG)</a>:
                        
                        <b>KG Query Builder</b>
                        With the <a href="https://query.kg.ebrains.eu" target="_blank">KG Query Builder</a>, you can design your own query to retrieve metadata for this instance and those of the same type conveniently via UI without the requirement of learning a graph query language (see the <a href="https://docs.kg.ebrains.eu/9b511d36d7608eafc94ea43c918f16b6/tutorials.html" target="_blank">tutorial</a>). You can also save the query and use it with the REST-API and the KG Core SDKs (see below). 
                        
                        <a href="https://query.kg.ebrains.eu/queries?type=%s&instanceId=%s" class="btn btn-secondary" style="color:#fff" target="_blank">Build a query for this instance</a>
                        
                        
                        <b>KG REST-API</b>
                        You can use the <a href="https://core.kg.ebrains.eu/swagger-ui.html" target="_blank">KG REST-API</a> to access and/or manipulate metadata on the EBRAINS KG as well as to run queries saved previously in the KG Query Builder.

                        <b>KG Core SDKs</b>
                        The <a href="https://github.com/HumanBrainProject/kg-core-sdks" target="_blank">KG Core SDKs</a> provide convenient ways to authenticate and make use of the functionality of the KG REST-API with your favorite programming language (currently available for Python and JavaScript/TypeScript).
                       
                        <b>fairgraph</b>
                        <a href="https://fairgraph.readthedocs.io" _target="blank">fairgraph</a> is a python library which enriches the KG Core Python SDK with knowledge about the metadata structure of choice at EBRAINS called <a href="https://openminds.ebrains.eu" _target="blank">openMINDS</a> and provides additional mechanisms to further simplify the interaction and manipulation of metadata instances on the EBRAINS KG.
                        """, URLEncoder.encode(type, StandardCharsets.UTF_8.toString()), id);

        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
}
