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

package eu.ebrains.kg.projects.ebrains.translators;

import eu.ebrains.kg.common.model.DataStage;
import eu.ebrains.kg.common.model.source.ResultsOfKG;
import eu.ebrains.kg.common.model.source.ServiceLink;
import eu.ebrains.kg.common.model.target.TargetExternalReference;
import eu.ebrains.kg.common.model.target.Value;
import eu.ebrains.kg.common.utils.IdUtils;
import eu.ebrains.kg.common.utils.TranslationException;
import eu.ebrains.kg.common.utils.TranslatorUtils;
import eu.ebrains.kg.projects.ebrains.source.FileBundleV3;
import eu.ebrains.kg.projects.ebrains.target.FileBundle;
import eu.ebrains.kg.projects.ebrains.translators.commons.EBRAINSTranslator;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FileBundleTranslator extends EBRAINSTranslator<FileBundleV3, FileBundle, FileBundleTranslator.Result> {
    public static class Result extends ResultsOfKG<FileBundleV3> {
    }

    @Override
    public Class<FileBundleV3> getSourceType() {
        return FileBundleV3.class;
    }

    @Override
    public Class<FileBundle> getTargetType() {
        return FileBundle.class;
    }

    @Override
    public Class<Result> getResultType() {
        return Result.class;
    }

    @Override
    public List<String> getQueryIds() {
        return Collections.singletonList("7ebfdf3e-e5a0-4436-90d9-423bb41fff03");
    }

    @Override
    public List<String> semanticTypes() {
        return Collections.singletonList("https://openminds.ebrains.eu/core/FileBundle");
    }

    public FileBundle translate(FileBundleV3 fileBundle, DataStage dataStage, boolean liveMode, TranslatorUtils translatorUtils) throws TranslationException {
        FileBundle fb = new FileBundle();

        fb.setCategory(new Value<>("File Bundle"));
        fb.setDisclaimer(new Value<>("Please alert us at [curation-support@ebrains.eu](mailto:curation-support@ebrains.eu) for errors or quality concerns regarding the dataset, so we can forward this information to the Data Custodian responsible."));

        fb.setId(IdUtils.getUUID(fileBundle.getId()));
        fb.setTitle(value(fileBundle.getName()));
        fb.setAllIdentifiers(fileBundle.getIdentifier());
        fb.setIdentifier(IdUtils.getUUID(fileBundle.getIdentifier()).stream().distinct().collect(Collectors.toList()));
        if(!CollectionUtils.isEmpty(fileBundle.getServiceLinks())){
            fb.setViewer(fileBundle.getServiceLinks().stream().sorted(Comparator.comparing(ServiceLink::displayLabel)).map(s -> new TargetExternalReference(s.getUrl(), String.format("Open in %s", s.getService()))).collect(Collectors.toList()));
        }
        return fb;
    }
}
