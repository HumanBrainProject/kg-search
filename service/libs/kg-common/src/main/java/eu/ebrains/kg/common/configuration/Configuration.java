package eu.ebrains.kg.common.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class Configuration {

    private final boolean showHierarchicalSpecimen;

    public Configuration(@Value("${eu.ebrains.kg.search.features.showHierarchicalSpecimen:false}") boolean showHierarchicalSpecimen) {
        this.showHierarchicalSpecimen = showHierarchicalSpecimen;
    }

    public static Configuration defaultConfiguration(){
        return new Configuration(false);
    }
}
