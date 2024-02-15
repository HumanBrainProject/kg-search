package eu.ebrains.kg.projects.ebrains;

import eu.ebrains.kg.common.controller.translation.TranslatorRegistry;
import eu.ebrains.kg.common.controller.translation.models.TranslatorModel;
import eu.ebrains.kg.projects.ebrains.target.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import eu.ebrains.kg.projects.ebrains.translators.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@Profile("ebrains")
public class EBRAINSTranslatorRegistry implements TranslatorRegistry {
    private final List<TranslatorModel<?,?>> TRANSLATORS = Arrays.asList(
            new TranslatorModel<>(Project.class, new ProjectTranslator(), false, 1000, true),
            new TranslatorModel<>(Dataset.class, new DatasetTranslator(), false, 1000, true),
            new TranslatorModel<>(DatasetVersion.class, new DatasetVersionTranslator(), false, 1, true),
            new TranslatorModel<>(Subject.class, new SubjectTranslator(), false, 1000, true),
            new TranslatorModel<>(Model.class, new ModelTranslator(), false, 1000, true),
            new TranslatorModel<>(ModelVersion.class, new ModelVersionTranslator(), false, 1000, true),
            new TranslatorModel<>(MetaDataModel.class, new MetaDataModelTranslator(), false, 1000, true),
            new TranslatorModel<>(MetaDataModelVersion.class, new MetaDataModelVersionTranslator(), false, 1000, true),
            new TranslatorModel<>(Software.class, new SoftwareTranslator(), false, 1000, true),
            new TranslatorModel<>(SoftwareVersion.class, new SoftwareVersionTranslator(), false, 1000, true),
            new TranslatorModel<>(WebService.class, new WebServiceTranslator(), false, 1000, true),
            new TranslatorModel<>(WebServiceVersion.class, new WebServiceVersionTranslator(), false, 1000, true),
            new TranslatorModel<>(Contributor.class, new ContributorTranslator(), false, 1000, true),
            new TranslatorModel<>(ControlledTerm.class, new ControlledTermTranslator(), false, 1000, true),
            new TranslatorModel<>(ContentType.class, new ContentTypeTranslator(), false, 1000, false),
            new TranslatorModel<>(File.class, new FileTranslator(), true, 1000, false),
            new TranslatorModel<>(FileBundle.class, new FileBundleTranslator(), true, 1000, false),
            new TranslatorModel<>(ParcellationEntity.class, new ParcellationEntityTranslator(), false, 1000, true),
            new TranslatorModel<>(BehavioralProtocol.class, new BehavioralProtocolTranslator(), false, 1000, true),
            new TranslatorModel<>(BrainAtlas.class, new BrainAtlasTranslator(), false, 1000, true),
            new TranslatorModel<>(CoordinateSpace.class, new CoordinateSpaceTranslator(), false, 1000, true),
            new TranslatorModel<>(WorkflowRecipeVersion.class, new WorkflowRecipeVersionTranslator(), false, 1000, true),
            new TranslatorModel<>(Protocol.class, new ProtocolTranslator(), false, 1000, true)
    );
    @Override
    public List<TranslatorModel<?, ?>> getTranslators() {
        return TRANSLATORS;
    }

    @Override
    public Class<?> getFileClass() {
        return File.class;
    }

    @Override
    public Optional<String> getIndexPrefix() {
        return Optional.empty();
    }

    @Override
    public String getName() {
        return "EBRAINS RI";
    }
}
