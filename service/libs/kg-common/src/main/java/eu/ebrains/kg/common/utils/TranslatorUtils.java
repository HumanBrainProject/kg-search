package eu.ebrains.kg.common.utils;

import eu.ebrains.kg.common.model.ErrorReport;
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
