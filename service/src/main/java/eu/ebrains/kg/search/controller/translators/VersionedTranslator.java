package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.DataStage;

public interface VersionedTranslator<Source, ParentSource, Target> {
    public Target translate(Source source, ParentSource parentSource, DataStage dataStage);
}
