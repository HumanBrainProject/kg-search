package eu.ebrains.kg.search.model;

import eu.ebrains.kg.search.controller.mergers.ContributorMerger;
import eu.ebrains.kg.search.controller.mergers.Merger;
import eu.ebrains.kg.search.controller.mergers.ProjectMerger;
import eu.ebrains.kg.search.controller.translators.Translator;
import eu.ebrains.kg.search.controller.translators.kgv2.*;
import eu.ebrains.kg.search.controller.translators.kgv3.*;
import eu.ebrains.kg.search.model.source.ResultsOfKGv2;
import eu.ebrains.kg.search.model.source.ResultsOfKGv3;
import eu.ebrains.kg.search.model.target.elasticsearch.TargetInstance;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.*;

import java.util.Arrays;
import java.util.List;

public class TranslatorModel<v1Input, v2Input, v3Input, Output extends TargetInstance> {

    public static final List<TranslatorModel<?,?,?,?>> MODELS = Arrays.asList(
            new TranslatorModel<>(Project.class, new ProjectV1Translator(), null, new ProjectV3Translator(), new ProjectMerger(), false, false),
            new TranslatorModel<>(Dataset.class, null, null, new DatasetV3Translator(), null, false, false),
            new TranslatorModel<>(DatasetVersion.class, new DatasetV1Translator(), null, new DatasetVersionV3Translator(), null, false, false),
            new TranslatorModel<>(Subject.class, new SubjectV1Translator(), null, new SubjectV3Translator(), null, false, false),
            new TranslatorModel<>(Sample.class, new SampleV1Translator(), null, null, null, false, false),
            new TranslatorModel<>(ModelVersion.class, null, null, new ModelVersionV3Translator(), null, false, false),
            new TranslatorModel<>(SoftwareVersion.class, null, null, new SoftwareVersionV3Translator(), null, false, true),
            new TranslatorModel<>(Model.class, null, null, new ModelV3Translator(), null, false, false),
            new TranslatorModel<>(Software.class, null, null, new SoftwareV3Translator(), null, false, false),
            new TranslatorModel<>(Contributor.class, new PersonV1Translator(), new PersonV2Translator(), new ContributorV3Translator(), new ContributorMerger(), false, false),
            new TranslatorModel<>(ControlledTerm.class, null, null, new ControlledTermV3Translator(), null, false, false),
            new TranslatorModel<>(ContentType.class, null, null, new ContentTypeV3Translator(), null, false, false),
            new TranslatorModel<>(File.class, null, null, new FileV3Translator(), null, true, false),
            new TranslatorModel<>(FileBundle.class, null, null, new FileBundleV3Translator(), null, true, false),
//            ,
//            new TranslatorModel<>(ParcellationEntityVersion.class, null, null, new ParcellationEntityVersionV3Translator(), null, false, false),
            new TranslatorModel<>(ParcellationEntity.class, null, null, new ParcellationEntityV3Translator(), null, false, false),
            new TranslatorModel<>(BehavioralProtocol.class, null, null, new BehavioralProtocolV3Translator(), null, false, false)
    );

    public static TranslatorModel<?,?,?,?> getModelByType(String type){
        return MODELS.stream().filter(m -> m.getV3translator()!=null && m.getV3translator().semanticTypes().contains(type)).findFirst().orElse(null);
    }


    private final Class<Output> targetClass;
    private final Translator<v1Input, Output, ? extends ResultsOfKGv2<v1Input>> v1translator;
    private final Translator<v2Input, Output, ? extends ResultsOfKGv2<v2Input>> v2translator;
    private final TranslatorV3<v3Input, Output, ? extends ResultsOfKGv3<v3Input>> v3translator;
    private final Merger<Output> merger;
    private final boolean autoRelease;
    private final boolean onlyV3ForInProgress;

    private TranslatorModel(Class<Output> targetClass, Translator<v1Input, Output, ? extends ResultsOfKGv2<v1Input>> v1translator, Translator<v2Input, Output, ? extends ResultsOfKGv2<v2Input>> v2translator, TranslatorV3<v3Input, Output, ? extends ResultsOfKGv3<v3Input>> v3Translator, Merger<Output> merger, boolean autoRelease, boolean onlyV3ForInProgress) {
        this.targetClass = targetClass;
        this.v1translator = v1translator;
        this.v2translator = v2translator;
        this.v3translator = v3Translator;
        this.merger = merger;
        this.autoRelease = autoRelease;
        this.onlyV3ForInProgress = onlyV3ForInProgress;
    }

    public boolean isAutoRelease() {
        return autoRelease;
    }

    public boolean isOnlyV3ForInProgress() {
        return onlyV3ForInProgress;
    }

    public Merger<Output> getMerger() {
        return merger;
    }

    public Class<Output> getTargetClass() {
        return targetClass;
    }

    public Translator<v1Input, Output, ? extends ResultsOfKGv2<v1Input>> getV1translator() {
        return v1translator;
    }


    public Translator<v2Input, Output, ? extends ResultsOfKGv2<v2Input>> getV2translator() {
        return v2translator;
    }

    public TranslatorV3<v3Input, Output, ? extends ResultsOfKGv3<v3Input>> getV3translator() {
        return v3translator;
    }

    public int getBulkSize() {
        //TODO handle the "big" source models by restraining the bulk size
        return 1000;
    }
}
