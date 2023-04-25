package eu.ebrains.kg.common.model;

import eu.ebrains.kg.common.controller.translators.kgv3.*;
import eu.ebrains.kg.common.model.source.ResultsOfKGv3;
import eu.ebrains.kg.common.model.source.openMINDSv3.SourceInstanceV3;
import eu.ebrains.kg.common.model.target.elasticsearch.TargetInstance;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.*;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("java:S1452") // we keep the generics intentionally
public class TranslatorModel<v3Input extends SourceInstanceV3, Output extends TargetInstance> {

    public static final List<TranslatorModel<?, ?>> MODELS = Arrays.asList(
            new TranslatorModel<>(Project.class, new ProjectV3Translator(), false, 1000),
            new TranslatorModel<>(Dataset.class, new DatasetV3Translator(), false, 1000),
            new TranslatorModel<>(DatasetVersion.class, new DatasetVersionV3Translator(), false, 1),
            new TranslatorModel<>(Subject.class, new SubjectV3Translator(), false, 1000),
            new TranslatorModel<>(Model.class, new ModelV3Translator(), false, 1000),
            new TranslatorModel<>(ModelVersion.class, new ModelVersionV3Translator(), false, 1000),
            new TranslatorModel<>(MetaDataModel.class, new MetaDataModelV3Translator(), false, 1000),
            new TranslatorModel<>(MetaDataModelVersion.class, new MetaDataModelVersionV3Translator(), false, 1000),
            new TranslatorModel<>(Software.class, new SoftwareV3Translator(), false, 1000),
            new TranslatorModel<>(SoftwareVersion.class, new SoftwareVersionV3Translator(), false, 1000),
            new TranslatorModel<>(WebService.class, new WebServiceV3Translator(), false, 1000),
            new TranslatorModel<>(WebServiceVersion.class, new WebServiceVersionV3Translator(), false, 1000),
            new TranslatorModel<>(Contributor.class, new ContributorV3Translator(), false, 1000),
            new TranslatorModel<>(ControlledTerm.class, new ControlledTermV3Translator(), false, 1000),
            new TranslatorModel<>(ContentType.class, new ContentTypeV3Translator(), false, 1000),
            new TranslatorModel<>(File.class, new FileV3Translator(), true, 1000),
            new TranslatorModel<>(FileBundle.class, new FileBundleV3Translator(), true, 1000),
            new TranslatorModel<>(ParcellationEntity.class, new ParcellationEntityV3Translator(), false, 1000),
            new TranslatorModel<>(BehavioralProtocol.class, new BehavioralProtocolV3Translator(), false, 1000),
            new TranslatorModel<>(BrainAtlas.class, new BrainAtlasV3Translator(), false, 1000),
            new TranslatorModel<>(CoordinateSpace.class, new CoordinateSpaceV3Translator(), false, 1000),
            new TranslatorModel<>(WorkflowRecipeVersion.class, new WorkflowRecipeVersionV3Translator(), false, 1000),
            new TranslatorModel<>(Protocol.class, new ProtocolV3Translator(), false, 1000)
    );

    private final Class<Output> targetClass;
    private final TranslatorV3<v3Input, Output, ? extends ResultsOfKGv3<v3Input>> v3translator;
    private final boolean autoRelease;
    private final int bulkSize;

    private TranslatorModel(Class<Output> targetClass, TranslatorV3<v3Input, Output, ? extends ResultsOfKGv3<v3Input>> v3Translator, boolean autoRelease, int bulkSize) {
        this.targetClass = targetClass;
        this.v3translator = v3Translator;
        this.autoRelease = autoRelease;
        this.bulkSize = bulkSize;
    }

    public boolean isAutoRelease() {
        return autoRelease;
    }

    public Class<Output> getTargetClass() {
        return targetClass;
    }

    public TranslatorV3<v3Input, Output, ? extends ResultsOfKGv3<v3Input>> getV3translator() {
        return v3translator;
    }

    public int getBulkSize() {
        return bulkSize;
    }

}
