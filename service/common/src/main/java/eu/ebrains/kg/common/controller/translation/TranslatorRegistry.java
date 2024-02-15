package eu.ebrains.kg.common.controller.translation;
import eu.ebrains.kg.common.controller.translation.models.TranslatorModel;

import java.util.List;
import java.util.Optional;

public interface TranslatorRegistry {
    List<TranslatorModel<?,?>> getTranslators();

    Class<?> getFileClass();

    Optional<String> getIndexPrefix();

    String getName();

}
