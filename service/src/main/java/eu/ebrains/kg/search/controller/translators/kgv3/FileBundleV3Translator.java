/*
 * Copyright 2018 - 2021 Swiss Federal Institute of Technology Lausanne (EPFL)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This open source software code was developed in part or in whole in the
 * Human Brain Project, funded from the European Union's Horizon 2020
 * Framework Programme for Research and Innovation under
 * Specific Grant Agreements No. 720270, No. 785907, and No. 945539
 * (Human Brain Project SGA1, SGA2 and SGA3).
 *
 */

package eu.ebrains.kg.search.controller.translators.kgv3;

import eu.ebrains.kg.search.controller.translators.Translator;
import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.ResultsOfKGv3;
import eu.ebrains.kg.search.model.source.openMINDSv3.FileBundleV3;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.FileBundle;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetExternalReference;
import eu.ebrains.kg.search.services.DOICitationFormatter;
import eu.ebrains.kg.search.utils.IdUtils;
import eu.ebrains.kg.search.utils.TranslationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class FileBundleV3Translator extends TranslatorV3<FileBundleV3, FileBundle, FileBundleV3Translator.Result> {
    public static class Result extends ResultsOfKGv3<FileBundleV3> {
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

    public FileBundle translate(FileBundleV3 fileBundle, DataStage dataStage, boolean liveMode, DOICitationFormatter doiCitationFormatter) throws TranslationException {
        FileBundle fb = new FileBundle();
        fb.setId(IdUtils.getUUID(fileBundle.getId()));
        fb.setTitle(value(fileBundle.getName()));
        fb.setAllIdentifiers(fileBundle.getIdentifier());
        fb.setIdentifier(IdUtils.getUUID(fileBundle.getIdentifier()));
        if (fileBundle.getDataLocation() != null && StringUtils.isNotBlank(fileBundle.getDataLocation().getUrl())) {
            fb.setDataLocation(new TargetExternalReference(fileBundle.getDataLocation().getUrl(), fileBundle.getDataLocation().getLabel()));
        }
        return fb;
    }
}
