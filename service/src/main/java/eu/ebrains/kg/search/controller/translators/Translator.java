package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.DatabaseScope;

public interface Translator<Source, Target> {

    public Target translate(Source source, DatabaseScope databaseScope, boolean liveMode);

}
