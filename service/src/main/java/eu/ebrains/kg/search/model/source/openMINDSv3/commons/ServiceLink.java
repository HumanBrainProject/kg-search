package eu.ebrains.kg.search.model.source.openMINDSv3.commons;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceLink extends ExternalRef{
    private String service;

    public String displayLabel(){
        return String.format("Open %s in %s", this.getLabel(), this.getService());
    }
}
