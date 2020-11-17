package eu.ebrains.kg.search.model.target.elasticsearch;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FieldInfo {
    String label() default "";
    String hint() default "";
    boolean optional() default true;
    boolean sort() default false;
    boolean visible() default true;
    boolean labelHidden() default false;
    boolean markdown() default false;
    boolean overview() default false;
    boolean ignoreForSearch() default false;
    boolean isButton() default false;
    boolean termsOfUse() default false;
    Type type() default Type.UNDEFINED;
    Layout layout() default Layout.UNDEFINED;
    String linkIcon() default "";
    String tagIcon() default "";
    String separator() default "";
    float boost() default 1.0f;
    Facet facet() default Facet.UNDEFINED;
    FacetOrder facetOrder() default  FacetOrder.UNDEFINED;
    Aggregate aggregate() default Aggregate.UNDEFINED;


    enum Facet{
        UNDEFINED, EXISTS, LIST
    }

    enum FacetOrder{
        UNDEFINED, BYCOUNT, BYVALUE
    }

    enum Aggregate{
        UNDEFINED, COUNT
    }

    enum Type{
        UNDEFINED, TEXT, DATE
    }

    enum Layout{
        UNDEFINED, GROUP, HEADER, SUMMARY
    }
}
