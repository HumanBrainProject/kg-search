package eu.ebrains.kg.common.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * A component which allows to pass configuration information such as feature toggles, etc.
 */
@Component
@Getter
public class Configuration {

    public static Configuration defaultConfiguration(){
        return new Configuration();
    }
}
