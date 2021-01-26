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
    String icon() default "";
    boolean optional() default true;
    boolean sort() default false;
    boolean visible() default true;
    boolean labelHidden() default false;
    boolean markdown() default false;
    boolean overview() default false;
    boolean ignoreForSearch() default false;
    boolean isButton() default false;
    boolean termsOfUse() default false;
    boolean isFilterableFacet() default false;
    boolean isHierarchicalFiles() default false;
    boolean groupBy() default false;
    boolean isTable() default false;
    Type type() default Type.UNDEFINED;
    Layout layout() default Layout.UNDEFINED;
    String linkIcon() default "";
    String tagIcon() default "";
    String separator() default "";
    double boost() default 1.0;
    Facet facet() default Facet.UNDEFINED;
    FacetOrder facetOrder() default  FacetOrder.UNDEFINED;
    Aggregate aggregate() default Aggregate.UNDEFINED;
    int order() default 0;
    int overviewMaxDisplay() default 0;


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
