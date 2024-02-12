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

package eu.ebrains.kg.common.controller.translators;

import eu.ebrains.kg.common.model.DataStage;
import eu.ebrains.kg.common.model.source.ResultsOfKG;
import eu.ebrains.kg.common.services.ESServiceClient;
import eu.ebrains.kg.common.utils.TranslationException;
import eu.ebrains.kg.common.utils.TranslatorUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class Translator<Source, Target, ListResult extends ResultsOfKG<Source>> extends TranslatorBase {

    public abstract Target translate(Source source, DataStage dataStage, boolean liveMode, TranslatorUtils translatorUtils) throws TranslationException;

    public abstract Class<Source> getSourceType();

    public abstract Class<Target> getTargetType();

    public abstract Class<ListResult> getResultType();

    public abstract List<String> getQueryIds();

    public String getQueryFileName(String semanticType) {
        final String simpleName = getClass().getSimpleName();
        return StringUtils.uncapitalize(simpleName.substring(0, simpleName.indexOf("V3")));
    }

    public Map<String, Object> populateTranslationContext(ESServiceClient esServiceClient, DataStage stage){
        return Collections.emptyMap();
    }

}