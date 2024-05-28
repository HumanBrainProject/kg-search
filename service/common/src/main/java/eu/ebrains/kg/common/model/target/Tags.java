package eu.ebrains.kg.common.model.target;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Tags {
    public Tags(List<String> data, int total, int size, int from) {
        this.data = data;
        this.total = total;
        this.size = size;
        this.from = from;
    }
    private List<String> data;
    private int total;
    private int size;
    private int from;
}