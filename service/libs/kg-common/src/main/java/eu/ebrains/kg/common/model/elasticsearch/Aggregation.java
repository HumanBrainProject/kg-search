package eu.ebrains.kg.common.model.elasticsearch;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Aggregation extends ValueAgg {

    private KeywordsAgg keywords;
    private Total total;
    private Inner inner;

    @Getter
    @Setter
    public static class Total {

        private Integer value;
    }

    @Getter
    @Setter
    public static class Inner {

        private KeywordsAgg keywords;
    }
}
