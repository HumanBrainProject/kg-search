/*
 *  Copyright 2018 - 2021 Swiss Federal Institute of Technology Lausanne (EPFL)
 *  Copyright 2021 - 2023 EBRAINS AISBL
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0.
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  This open source software code was developed in part or in whole in the
 *  Human Brain Project, funded from the European Union's Horizon 2020
 *  Framework Programme for Research and Innovation under
 *  Specific Grant Agreements No. 720270, No. 785907, and No. 945539
 *  (Human Brain Project SGA1, SGA2 and SGA3).
 */

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
            new TranslatorModel<>(Project.class, new ProjectV3Translator(), false, 1000, true),
            new TranslatorModel<>(Dataset.class, new DatasetV3Translator(), false, 1000, true),
            new TranslatorModel<>(DatasetVersion.class, new DatasetVersionV3Translator(), false, 1, true),
            new TranslatorModel<>(Subject.class, new SubjectV3Translator(), false, 1000, true),
            new TranslatorModel<>(Model.class, new ModelV3Translator(), false, 1000, true),
            new TranslatorModel<>(ModelVersion.class, new ModelVersionV3Translator(), false, 1000, true),
            new TranslatorModel<>(MetaDataModel.class, new MetaDataModelV3Translator(), false, 1000, true),
            new TranslatorModel<>(MetaDataModelVersion.class, new MetaDataModelVersionV3Translator(), false, 1000, true),
            new TranslatorModel<>(Software.class, new SoftwareV3Translator(), false, 1000, true),
            new TranslatorModel<>(SoftwareVersion.class, new SoftwareVersionV3Translator(), false, 1000, true),
            new TranslatorModel<>(WebService.class, new WebServiceV3Translator(), false, 1000, true),
            new TranslatorModel<>(WebServiceVersion.class, new WebServiceVersionV3Translator(), false, 1000, true),
            new TranslatorModel<>(Contributor.class, new ContributorV3Translator(), false, 1000, true),
            new TranslatorModel<>(ControlledTerm.class, new ControlledTermV3Translator(), false, 1000, true),
            new TranslatorModel<>(ContentType.class, new ContentTypeV3Translator(), false, 1000, false),
            new TranslatorModel<>(File.class, new FileV3Translator(), true, 1000, false),
            new TranslatorModel<>(FileBundle.class, new FileBundleV3Translator(), true, 1000, false),
            new TranslatorModel<>(ParcellationEntity.class, new ParcellationEntityV3Translator(), false, 1000, true),
            new TranslatorModel<>(BehavioralProtocol.class, new BehavioralProtocolV3Translator(), false, 1000, true),
            new TranslatorModel<>(BrainAtlas.class, new BrainAtlasV3Translator(), false, 1000, true),
            new TranslatorModel<>(CoordinateSpace.class, new CoordinateSpaceV3Translator(), false, 1000, true),
            new TranslatorModel<>(WorkflowRecipeVersion.class, new WorkflowRecipeVersionV3Translator(), false, 1000, true),
            new TranslatorModel<>(Protocol.class, new ProtocolV3Translator(), false, 1000, true)
    );

    private final Class<Output> targetClass;
    private final TranslatorV3<v3Input, Output, ? extends ResultsOfKGv3<v3Input>> v3translator;
    private final boolean autoRelease;
    private final int bulkSize;

    private final boolean addToSitemap;

    private TranslatorModel(Class<Output> targetClass, TranslatorV3<v3Input, Output, ? extends ResultsOfKGv3<v3Input>> v3Translator, boolean autoRelease, int bulkSize, boolean addToSitemap) {
        this.targetClass = targetClass;
        this.v3translator = v3Translator;
        this.autoRelease = autoRelease;
        this.bulkSize = bulkSize;
        this.addToSitemap = addToSitemap;
    }

    public boolean isAddToSitemap() {
        return addToSitemap;
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
