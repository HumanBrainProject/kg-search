package eu.ebrains.kg.search.model.target.elasticsearch;

public @interface FieldInfo {
    String label() default "";
    boolean optional() default false;
    boolean sort() default false;
    boolean visible() default true;
    boolean labelHidden() default false;
    boolean markdown() default false;
    boolean overview() default false;
    String type() default  "";
    Layout layout() default Layout.STANDARD;
    String linkIcon() default "";
    String tagIcon() default "";
    String facet() default "";
    boolean ignoreForSearch() default false;
    float boost() default 1.0f;


    public enum Layout{
        STANDARD, GROUP, HEADER
    }

}
