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
import eu.ebrains.kg.search.model.source.openMINDSv3.FileV3;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.ServiceLink;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.File;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetExternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.services.DOICitationFormatter;
import eu.ebrains.kg.search.utils.IdUtils;
import eu.ebrains.kg.search.utils.TranslationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

public class FileV3Translator extends TranslatorV3<FileV3, File, FileV3Translator.Result> {
    public static class Result extends ResultsOfKGv3<FileV3> {
    }

    @Override
    public Class<FileV3> getSourceType() {
        return FileV3.class;
    }

    @Override
    public Class<File> getTargetType() {
        return File.class;
    }

    @Override
    public Class<Result> getResultType() {
        return Result.class;
    }

    @Override
    public List<String> getQueryIds() {
        return Collections.singletonList("b96f11b3-1bcb-44c1-8d12-d33dbec3b990");
    }

    @Override
    public List<String> semanticTypes() {
        return Collections.singletonList("https://openminds.ebrains.eu/core/File");
    }

    public File translate(FileV3 file, DataStage dataStage, boolean liveMode, DOICitationFormatter doiCitationFormatter) throws TranslationException {
        String fileRepository = file.getFileRepository();

        if(StringUtils.isBlank(fileRepository) || StringUtils.isBlank(file.getIri()) || StringUtils.isBlank(file.getName())) {
            return null;
        }

        File f = new File();
        f.setId(IdUtils.getUUID(file.getId()));
        f.setAllIdentifiers(file.getIdentifier());
        f.setIdentifier(IdUtils.getUUID(file.getIdentifier()));
        f.setFileRepository(IdUtils.getUUID(fileRepository));
        f.setTitle(value(file.isPrivateAccess() ? String.format("ACCESS PROTECTED: %s", file.getName()) : file.getName()));
        if(!CollectionUtils.isEmpty(file.getServiceLinks())){
            f.setViewer(file.getServiceLinks().stream().sorted(Comparator.comparing(ServiceLink::displayLabel)).map(s -> new TargetExternalReference(s.getUrl(), String.format("Open in %s", s.getService()))).collect(Collectors.toList()));
        }
        String iri = file.isPrivateAccess() ? String.format("%s/files/cscs?url=%s", Translator.fileProxy, file.getIri()) : file.getIri();
        if (StringUtils.isNotBlank(iri)) {
            f.setIri(new TargetExternalReference(iri, iri));
        }
        FileV3.Size size = file.getSize();
        if(size != null && StringUtils.isNotBlank(size.getUnit())) {
            f.setSize(value(FileUtils.byteCountToDisplaySize(size.getValue())));
        }
        if(file.getFormat()!=null){
            f.setFormat(ref(file.getFormat()));
            f.setInputTypeForSoftware(ref(file.getFormat().getInputFormatForSoftware()));
        }
        Map<String, File.GroupingType> groupingTypes = new HashMap<>();
        file.getFileBundles().forEach(fileBundle -> {
            String groupingTypeName = fileBundle.getGroupingType();
            TargetInternalReference fb =  new TargetInternalReference(
                    IdUtils.getUUID(fileBundle.getId()), fileBundle.getName());
            if (!groupingTypes.containsKey(groupingTypeName)) {
                File.GroupingType groupingType = new File.GroupingType();
                groupingType.setName(groupingTypeName);
                groupingType.setFileBundles(new ArrayList<>(Collections.singletonList(fb)));
                groupingTypes.put(groupingTypeName, groupingType);
            } else {
                File.GroupingType groupingType = groupingTypes.get(groupingTypeName);
                List<TargetInternalReference> fileBundles = groupingType.getFileBundles();
                fileBundles.add(fb);
                Collections.sort(fileBundles);
            }
        });
        List<File.GroupingType> groupingTypeList = new ArrayList<>(groupingTypes.values());
        if (!groupingTypeList.isEmpty()) {
            groupingTypeList.sort(Comparator.comparing(File.GroupingType::getName));
            f.setGroupingTypes(groupingTypeList);
        }
        return f;
    }
}
