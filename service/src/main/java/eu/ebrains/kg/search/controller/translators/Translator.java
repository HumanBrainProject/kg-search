package eu.ebrains.kg.search.controller.translators;

public interface Translator<Source, Target> {

    public Target translate(Source source);

}
