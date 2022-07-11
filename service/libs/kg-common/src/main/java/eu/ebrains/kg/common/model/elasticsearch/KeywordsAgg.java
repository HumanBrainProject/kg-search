package eu.ebrains.kg.common.model.elasticsearch;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class KeywordsAgg extends Agg {

    private List<KeywordsBucket> buckets;

}
