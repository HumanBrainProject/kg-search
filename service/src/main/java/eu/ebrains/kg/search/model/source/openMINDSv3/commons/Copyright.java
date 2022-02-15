package eu.ebrains.kg.search.model.source.openMINDSv3.commons;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Copyright  {
    private String year;
    List<PersonOrOrganizationRef> holder;
}
