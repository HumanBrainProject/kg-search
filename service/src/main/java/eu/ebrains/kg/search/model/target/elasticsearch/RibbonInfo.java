package eu.ebrains.kg.search.model.target.elasticsearch;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RibbonInfo {
    String content();
    String aggregation();
    String dataField();
    String singular();
    String plural();
}