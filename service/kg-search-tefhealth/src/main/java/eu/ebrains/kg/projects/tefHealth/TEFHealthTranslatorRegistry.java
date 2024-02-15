package eu.ebrains.kg.projects.tefHealth;

import eu.ebrains.kg.common.controller.translation.TranslatorRegistry;
import eu.ebrains.kg.common.controller.translation.models.TranslatorModel;
import eu.ebrains.kg.projects.tefHealth.target.Country;
import eu.ebrains.kg.projects.tefHealth.target.Institution;
import eu.ebrains.kg.projects.tefHealth.translators.CountryTranslator;
import eu.ebrains.kg.projects.tefHealth.translators.InstitutionTranslator;
import eu.ebrains.kg.projects.tefHealth.target.Service;
import eu.ebrains.kg.projects.tefHealth.translators.ServiceTranslator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@Profile("tef-health")
public class TEFHealthTranslatorRegistry implements TranslatorRegistry {

    private final List<eu.ebrains.kg.common.controller.translation.models.TranslatorModel<?,?>> TRANSLATORS = Arrays.asList(
            new TranslatorModel<>(Service.class, new ServiceTranslator(), false, 1000, true),
            new TranslatorModel<>(Institution.class, new InstitutionTranslator(), false, 1000, true),
            new TranslatorModel<>(Country.class, new CountryTranslator(), false, 1000, true)
    );

    @Override
    public List<TranslatorModel<?, ?>> getTranslators() {
        return TRANSLATORS;
    }

    @Override
    public Class<?> getFileClass() {
        return null;
    }

    @Override
    public Optional<String> getIndexPrefix() {
        return Optional.of("tefhealth");
    }

    @Override
    public String getName() {
        return "TEF-Health";
    }
}
