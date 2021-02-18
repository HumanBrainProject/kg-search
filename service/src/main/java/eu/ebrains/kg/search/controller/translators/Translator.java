package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.DataStage;

public interface Translator<Source, Target> {
    public static final String fileProxy = ""; //TODO: Should that be changed ?
//    public static final String fileProxy = "https://kg.ebrains.eu";

    public Target translate(Source source, DataStage dataStage, boolean liveMode);

}
