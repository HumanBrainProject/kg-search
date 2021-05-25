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

package eu.ebrains.kg.search.controller.translators;


import eu.ebrains.kg.search.constants.Queries;
import eu.ebrains.kg.search.controller.Constants;
import eu.ebrains.kg.search.controller.kg.KGv2;
import eu.ebrains.kg.search.controller.kg.KGv3;
import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.*;
import eu.ebrains.kg.search.model.source.openMINDSv1.*;
import eu.ebrains.kg.search.model.source.openMINDSv2.ModelV2;
import eu.ebrains.kg.search.model.source.openMINDSv2.PersonV2;
import eu.ebrains.kg.search.model.source.openMINDSv2.SoftwareV2;
import eu.ebrains.kg.search.model.source.openMINDSv3.*;
import eu.ebrains.kg.search.model.target.elasticsearch.TargetInstance;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.*;
import eu.ebrains.kg.search.utils.IdUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class TranslationController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final KGv2 kgV2;
    private final KGv3 kgV3;

    public TranslationController(KGv2 kgV2, KGv3 kgV3) {
        this.kgV2 = kgV2;
        this.kgV3 = kgV3;
    }

    private static class ResultsOfKGV3PersonV3 extends ResultsOfKGv3<PersonV3> {}

    private static class PersonV2Results extends ResultsOfKGv2<PersonV2> {}

    private TargetInstance createContributorForIndexing(DataStage dataStage, boolean liveMode, String legacyAuthorization, IdSources ids) {
        String queryForV1 = "query/minds/core/person/v1.0.0/search";
        String queryForV2 = "query/uniminds/core/person/v1.0.0/search";
        PersonSources source = new PersonSources();
        String idV1 = ids.getIdV1();
        String idV2 = ids.getIdV2();
        String idV3 = ids.getIdV3();
        if (StringUtils.isNotBlank(idV1)) {
            logger.info(String.format("Starting to query contributor %s for v1", idV1));
            try {
                PersonV1 personV1 = kgV2.executeQuery(PersonV1.class, dataStage, queryForV1, idV1, legacyAuthorization);
                if (personV1 != null) {
                    source.setPersonV1(personV1);
                    logger.debug(String.format("Successfully queried contributor %s for v1", idV1));
                } else {
                    logger.debug(String.format("Failed to query contributor %s for v1", idV1));
                }
            } catch (WebClientResponseException e) {
                if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                    logger.debug(String.format("The contributor %s for v1 is skipped because it is missing mandatory fields", idV1));
                }
            }
        }
        if (StringUtils.isNotBlank(idV2)) {
            logger.info(String.format("Starting to query contributor %s for v2", idV2));
            try {
                PersonV2 personV2 = kgV2.executeQuery(PersonV2.class, dataStage, queryForV2, idV2, legacyAuthorization);
                if (personV2 != null) {
                    source.setPersonV2(personV2);
                    logger.debug(String.format("Successfully queried contributor %s for v2", idV2));
                } else {
                    logger.debug(String.format("Failed to query contributor %s for v2", idV2));
                }
            } catch (WebClientResponseException e) {
                if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                    logger.debug(String.format("The contributor %s for v2 is skipped because it is missing mandatory fields", idV2));
                }
            }

        }
        if (StringUtils.isNotBlank(idV3)) {
            logger.info(String.format("Starting to query contributor %s for v3", idV3));
            ResultsOfKGv3<PersonV3> queryResult = kgV3.executeQueryForIndexing(ResultsOfKGV3PersonV3.class, dataStage, Queries.CONTRIBUTOR_QUERY_ID, idV3);
            if(queryResult != null) {
                List<PersonV3> persons = queryResult.getData();
                if (!CollectionUtils.isEmpty(persons)) {
                    logger.debug(String.format("Successfully queried contributor %s for v3", idV3));
                    PersonV3 person = persons.get(0);
                    source.setPersonV3(person);
                } else {
                    ResultsOfKGv3.Error error = queryResult.getError();
                    String e = error == null ? "" : String.format("([%d] %s)", error.getCode(), error.getMessage());
                    logger.debug(String.format("Failed to query contributor %s for v3%s", idV3, e));
                }
            }
        }

        Contributor ContributorOfKGV2 = null;
        Contributor ContributorOfKGV3 = null;
        if (source.getPersonV1() != null || source.getPersonV2() != null) {
            ContributorOfKGV2Translator translator = new ContributorOfKGV2Translator();
            ContributorOfKGV2 = translator.translate(source, dataStage, liveMode);
        }
        if (source.getPersonV3() != null) {
            ContributorOfKgV3Translator translator = new ContributorOfKgV3Translator();
            ContributorOfKGV3 = translator.translate(source.getPersonV3(), dataStage, liveMode);
        }
        return ContributorHelpers.merge(ContributorOfKGV2, ContributorOfKGV3);
    }

    public Contributor createContributorFromKGv2(DataStage dataStage, boolean liveMode, String query, String id, String authorization) {
        PersonV2 personV2 = kgV2.executeQuery(PersonV2.class, dataStage, query, id, authorization);
        ContributorOfKGV2Translator translator = new ContributorOfKGV2Translator();
        PersonSources personSource = new PersonSources();
        personSource.setPersonV2(personV2);
        return translator.translate(personSource, dataStage, liveMode);
    }

    public Contributor createContributorFromKGv2(DataStage dataStage, boolean liveMode, List<String> queries, String id, String authorization) {
        String personV1Query = queries.get(0);
        String personV2Query = queries.get(1);
        PersonSources personSource = new PersonSources();
        PersonV1 personV1 = kgV2.executeQuery(PersonV1.class, dataStage, personV1Query, id, authorization);
        personSource.setPersonV1(personV1);
        PersonV2Results personV2Result = kgV2.executeQueryByIdentifier(PersonV2Results.class, dataStage, personV2Query, personV1.getIdentifier(), authorization);
        if (personV2Result != null && !CollectionUtils.isEmpty(personV2Result.getResults())) {
            PersonV2 personV2 = personV2Result.getResults().get(0);
            personSource.setPersonV2(personV2);
        }
        ContributorOfKGV2Translator translator = new ContributorOfKGV2Translator();
        return translator.translate(personSource, dataStage, liveMode);
    }

    public Contributor createContributorFromKGv3(DataStage dataStage, boolean liveMode, String id) {
        ResultsOfKGV3PersonV3 result = kgV3.executeQuery(ResultsOfKGV3PersonV3.class, dataStage, Queries.CONTRIBUTOR_QUERY_ID, id);
        if(result != null) {
            List<PersonV3> persons = result.getData();
            if(!CollectionUtils.isEmpty(persons)) {
                PersonV3 person = persons.get(0);
                ContributorOfKgV3Translator translator = new ContributorOfKgV3Translator();
                return translator.translate(person, dataStage, liveMode);
            }
        }
        throw new HttpClientErrorException(HttpStatus.NOT_FOUND, String.format("Person %s does not exist!", id));
    }

    public TargetInstance createSoftwareVersionForIndexing(DataStage dataStage, boolean liveMode, String legacyAuthorization, IdSources ids) {
        if (StringUtils.isNotBlank(ids.getIdV3())) {
            String id = ids.getIdV3();
            logger.info(String.format("Starting to query software %s for v3", id));
            ResultsOfKGv3<SoftwareVersionV3> queryResult = kgV3.executeQueryForIndexing(ResultsOfKGV3SoftwareVersionV3.class, dataStage, Queries.SOFTWARE_VERSION_QUERY_ID, id);
            if(queryResult != null) {
                List<SoftwareVersionV3> softwares = queryResult.getData();
                if (!CollectionUtils.isEmpty(softwares)) {
                    logger.debug(String.format("Successfully queried software %s for v3", id));
                    SoftwareVersionV3 software = softwares.get(0);
                    SoftwareVersionOfKGV3Translator translator = new SoftwareVersionOfKGV3Translator();
                    return translator.translate(software, dataStage, liveMode);
                } else {
                    ResultsOfKGv3.Error error = queryResult.getError();
                    String e = error == null ? "" : String.format("([%d] %s)", error.getCode(), error.getMessage());
                    logger.debug(String.format("Failed to query software %s for v3%s", id, e));
                }
            }
        } else if (StringUtils.isNotBlank(ids.getIdV2())) {
            String query = "query/softwarecatalog/software/softwareproject/v1.0.0/search";
            String id = ids.getIdV2();
            logger.info(String.format("Starting to query software %s for v2", id));
            try {
                SoftwareV2 software = kgV2.executeQuery(SoftwareV2.class, dataStage, query, id, legacyAuthorization);
                if (software != null) {
                    logger.debug(String.format("Successfully queried software %s for v2", id));
                    SoftwareVersionOfKGV2Translator translator = new SoftwareVersionOfKGV2Translator();
                    return translator.translate(software, dataStage, liveMode);
                } else {
                    logger.debug(String.format("Failed to query software %s for v2", id));
                }
            } catch (WebClientResponseException e) {
                if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                    logger.debug(String.format("The software %s for v2 is skipped because it is missing mandatory fields", id));
                } else {
                    throw e;
                }
            }
        }
        return null;
    }

    public SoftwareVersion createSoftwareVersionFromKGv2(DataStage dataStage, boolean liveMode, String query, String id, String authorization) {
        SoftwareV2 software = kgV2.executeQuery(SoftwareV2.class, dataStage, query, id, authorization);
        SoftwareVersionOfKGV2Translator translator = new SoftwareVersionOfKGV2Translator();
        return translator.translate(software, dataStage, liveMode);
    }

    private static class ResultsOfKGV3DatasetV3 extends ResultsOfKGv3<DatasetV3> {
    }

    private static class ResultsOfKGV3DatasetVersionV3 extends ResultsOfKGv3<DatasetVersionV3> {
    }

    public TargetInstance createDatasetVersionForIndexing(DataStage dataStage, boolean liveMode, String legacyAuthorization, IdSources ids) {
        if (StringUtils.isNotBlank(ids.getIdV3())) {
            String id = ids.getIdV3();
            logger.info(String.format("Starting to query datasetVersion %s for v3", id));
            ResultsOfKGv3<DatasetVersionV3> queryResult = kgV3.executeQueryForIndexing(ResultsOfKGV3DatasetVersionV3.class, dataStage, Queries.DATASET_VERSION_QUERY_ID, id);
            if(queryResult != null) {
                List<DatasetVersionV3> datasetVersions = queryResult.getData();
                if (!CollectionUtils.isEmpty(datasetVersions)) {
                    logger.debug(String.format("Successfully queried datasetVersion %s for v3", id));
                    DatasetVersionV3 datasetVersion = datasetVersions.get(0);
                    DatasetVersionOfKGV3Translator translator = new DatasetVersionOfKGV3Translator();
                    return translator.translate(datasetVersion, dataStage, liveMode);
                } else {
                    ResultsOfKGv3.Error error = queryResult.getError();
                    String e = error == null ? "" : String.format("([%d] %s)", error.getCode(), error.getMessage());
                    logger.debug(String.format("Failed to query datasetVersion %s for v3%s", id, e));
                }
            }

        } else if (StringUtils.isNotBlank(ids.getIdV1())) {
            String id = ids.getIdV1();
            logger.info(String.format("Starting to query dataset %s for v1", id));
            try {
                String query = "query/minds/core/dataset/v1.0.0/search";
                DatasetV1 dataset = kgV2.executeQuery(DatasetV1.class, dataStage, query, id, legacyAuthorization);
                if (dataset != null) {
                    logger.debug(String.format("Successfully queried dataset %s for v1", id));
                    DatasetVersionOfKGV2Translator translator = new DatasetVersionOfKGV2Translator();
                    return translator.translate(dataset, dataStage, liveMode);
                } else {
                    logger.debug(String.format("Failed to query dataset %s for v1", id));
                }
            } catch (WebClientResponseException e) {
                if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                    logger.debug(String.format("The dataset %s for v1 is skipped because it is missing mandatory fields", id));
                } else {
                    throw e;
                }
            }

        }
        return null;
    }

    public DatasetVersion createDatasetVersionFromKGv2(DataStage dataStage, boolean liveMode, String query, String id, String authorization) {
        DatasetV1 datasetV1 = kgV2.executeQuery(DatasetV1.class, dataStage, query, id, authorization);
        DatasetVersionOfKGV2Translator translator = new DatasetVersionOfKGV2Translator();
        return translator.translate(datasetV1, dataStage, liveMode);
    }

    public DatasetVersion createDatasetVersionFromKGv3(DataStage dataStage, boolean liveMode, String id) {
        ResultsOfKGV3DatasetVersionV3 result = kgV3.executeQuery(ResultsOfKGV3DatasetVersionV3.class, dataStage, Queries.DATASET_VERSION_QUERY_ID, id);
        if(result != null) {
            List<DatasetVersionV3> datasetVersions = result.getData();
            if (!CollectionUtils.isEmpty(datasetVersions)) {
                DatasetVersionV3 datasetVersion = datasetVersions.get(0);
                DatasetVersionOfKGV3Translator translator = new DatasetVersionOfKGV3Translator();
                return translator.translate(datasetVersion, dataStage, liveMode);
            }
        }
        throw new HttpClientErrorException(HttpStatus.NOT_FOUND, String.format("DatasetVersion %s does not exist!", id));
    }

    private static class ResultsOfKGV3ModelVersionV3 extends ResultsOfKGv3<ModelVersionV3> {}

    public ModelVersion createModelVersionFromKGv3(DataStage dataStage, boolean liveMode, String id) {
        ResultsOfKGV3ModelVersionV3 result = kgV3.executeQuery(ResultsOfKGV3ModelVersionV3.class, dataStage, Queries.MODEL_VERSION_QUERY_ID, id);
        if(result != null) {
            List<ModelVersionV3> modelVersions = result.getData();
            if (!CollectionUtils.isEmpty(modelVersions)) {
                ModelVersionV3 modelVersion = modelVersions.get(0);
                ModelVersionOfKGV3Translator translator = new ModelVersionOfKGV3Translator();
                return translator.translate(modelVersion, dataStage, liveMode);
            }
        }
        throw new HttpClientErrorException(HttpStatus.NOT_FOUND, String.format("ModelVersion %s does not exist!", id));
    }

    private static class ResultsOfKGV3SoftwareVersionV3 extends ResultsOfKGv3<SoftwareVersionV3> {}

    public SoftwareVersion createSoftwareVersionFromKGv3(DataStage dataStage, boolean liveMode, String id) {
        ResultsOfKGV3SoftwareVersionV3 result = kgV3.executeQuery(ResultsOfKGV3SoftwareVersionV3.class, dataStage, Queries.SOFTWARE_VERSION_QUERY_ID, id);
        if(result != null) {
            List<SoftwareVersionV3> softwareVersions = result.getData();
            if (!CollectionUtils.isEmpty(softwareVersions)) {
                SoftwareVersionV3 softwareVersion = softwareVersions.get(0);
                SoftwareVersionOfKGV3Translator translator = new SoftwareVersionOfKGV3Translator();
                return translator.translate(softwareVersion, dataStage, liveMode);
            }
        }

        throw new HttpClientErrorException(HttpStatus.NOT_FOUND, String.format("SoftwareVersion %s does not exist!", id));
    }

    public Project createProjectFromKGv3(DataStage dataStage, boolean liveMode, String id) {
        ResultsOfKGV3ProjectV3 result = kgV3.executeQuery(ResultsOfKGV3ProjectV3.class, dataStage, Queries.PROJECT_QUERY_ID, id);
        if(result != null) {
            List<ProjectV3> projects = result.getData();
            if (!CollectionUtils.isEmpty(projects)) {
                ProjectV3 project = projects.get(0);
                ProjectOfKGV3Translator translator = new ProjectOfKGV3Translator();
                return translator.translate(project, dataStage, liveMode);
            }
        }
        throw new HttpClientErrorException(HttpStatus.NOT_FOUND, String.format("SoftwareVersion %s does not exist!", id));
    }

    public Subject createSubjectFromKGv3(DataStage dataStage, boolean liveMode, String id) {
        ResultsOfKGV3SubjectV3 result = kgV3.executeQuery(ResultsOfKGV3SubjectV3.class, dataStage, Queries.SUBJECT_QUERY_ID, id);
        if(result != null) {
            List<SubjectV3> subjects = result.getData();
            if (!CollectionUtils.isEmpty(subjects)) {
                SubjectV3 subject = subjects.get(0);
                SubjectOfKGV3Translator translator = new SubjectOfKGV3Translator();
                return translator.translate(subject, dataStage, liveMode);
            }
        }
        throw new HttpClientErrorException(HttpStatus.NOT_FOUND, String.format("SoftwareVersion %s does not exist!", id));
    }

    public Sample createSampleFromKGv3(DataStage dataStage, boolean liveMode, String id) {
        ResultsOfKGV3SampleV3 result = kgV3.executeQuery(ResultsOfKGV3SampleV3.class, dataStage, Queries.SAMPLE_QUERY_ID, id);
        if(result != null) {
            List<SampleV3> samples = result.getData();
            if (!CollectionUtils.isEmpty(samples)) {
                SampleV3 sample = samples.get(0);
                SampleOfKGV3Translator translator = new SampleOfKGV3Translator();
                return translator.translate(sample, dataStage, liveMode);
            }
        }
        throw new HttpClientErrorException(HttpStatus.NOT_FOUND, String.format("SoftwareVersion %s does not exist!", id));
    }

    public TargetInstancesResult createDatasetsForIndexing(DataStage dataStage, boolean liveMode, int from, int size) {
        logger.info("Starting to query datasets for v3");
        ResultsOfKGv3<DatasetV3> datasets = kgV3.executeQueryForIndexing(ResultsOfKGV3DatasetV3.class, dataStage, Queries.DATASET_QUERY_ID, from, size);
        logger.info(String.format("Queried %d datasets for v3", CollectionUtils.isEmpty(datasets.getData()) ? 0 : datasets.getData().size()));
        DatasetOfKGV3Translator translator = new DatasetOfKGV3Translator();
        List<TargetInstance> instances = datasets.getData().stream().map(s -> (TargetInstance) translator.translate(s, dataStage, liveMode)).filter(Objects::nonNull).collect(Collectors.toList());
        TargetInstancesResult result = new TargetInstancesResult();
        result.setTargetInstances(instances);
        result.setFrom(datasets.getFrom());
        result.setSize(datasets.getSize());
        result.setTotal(datasets.getTotal());
        return result;
    }

    public TargetInstancesResult createModelsForIndexing(DataStage dataStage, boolean liveMode, int from, int size) {
        logger.info("Starting to query models for v3");
        ResultsOfKGv3<ModelV3> models = kgV3.executeQueryForIndexing(ResultsOfKGV3ModelV3.class, dataStage, Queries.MODEL_QUERY_ID, from, size);
        logger.info(String.format("Queried %d models for v3", CollectionUtils.isEmpty(models.getData()) ? 0 : models.getData().size()));
        ModelOfKGV3Translator translator = new ModelOfKGV3Translator();
        List<TargetInstance> instances = models.getData().stream().map(s -> (TargetInstance) translator.translate(s, dataStage, liveMode)).filter(Objects::nonNull).collect(Collectors.toList());
        TargetInstancesResult result = new TargetInstancesResult();
        result.setTargetInstances(instances);
        result.setFrom(models.getFrom());
        result.setSize(models.getSize());
        result.setTotal(models.getTotal());
        return result;
    }


    public TargetInstancesResult createSoftwaresForIndexing(DataStage dataStage, boolean liveMode, int from, int size) {
        logger.info("Starting to query softwares for v3");
        ResultsOfKGv3<SoftwareV3> softwares = kgV3.executeQueryForIndexing(ResultsOfKGV3SoftwareV3.class, dataStage, Queries.SOFTWARE_QUERY_ID, from, size);
        logger.info(String.format("Queried %d softwares for v3", CollectionUtils.isEmpty(softwares.getData()) ? 0 : softwares.getData().size()));
        SoftwareOfKGV3Translator translator = new SoftwareOfKGV3Translator();
        List<TargetInstance> instances = softwares.getData().stream().map(s -> (TargetInstance) translator.translate(s, dataStage, liveMode)).filter(Objects::nonNull).collect(Collectors.toList());
        TargetInstancesResult result = new TargetInstancesResult();
        result.setTargetInstances(instances);
        result.setFrom(softwares.getFrom());
        result.setSize(softwares.getSize());
        result.setTotal(softwares.getTotal());
        return result;
    }

    public Dataset createDatasetFromKGv3(DataStage dataStage, boolean liveMode, String id) {
        ResultsOfKGV3DatasetV3 result = kgV3.executeQuery(ResultsOfKGV3DatasetV3.class, dataStage, Queries.DATASET_QUERY_ID, id);
        if(result != null) {
            List<DatasetV3> datasets = result.getData();
            if (!CollectionUtils.isEmpty(datasets)) {
                DatasetV3 dataset = datasets.get(0);
                DatasetOfKGV3Translator translator = new DatasetOfKGV3Translator();
                return translator.translate(dataset, dataStage, liveMode);
            }
        }
        throw new HttpClientErrorException(HttpStatus.NOT_FOUND, String.format("Dataset %s does not exist!", id));
    }

    private static class ResultsOfKGV3ModelV3 extends ResultsOfKGv3<ModelV3> {}

    public Model createModelFromKGv3(DataStage dataStage, boolean liveMode, String id) {
        ResultsOfKGV3ModelV3 result = kgV3.executeQuery(ResultsOfKGV3ModelV3.class, dataStage, Queries.MODEL_QUERY_ID, id);
        if(result != null) {
            List<ModelV3> models = result.getData();
            if (!CollectionUtils.isEmpty(models)) {
                ModelV3 model = models.get(0);
                ModelOfKGV3Translator translator = new ModelOfKGV3Translator();
                return translator.translate(model, dataStage, liveMode);
            }
        }
        throw new HttpClientErrorException(HttpStatus.NOT_FOUND, String.format("Model %s does not exist!", id));
    }

    private static class ResultsOfKGV3SoftwareV3 extends ResultsOfKGv3<SoftwareV3> {}

    public Software createSoftwareFromKGv3(DataStage dataStage, boolean liveMode, String id) {
        ResultsOfKGV3SoftwareV3 result = kgV3.executeQuery(ResultsOfKGV3SoftwareV3.class, dataStage, Queries.SOFTWARE_QUERY_ID, id);
        if(result != null) {
            List<SoftwareV3> softwares = result.getData();
            if (!CollectionUtils.isEmpty(softwares)) {
                SoftwareV3 software = softwares.get(0);
                SoftwareOfKGV3Translator translator = new SoftwareOfKGV3Translator();
                return translator.translate(software, dataStage, liveMode);
            }
        }
        throw new HttpClientErrorException(HttpStatus.NOT_FOUND, String.format("Software %s does not exist!", id));
    }

    public TargetInstance createModelVersionForIndexing(DataStage dataStage, boolean liveMode, String legacyAuthorization, IdSources ids) {
        if (StringUtils.isNotBlank(ids.getIdV3())) {
            String id = ids.getIdV3();
            logger.info(String.format("Starting to query modelVersion %s for v3", id));
            ResultsOfKGv3<ModelVersionV3> queryResult = kgV3.executeQueryForIndexing(ResultsOfKGV3ModelVersionV3.class, dataStage, Queries.MODEL_VERSION_QUERY_ID, id);
            if(queryResult != null) {
                List<ModelVersionV3> modelVersions = queryResult.getData();
                if (!CollectionUtils.isEmpty(modelVersions)) {
                    logger.debug(String.format("Successfully queried modelVersion %s for v3", id));
                    ModelVersionV3 modelVersion = modelVersions.get(0);
                    ModelVersionOfKGV3Translator translator = new ModelVersionOfKGV3Translator();
                    return translator.translate(modelVersion, dataStage, liveMode);
                } else {
                    ResultsOfKGv3.Error error = queryResult.getError();
                    String e = error == null ? "" : String.format("([%d] %s)", error.getCode(), error.getMessage());
                    logger.debug(String.format("Failed to query modelVersion %s for v3%s", id, e));
                }
            }
        } else if (StringUtils.isNotBlank(ids.getIdV2())) {
            String query = "query/uniminds/core/modelinstance/v1.0.0/search";
            String id = ids.getIdV2();
            logger.info(String.format("Starting to query model %s for v2", id));
            try {
                ModelV2 model = kgV2.executeQuery(ModelV2.class, dataStage, query, id, legacyAuthorization);
                if (model != null) {
                    logger.debug(String.format("Successfully queried model %s for v2", id));
                    ModelVersionOfKGV2Translator translator = new ModelVersionOfKGV2Translator();
                    return translator.translate(model, dataStage, liveMode);
                } else {
                    logger.debug(String.format("Failed to query model %s for v2", id));
                }
            } catch (WebClientResponseException e) {
                if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                    logger.debug(String.format("The model %s for v2 is skipped because it is missing mandatory fields", id));
                } else {
                    throw e;
                }
            }
        }
        return null;
    }


    public ModelVersion createModelVersionFromKGv2(DataStage dataStage, boolean liveMode, String query, String id, String authorization) {
        ModelV2 model = kgV2.executeQuery(ModelV2.class, dataStage, query, id, authorization);
        ModelVersionOfKGV2Translator translator = new ModelVersionOfKGV2Translator();
        return translator.translate(model, dataStage, liveMode);
    }

    private static class ResultsOfKGV3ProjectV3 extends ResultsOfKGv3<ProjectV3> {}

    public TargetInstance createProjectForIndexing(DataStage dataStage, boolean liveMode, String legacyAuthorization, IdSources ids) {
        if (StringUtils.isNotBlank(ids.getIdV3())) {
            String id = ids.getIdV3();
            logger.info(String.format("Starting to query project %s for v3", id));
            ResultsOfKGv3<ProjectV3> queryResult = kgV3.executeQueryForIndexing(ResultsOfKGV3ProjectV3.class, dataStage, Queries.PROJECT_QUERY_ID, id);
            if(queryResult != null) {
                List<ProjectV3> projects  = queryResult.getData();
                if (!CollectionUtils.isEmpty(projects)) {
                    logger.debug(String.format("Successfully queried project %s for v3", id));
                    ProjectV3 project = projects.get(0);
                    ProjectOfKGV3Translator translator = new ProjectOfKGV3Translator();
                    return translator.translate(project, dataStage, liveMode);
                } else {
                    ResultsOfKGv3.Error error = queryResult.getError();
                    String e = error == null?"":String.format("([%d] %s)", error.getCode(), error.getMessage());
                    logger.debug(String.format("Failed to query project %s for v3%s", id, e));
                }
            }
        } else if (StringUtils.isNotBlank(ids.getIdV1())) {
            String id = ids.getIdV1();
            logger.info(String.format("Starting to query project %s for v1", id));
            try {
                String query = "query/minds/core/placomponent/v1.0.0/search";
                ProjectV1 project = kgV2.executeQuery(ProjectV1.class, dataStage, query, id, legacyAuthorization);
                if (project != null) {
                    logger.debug(String.format("Successfully queried project %s for v1", id));
                    ProjectOfKGV2Translator translator = new ProjectOfKGV2Translator();
                    return translator.translate(project, dataStage, liveMode);
                } else {
                    logger.debug(String.format("Failed to query project %s for v1", id));
                }
            } catch (WebClientResponseException e) {
                if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                    logger.debug(String.format("The project %s for v1 is skipped because it is missing mandatory fields", id));
                } else {
                    throw e;
                }
            }

        }
        return null;
    }

    public Project createProjectFromKGv2(DataStage dataStage, boolean liveMode, String query, String id, String authorization) {
        ProjectV1 project = kgV2.executeQuery(ProjectV1.class, dataStage, query, id, authorization);
        ProjectOfKGV2Translator translator = new ProjectOfKGV2Translator();
        return translator.translate(project, dataStage, liveMode);
    }

    private static class ResultsOfKGV3SampleV3 extends ResultsOfKGv3<SampleV3> {}

    public TargetInstance createSampleForIndexing(DataStage dataStage, boolean liveMode, String legacyAuthorization, IdSources ids) {
        if (StringUtils.isNotBlank(ids.getIdV3())) {
            String id = ids.getIdV3();
            logger.info(String.format("Starting to query sample %s for v3", id));
            ResultsOfKGv3<SampleV3> queryResult = kgV3.executeQueryForIndexing(ResultsOfKGV3SampleV3.class, dataStage, Queries.SAMPLE_QUERY_ID, id);
            if(queryResult != null) {
                List<SampleV3> samples = queryResult.getData();
                if (!CollectionUtils.isEmpty(samples)) {
                    logger.debug(String.format("Successfully queried sample %s for v3", id));
                    SampleV3 sample = samples.get(0);
                    SampleOfKGV3Translator translator = new SampleOfKGV3Translator();
                    return translator.translate(sample, dataStage, liveMode);
                } else {
                    ResultsOfKGv3.Error error = queryResult.getError();
                    String e = error == null?"":String.format("([%d] %s)", error.getCode(), error.getMessage());
                    logger.debug(String.format("Failed to query sample %s for v3%s", id, e));
                }
            }
        } else if (StringUtils.isNotBlank(ids.getIdV1())) {
            String query = "query/minds/experiment/sample/v1.0.0/search";
            String id = ids.getIdV1();
            logger.info(String.format("Starting to query sample %s for v1", id));
            try {
                SampleV1 sample = kgV2.executeQuery(SampleV1.class, dataStage, query, id, legacyAuthorization);
                if (sample != null) {
                    logger.info(String.format("Successfully query sample %s for v1", id));
                    SampleOfKGV2Translator translator = new SampleOfKGV2Translator();
                    return translator.translate(sample, dataStage, liveMode);
                } else {
                    logger.debug(String.format("Failed to query sample %s for v1", id));
                }
            } catch (WebClientResponseException e) {
                if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                    logger.debug(String.format("The sample %s for v1 is skipped because it is missing mandatory fields", id));
                } else {
                    throw e;
                }
            }
        }
        return null;
    }

    public Sample createSampleFromKGv2(DataStage dataStage, boolean liveMode, String query, String id, String authorization) {
        SampleV1 sample = kgV2.executeQuery(SampleV1.class, dataStage, query, id, authorization);
        SampleOfKGV2Translator translator = new SampleOfKGV2Translator();
        return translator.translate(sample, dataStage, liveMode);
    }

    private static class ResultsOfKGV3SubjectV3 extends ResultsOfKGv3<SubjectV3> {}

    public TargetInstance createSubjectForIndexing(DataStage dataStage, boolean liveMode, String legacyAuthorization, IdSources ids) {
        if (StringUtils.isNotBlank(ids.getIdV3())) {
            String id = ids.getIdV3();
            logger.info(String.format("Starting to query subject %s for v3", id));
            ResultsOfKGv3<SubjectV3> queryResult = kgV3.executeQueryForIndexing(ResultsOfKGV3SubjectV3.class, dataStage, Queries.SAMPLE_QUERY_ID, id);
            if(queryResult != null) {
                List<SubjectV3> subjects = queryResult.getData();
                if (!CollectionUtils.isEmpty(subjects)) {
                    logger.debug(String.format("Successfully queried subject %s for v3", id));
                    SubjectV3 subject = subjects.get(0);
                    SubjectOfKGV3Translator translator = new SubjectOfKGV3Translator();
                    return translator.translate(subject, dataStage, liveMode);
                } else {
                    ResultsOfKGv3.Error error = queryResult.getError();
                    String e = error == null?"":String.format("([%d] %s)", error.getCode(), error.getMessage());
                    logger.debug(String.format("Failed to query subject %s for v3%s", id, e));
                }
            }
        } else if (StringUtils.isNotBlank(ids.getIdV1())) {
            String query = "query/minds/experiment/subject/v1.0.0/search";
            String id = ids.getIdV1();
            logger.info(String.format("Starting to query subject %s for v1", id));
            try {
                SubjectV1 subject = kgV2.executeQuery(SubjectV1.class, dataStage, query, id, legacyAuthorization);
                if (subject != null) {
                    logger.info(String.format("Successfully query subject %s for v1", id));
                    SubjectOfKGV2Translator translator = new SubjectOfKGV2Translator();
                    return translator.translate(subject, dataStage, liveMode);
                } else {
                    logger.debug(String.format("Failed to query subject %s for v1", id));
                }
            } catch (WebClientResponseException e) {
                if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                    logger.debug(String.format("The subject %s for v1 is skipped because it is missing mandatory fields", id));
                } else {
                    throw e;
                }
            }

        }
        return null;
    }

    public Subject createSubjectFromKGv2(DataStage dataStage, boolean liveMode, String query, String id, String authorization) {
        SubjectV1 subject = kgV2.executeQuery(SubjectV1.class, dataStage, query, id, authorization);
        SubjectOfKGV2Translator translator = new SubjectOfKGV2Translator();
        return translator.translate(subject, dataStage, liveMode);
    }

    public TargetInstance createInstanceFromKGv2(DataStage dataStage, boolean liveMode, String org, String domain, String schema, String version, String id, String legacyAuthorization) {
        String type = String.format("%s/%s/%s/%s", org, domain, schema, version);
        String query = String.format("query/%s/search", type);
        switch (type) {
            case "minds/core/dataset/v1.0.0":
                return this.createDatasetVersionFromKGv2(dataStage, liveMode, query, id, legacyAuthorization);
            case "minds/core/person/v1.0.0":
                return this.createContributorFromKGv2(dataStage, liveMode, Arrays.asList(query, "query/uniminds/core/person/v1.0.0/search"), id, legacyAuthorization);
            case "uniminds/core/person/v1.0.0":
                return this.createContributorFromKGv2(dataStage, liveMode, query, id, legacyAuthorization);
            case "minds/core/placomponent/v1.0.0":
                return this.createProjectFromKGv2(dataStage, liveMode, query, id, legacyAuthorization);
            case "uniminds/core/modelinstance/v1.0.0":
                return this.createModelVersionFromKGv2(dataStage, liveMode, query, id, legacyAuthorization);
            case "softwarecatalog/software/softwareproject/v1.0.0":
                return this.createSoftwareVersionFromKGv2(dataStage, liveMode, query, id, legacyAuthorization);
            case "minds/experiment/subject/v1.0.0":
                return this.createSubjectFromKGv2(dataStage, liveMode, query, id, legacyAuthorization);
            case "minds/experiment/sample/v1.0.0":
                return this.createSampleFromKGv2(dataStage, liveMode, query, id, legacyAuthorization);
        }
        throw new HttpClientErrorException(HttpStatus.NOT_FOUND, String.format("Type %s is not recognized as a valid search resource!", type));
    }

    public TargetInstance createLiveInstanceFromKGv3(DataStage dataStage, boolean liveMode, String id) {
        Map instance = kgV3.fetchInstance(id, dataStage);
        String type;
        try {
            Map data = (Map) instance.get("data");
            List<String> types = (List<String>) data.get("@type");
            type = types.stream().filter(Constants.SOURCE_MODELS::contains).collect(Collectors.toList()).stream().findFirst().orElse(null);
        } catch (Exception e) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, String.format("Instance %s does not exist!", id));
        }
        if (type != null) {
            switch (type) {
                case Constants.SOURCE_MODEL_DATASET:
                    return this.createDatasetFromKGv3(dataStage, liveMode, id);
                case Constants.SOURCE_MODEL_DATASET_VERSIONS:
                    return this.createDatasetVersionFromKGv3(dataStage, liveMode, id);
                case Constants.SOURCE_MODEL_PERSON:
                    return this.createContributorFromKGv3(dataStage, liveMode, id);
                case Constants.SOURCE_MODEL_MODEL:
                    return this.createModelFromKGv3(dataStage, liveMode, id);
                case Constants.SOURCE_MODEL_MODEL_VERSION:
                    return this.createModelVersionFromKGv3(dataStage, liveMode, id);
                case Constants.SOURCE_MODEL_SOFTWARE:
                    return this.createSoftwareFromKGv3(dataStage, liveMode, id);
                case Constants.SOURCE_MODEL_SOFTWARE_VERSION:
                    return this.createSoftwareVersionFromKGv3(dataStage, liveMode, id);
                case Constants.SOURCE_MODEL_PROJECT:
                    return this.createProjectFromKGv3(dataStage, liveMode, id);
                case Constants.SOURCE_MODEL_SUBJECT:
                    return this.createSubjectFromKGv3(dataStage, liveMode, id);
                case Constants.SOURCE_MODEL_SAMPLE:
                    return this.createSampleFromKGv3(dataStage, liveMode, id);
                default:
                    return null;
            }
        }
        throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Type is not recognized as a valid search resource!");
    }

    private List<IdSources> getSamplesIdSources(DataStage dataStage, String legacyAuthorization) {
        String query = "query/minds/experiment/sample/v1.0.0/searchIdentifier";
        logger.info("Starting to query sample's ids for v1");
        List<SourceInstanceIdentifierV1andV2> sourceInstanceV1 = kgV2.executeQueryForIndexing(dataStage, query, legacyAuthorization);
        logger.info(String.format("Queried %s sample's ids for v1", sourceInstanceV1.size()));
        logger.info("Starting to query sample's ids for v3");
        List<SourceInstanceV3> sourceInstanceV3 = kgV3.executeQueryForIndexing(dataStage, Queries.SAMPLE_IDENTIFIER_QUERY_ID);
        logger.info(String.format("Successfully queried %s sample's ids for v3", sourceInstanceV3.size()));
        List<IdSources> sources = new ArrayList<>();

        Map<String, IdSources> sourcesIdentifiers = new HashMap<>();

        sourceInstanceV3.forEach(dv -> {
            IdSources source = new IdSources();
            sources.add(source);
            String id = IdUtils.getUUID(dv.getId());
            source.setIdV3(id);
            dv.getIdentifier().forEach(identifier -> sourcesIdentifiers.put(IdUtils.getUUID(identifier), source));
        });

        sourceInstanceV1.forEach(d -> {
            String identifier = d.getIdentifier();
            if (!sourcesIdentifiers.containsKey(identifier)) {
                IdSources source = new IdSources();
                source.setIdV1(IdUtils.getUUID(d.getId()));
                sources.add(source);
            }
        });
        return sources;
    }

    private List<IdSources> getSubjectsIdSources(DataStage dataStage, String legacyAuthorization) {
        String query = "query/minds/experiment/subject/v1.0.0/searchIdentifier";
        logger.info("Starting to query subject's ids for v1");
        List<SourceInstanceIdentifierV1andV2> sourceInstanceV1 = kgV2.executeQueryForIndexing(dataStage, query, legacyAuthorization);
        logger.info(String.format("Queried %s subject's ids for v1", sourceInstanceV1.size()));
        logger.info("Starting to query subject's ids for v3");
        List<SourceInstanceV3> sourceInstanceV3 = kgV3.executeQueryForIndexing(dataStage, Queries.SUBJECT_IDENTIFIER_QUERY_ID);
        logger.info(String.format("Successfully queried %s subject's ids for v3", sourceInstanceV3.size()));
        List<IdSources> sources = new ArrayList<>();

        Map<String, IdSources> sourcesIdentifiers = new HashMap<>();

        sourceInstanceV3.forEach(dv -> {
            IdSources source = new IdSources();
            sources.add(source);
            String id = IdUtils.getUUID(dv.getId());
            source.setIdV3(id);
            dv.getIdentifier().forEach(identifier -> sourcesIdentifiers.put(IdUtils.getUUID(identifier), source));
        });

        sourceInstanceV1.forEach(d -> {
            String identifier = d.getIdentifier();
            if (!sourcesIdentifiers.containsKey(identifier)) {
                IdSources source = new IdSources();
                source.setIdV1(IdUtils.getUUID(d.getId()));
                sources.add(source);
            }
        });
        return sources;
    }

    private List<IdSources> getProjectsIdSources(DataStage dataStage, String legacyAuthorization) {
        String query = "query/minds/core/placomponent/v1.0.0/searchIdentifier";
        logger.info("Starting to query project's ids for v1");
        List<SourceInstanceIdentifierV1andV2> sourceInstanceV1 = kgV2.executeQueryForIndexing(dataStage, query, legacyAuthorization);
        logger.info(String.format("Successfully Queried %s project's ids for v1", sourceInstanceV1.size()));
        logger.info("Starting to query project's ids for v3");
        List<SourceInstanceV3> sourceInstanceV3 = kgV3.executeQueryForIndexing(dataStage, Queries.PROJECT_IDENTIFIER_QUERY_ID);
        logger.info(String.format("Successfully queried %s project's ids for v3", sourceInstanceV3.size()));
        List<IdSources> sources = new ArrayList<>();

        Map<String, IdSources> sourcesIdentifiers = new HashMap<>();

        sourceInstanceV3.forEach(dv -> {
            IdSources source = new IdSources();
            sources.add(source);
            String id = IdUtils.getUUID(dv.getId());
            source.setIdV3(id);
            dv.getIdentifier().forEach(identifier -> sourcesIdentifiers.put(IdUtils.getUUID(identifier), source));
        });

        sourceInstanceV1.forEach(d -> {
            String identifier = d.getIdentifier();
            if (!sourcesIdentifiers.containsKey(identifier)) {
                IdSources source = new IdSources();
                source.setIdV1(IdUtils.getUUID(d.getId()));
                sources.add(source);
            }
        });
        return sources;
    }

    private List<IdSources> getSoftwareVersionsIdSources(DataStage dataStage, String legacyAuthorization) {
        String query = "query/softwarecatalog/software/softwareproject/v1.0.0/searchIdentifier";
        logger.info("Starting to query software's ids for v2");
        List<SourceInstanceIdentifierV1andV2> sourceInstanceV2 = kgV2.executeQueryForIndexing(dataStage, query, legacyAuthorization);
        logger.info(String.format("Queried %s software's ids for v2", sourceInstanceV2.size()));
        logger.info("Starting to query softwareVersion's ids for v3");
        List<SourceInstanceV3> sourceInstanceV3 = kgV3.executeQueryForIndexing(dataStage, Queries.SOFTWARE_VERSION_IDENTIFIER_QUERY_ID);
        logger.info(String.format("Successfully queried %s softwareVersion's ids for v3", sourceInstanceV3.size()));
        List<IdSources> sources = new ArrayList<>();

        Map<String, IdSources> sourcesIdentifiers = new HashMap<>();

        sourceInstanceV3.forEach(dv -> {
            IdSources source = new IdSources();
            sources.add(source);
            String id = IdUtils.getUUID(dv.getId());
            source.setIdV3(id);
            dv.getIdentifier().forEach(identifier -> sourcesIdentifiers.put(IdUtils.getUUID(identifier), source));
        });

        sourceInstanceV2.forEach(d -> {
            String identifier = d.getIdentifier();
            if (!sourcesIdentifiers.containsKey(identifier)) {
                IdSources source = new IdSources();
                source.setIdV2(IdUtils.getUUID(d.getId()));
                sources.add(source);
            }
        });
        return sources;
    }

    private List<IdSources> getModelVersionsIdSources(DataStage dataStage, String legacyAuthorization) {
        String query = "query/uniminds/core/modelinstance/v1.0.0/searchIdentifier";
        logger.info("Starting to query model's ids for v2");
        List<SourceInstanceIdentifierV1andV2> sourceInstanceV2 = kgV2.executeQueryForIndexing(dataStage, query, legacyAuthorization);
        logger.info(String.format("Queried %s model's ids for v2", sourceInstanceV2.size()));
        logger.info("Starting to query modelVersion's ids for v3");
        List<SourceInstanceV3> sourceInstanceV3 = kgV3.executeQueryForIndexing(dataStage, Queries.MODEL_VERSION_IDENTIFIER_QUERY_ID);
        logger.info(String.format("Successfully queried %s modelVersion's ids for v3", sourceInstanceV3.size()));
        List<IdSources> sources = new ArrayList<>();

        Map<String, IdSources> sourcesIdentifiers = new HashMap<>();

        sourceInstanceV3.forEach(dv -> {
            IdSources source = new IdSources();
            sources.add(source);
            String id = IdUtils.getUUID(dv.getId());
            source.setIdV3(id);
            dv.getIdentifier().forEach(identifier -> sourcesIdentifiers.put(IdUtils.getUUID(identifier), source));
        });

        sourceInstanceV2.forEach(d -> {
            String identifier = d.getIdentifier();
            if (!sourcesIdentifiers.containsKey(identifier)) {
                IdSources source = new IdSources();
                source.setIdV2(IdUtils.getUUID(d.getId()));
                sources.add(source);
            }
        });
        return sources;
    }

    private List<IdSources> getContributorsIdSources(DataStage dataStage, String legacyAuthorization) {
        String queryForV1 = "query/minds/core/person/v1.0.0/searchIdentifier";
        String queryForV2 = "query/uniminds/core/person/v1.0.0/searchIdentifier";
        logger.info("Starting to query contributor's ids for v1");
        List<SourceInstanceIdentifierV1andV2> sourceInstanceV1 = kgV2.executeQueryForIndexing(dataStage, queryForV1, legacyAuthorization);
        logger.info(String.format("Successfully queried %s contributor's ids for v1", sourceInstanceV1.size()));
        logger.info("Done querying contributors for v1");
        logger.info("Starting to query contributor's ids for v2");
        List<SourceInstanceIdentifierV1andV2> sourceInstanceV2 = kgV2.executeQueryForIndexing(dataStage, queryForV2, legacyAuthorization);
        logger.info(String.format("Successfully queried %s contributor's ids for v2", sourceInstanceV2.size()));

        logger.info("Starting to query contributor's ids for v3");
        List<SourceInstanceV3> sourceInstanceV3 = kgV3.executeQueryForIndexing(dataStage, Queries.CONTRIBUTOR_IDENTIFIER_QUERY_ID);
        logger.info(String.format("Successfully Successfully queried  %s contributor's ids for v3", sourceInstanceV3.size()));

        Map<String, IdSources> sourcesIdentifiers = new HashMap<>();
        List<IdSources> sources = new ArrayList<>();

        sourceInstanceV3.forEach(dv -> {
            IdSources source = new IdSources();
            sources.add(source);
            String id = IdUtils.getUUID(dv.getId());
            source.setIdV3(id);
            dv.getIdentifier().forEach(identifier -> sourcesIdentifiers.put(IdUtils.getUUID(identifier), source));
        });

        sourceInstanceV2.forEach(p -> {
            String identifier = p.getIdentifier();
            IdSources source = sourcesIdentifiers.get(identifier);
            if (source == null) {
                source = new IdSources();
                sources.add(source);
                sourcesIdentifiers.put(identifier, source);
            }
            source.setIdV2(IdUtils.getUUID(p.getId()));
        });

        sourceInstanceV1.forEach(p -> {
            String identifier = p.getIdentifier();
            IdSources source = sourcesIdentifiers.get(identifier);
            if (source == null) {
                source = new IdSources();
                sources.add(source);
            }
            source.setIdV1(IdUtils.getUUID(p.getId()));
        });
        return sources;
    }

    private List<IdSources> getDatasetVersionsIdSources(DataStage dataStage, String legacyAuthorization) {
        String query = "query/minds/core/dataset/v1.0.0/searchIdentifier";
        logger.info("Starting to query dataset's ids for v1");
        List<SourceInstanceIdentifierV1andV2> sourceInstanceV1 = kgV2.executeQueryForIndexing(dataStage, query, legacyAuthorization);
        logger.debug(String.format("Successfully queried %s dataset's ids for v1", sourceInstanceV1.size()));
        logger.info("Starting to query datasetVersion's ids for v3");
        List<SourceInstanceV3> sourceInstanceV3 = kgV3.executeQueryForIndexing(dataStage, Queries.DATASET_VERSION_IDENTIFIER_QUERY_ID);
        logger.info(String.format("Successfully queried %s datasetVersion's ids for v3", sourceInstanceV3.size()));
        List<IdSources> sources = new ArrayList<>();

        Map<String, IdSources> sourcesIdentifiers = new HashMap<>();

        sourceInstanceV3.forEach(dv -> {
            IdSources source = new IdSources();
            sources.add(source);
            String id = IdUtils.getUUID(dv.getId());
            source.setIdV3(id);
            dv.getIdentifier().forEach(identifier -> sourcesIdentifiers.put(IdUtils.getUUID(identifier), source));
        });

        sourceInstanceV1.forEach(d -> {
            String identifier = d.getIdentifier();
            if (!sourcesIdentifiers.containsKey(identifier)) {
                IdSources source = new IdSources();
                source.setIdV1(IdUtils.getUUID(d.getId()));
                sources.add(source);
            }
        });
        return sources;
    }

    public TargetInstance createInstanceCombinedForIndexing(Class<?> clazz, DataStage dataStage, boolean liveMode, String legacyAuthorization, IdSources source) {
        if (clazz == DatasetVersion.class) {
            return this.createDatasetVersionForIndexing(dataStage, liveMode, legacyAuthorization, source);
        }
        if (clazz == Contributor.class) {
            return this.createContributorForIndexing(dataStage, liveMode, legacyAuthorization, source);
        }
        if (clazz == Project.class) {
            return this.createProjectForIndexing(dataStage, liveMode, legacyAuthorization, source);
        }
        if (clazz == ModelVersion.class) {
            return this.createModelVersionForIndexing(dataStage, liveMode, legacyAuthorization, source);
        }
        if (clazz == SoftwareVersion.class) {
            return this.createSoftwareVersionForIndexing(dataStage, liveMode, legacyAuthorization, source);
        }
        return null;
    }

    public TargetInstancesResult createInstancesFromV3ForIndexing(Class<?> clazz, DataStage dataStage, boolean liveMode, int from, int size) {
        if (clazz == Dataset.class) {
            return this.createDatasetsForIndexing(dataStage, liveMode, from, size);
        }
        if (clazz == Model.class) {
            return this.createModelsForIndexing(dataStage, liveMode, from, size);
        }
        if (clazz == Software.class) {
            return this.createSoftwaresForIndexing(dataStage, liveMode, from, size);
        }
        return new TargetInstancesResult();
    }

    public List<IdSources> getIdSources(Class<?> clazz, DataStage dataStage, String legacyAuthorization) {
        if (clazz == DatasetVersion.class) {
            return this.getDatasetVersionsIdSources(dataStage, legacyAuthorization);
        }
        if (clazz == Contributor.class) {
            return this.getContributorsIdSources(dataStage, legacyAuthorization);
        }
        if (clazz == Project.class) {
            return this.getProjectsIdSources(dataStage, legacyAuthorization);
        }
        if (clazz == Subject.class) {
            return this.getSubjectsIdSources(dataStage, legacyAuthorization);
        }
        if (clazz == Sample.class) {
            return this.getSamplesIdSources(dataStage, legacyAuthorization);
        }
        if (clazz == ModelVersion.class) {
            return this.getModelVersionsIdSources(dataStage, legacyAuthorization);
        }
        if (clazz == SoftwareVersion.class) {
            return this.getSoftwareVersionsIdSources(dataStage, legacyAuthorization);
        }
        return Collections.emptyList();
    }

    public boolean isTypeCombined(Class<?> clazz) {
        return clazz != Dataset.class && clazz != Model.class && clazz != Software.class;
    }
}
