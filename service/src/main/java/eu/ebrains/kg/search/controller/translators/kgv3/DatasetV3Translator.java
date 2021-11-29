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

import eu.ebrains.kg.search.controller.translators.Helpers;
import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.ResultsOfKGv3;
import eu.ebrains.kg.search.model.source.openMINDSv3.DatasetV3;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Version;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Dataset;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Children;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Value;
import eu.ebrains.kg.search.services.DOICitationFormatter;
import eu.ebrains.kg.search.utils.IdUtils;
import eu.ebrains.kg.search.utils.TranslationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DatasetV3Translator extends TranslatorV3<DatasetV3, Dataset, DatasetV3Translator.Result> {

    private static final String QUERY_ID = "1967ced9-e3f9-4d8f-a2e6-296f3fb6329f";

    public static class Result extends ResultsOfKGv3<DatasetV3> {
    }

    @Override
    public List<String> getQueryIds() {
        return Collections.singletonList(QUERY_ID);
    }

    @Override
    public Class<Result> getResultType() {
        return Result.class;
    }

    @Override
    public Class<DatasetV3> getSourceType() {
        return DatasetV3.class;
    }

    @Override
    public Class<Dataset> getTargetType() {
        return Dataset.class;
    }

    @Override
    public List<String> semanticTypes() {
        return Collections.singletonList("https://openminds.ebrains.eu/core/Dataset");
    }

    public Dataset translate(DatasetV3 dataset, DataStage dataStage, boolean liveMode, DOICitationFormatter doiCitationFormatter) throws TranslationException {
        if (!CollectionUtils.isEmpty(dataset.getVersions()) && dataset.getVersions().size() > 1) {
            Dataset d = new Dataset();
            List<Version> sortedVersions = Helpers.sort(dataset.getVersions());
            List<Children<Dataset.Version>> datasetVersions = sortedVersions.stream().map(v -> {
                Dataset.Version version = new Dataset.Version();
                version.setVersion(new TargetInternalReference(IdUtils.getUUID(v.getId()), v.getVersionIdentifier()));
                version.setInnovation(v.getVersionInnovation() != null ? new Value<>(v.getVersionInnovation()) : null);
                return new Children<>(version);
            }).collect(Collectors.toList());
            d.setDatasets(datasetVersions);
            d.setId(IdUtils.getUUID(dataset.getId()));
            d.setIdentifier(IdUtils.getUUID(dataset.getIdentifier()));
            d.setDescription(dataset.getDescription());
            if (StringUtils.isNotBlank(dataset.getFullName())) {
                d.setTitle(dataset.getFullName());
            }
            if (!CollectionUtils.isEmpty(dataset.getAuthors())) {
                d.setAuthors(dataset.getAuthors().stream()
                        .map(a -> new TargetInternalReference(
                                IdUtils.getUUID(a.getId()),
                                Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                        )).collect(Collectors.toList()));
            }
            if (!CollectionUtils.isEmpty(dataset.getCustodians())) {
                d.setCustodians(dataset.getCustodians().stream()
                        .map(a -> new TargetInternalReference(
                                IdUtils.getUUID(a.getId()),
                                Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                        )).collect(Collectors.toList()));
            }
            String citation = dataset.getHowToCite();
            String doi = dataset.getDoi();
            if (StringUtils.isNotBlank(doi)) {
                if (StringUtils.isNotBlank(citation)) {
                    d.setDoi(doi);
                    String url = URLEncoder.encode(doi, StandardCharsets.UTF_8);
                    d.setCitation(citation + String.format(" [DOI: %s]\n[DOI: %s]: https://doi.org/%s", doi, doi, url));
                }
            }
            return d;
        }
        return null;
    }
}
