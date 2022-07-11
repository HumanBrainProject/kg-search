package eu.ebrains.kg.common.model.elasticsearch;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Suggestion {
    private String text;
    private Integer offset;
    private Integer length;
    private List<Option> options;

    @Getter
    @Setter
    public static class Option {
        private String text;
        private Double score;
        private Integer freq;
    }
}
