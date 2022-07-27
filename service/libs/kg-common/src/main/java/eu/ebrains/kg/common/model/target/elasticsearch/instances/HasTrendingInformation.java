package eu.ebrains.kg.common.model.target.elasticsearch.instances;

public interface HasTrendingInformation {

    void setTrending(boolean trending);
    int getLast30DaysViews();
    void setLast30DaysViews(int last30DaysViews);
}
