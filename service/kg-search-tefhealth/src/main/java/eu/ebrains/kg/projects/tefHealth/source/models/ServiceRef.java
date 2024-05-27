package eu.ebrains.kg.projects.tefHealth.source.models;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ServiceRef extends NameRefWithAbbreviation {

    private List<NameRef> categories;

}