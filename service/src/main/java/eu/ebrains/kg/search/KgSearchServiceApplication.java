package eu.ebrains.kg.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
public class KgSearchServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(KgSearchServiceApplication.class, args);
    }

}
