package eu.ebrains.kg.common.utils;

import eu.ebrains.kg.common.model.target.elasticsearch.instances.HasBadges;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.HasTrendingInformation;
import eu.ebrains.kg.common.services.DOICitationFormatter;
import lombok.Getter;

import java.util.*;

@Getter
public class TranslatorUtils {

    private final DOICitationFormatter doiCitationFormatter;
    private final Integer trendingThreshold;

    public TranslatorUtils(DOICitationFormatter doiCitationFormatter, Integer trendingThreshold) {
        this.doiCitationFormatter = doiCitationFormatter;
        this.trendingThreshold = trendingThreshold;
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
