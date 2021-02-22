package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.DataStage;

public interface VersionedTranslator<Source, Target> {
    public Target translate(Source source, DataStage dataStage, boolean liveMode, String versionIdentifier, boolean useMainIdentifier);
}
