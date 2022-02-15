package eu.ebrains.kg.search.model.source.openMINDSv3.commons;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NameWithIdentifier {
    private List<String> identifier;
    private String name;
}