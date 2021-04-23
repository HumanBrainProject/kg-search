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
import eu.ebrains.kg.search.model.target.elasticsearch.TargetInstances;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.*;
import eu.ebrains.kg.search.utils.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;

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

    private static class ResultOfKGV2PersonV1 extends ResultOfKGv2<PersonV1> {}
    private static class ResultOfKGV2PersonV2 extends ResultOfKGv2<PersonV2> {}
    private static class ResultOfKGV3PersonV3 extends ResultOfKGv3<PersonV3> {}

    private TargetInstances createContributors(DataStage dataStage, boolean liveMode, String legacyAuthorization) {
        String queryForV1 = "query/minds/core/person/v1.0.0/search";
        String queryForV2 = "query/uniminds/core/person/v1.0.0/search";
        logger.info("Starting to query contributors for v1");
        ResultOfKGV2PersonV1 personsFromV1 = kgV2.executeQueryForIndexing(ResultOfKGV2PersonV1.class, queryForV1, legacyAuthorization, dataStage);
        logger.info(String.format("Queried %s contributors for v1", CollectionUtils.isEmpty(personsFromV1.getResults()) ? 0 : personsFromV1.getResults().size()));
        logger.info("Done querying contributors for v1");
        logger.info("Starting to query contributors for v2");
        ResultOfKGv2<PersonV2> personsFromV2 = kgV2.executeQueryForIndexing(ResultOfKGV2PersonV2.class, queryForV2, legacyAuthorization, dataStage);
        logger.info(String.format("Queried %s contributors for v2", CollectionUtils.isEmpty(personsFromV2.getResults()) ? 0 : personsFromV2.getResults().size()));

        logger.info("Starting to query contributors for v3");
        ResultOfKGv3<PersonV3> personsFromV3 = kgV3.executeQueryForIndexing(ResultOfKGV3PersonV3.class, Queries.CONTRIBUTOR_QUERY_ID, dataStage);
        logger.info("Done querying contributors for v3");

        List<PersonSources> personSources = new ArrayList<>();
        personsFromV1.getResults().forEach(p -> {
            PersonSources source = new PersonSources();
            personSources.add(source);
            source.setPersonV1(p);
        });

        Map<String, PersonSources> personSourcesByV1Identifier = personSources.stream().collect(Collectors.toMap(k -> k.getPersonV1().getIdentifier(), v -> v));

        personsFromV2.getResults().forEach(p -> {
            PersonSources source = personSourcesByV1Identifier.get(p.getIdentifier());
            if (source == null) {
                source = new PersonSources();
                personSources.add(source);
            }
            source.setPersonV2(p);
        });

        Map<String, PersonSources> personSourcesByV1AndV2Identifier = personSources.stream().collect(Collectors.toMap(k -> {
            if (k.getPersonV1() != null) {
                return k.getPersonV1().getIdentifier();
            }
            return k.getPersonV2().getIdentifier();
        }, v -> v));

        personsFromV3.getData().forEach(p -> {
            String id = p.getIdentifier().stream().reduce(null, (found, i) -> {
                if (found == null) {
                    String uuid = IdUtils.getUUID(i);
                    PersonSources source = personSourcesByV1AndV2Identifier.get(uuid);
                    if (source != null) {
                        source.setPersonV3(p);
                        return uuid;
                    }
                }
                return found;
            });
            if (id == null) {
                PersonSources source = new PersonSources();
                source.setPersonV3(p);
                personSources.add(source);
            }
        });

        List<TargetInstance> list = personSources.stream().map(p -> {
            Contributor ContributorOfKGV2 = null;
            Contributor ContributorOfKGV3 = null;
            if (p.getPersonV1() != null || p.getPersonV2() != null) {
                ContributorOfKGV2Translator translator = new ContributorOfKGV2Translator();
                ContributorOfKGV2 = translator.translate(p, dataStage, liveMode);
            }
            if (p.getPersonV3() != null) {
                ContributorOfKgV3Translator translator = new ContributorOfKgV3Translator();
                ContributorOfKGV3 = translator.translate(p.getPersonV3(), dataStage, liveMode);
            }
//            if ((p.getPersonV1() != null || p.getPersonV2() != null) && p.getPersonV3() != null) {
//                System.out.println(p.getPersonV3().getId());
//            }
            return ContributorHelpers.merge(ContributorOfKGV2, ContributorOfKGV3);
        }).collect(Collectors.toList());
        TargetInstances result = new TargetInstances();
        result.setSearchableInstances(list);
        return result;
    }

    public Contributor createContributorFromKGv2(DataStage dataStage, boolean liveMode, String query, String id, String authorization) {
        PersonV2 personV2 = kgV2.fetchInstance(PersonV2.class, query, id, authorization, dataStage);
        ContributorOfKGV2Translator translator = new ContributorOfKGV2Translator();
        PersonSources personSource = new PersonSources();
        personSource.setPersonV2(personV2);
        return translator.translate(personSource, dataStage, liveMode);
    }

    public Contributor createContributorFromKGv3(DataStage dataStage, boolean liveMode, String id) {
        ResultOfKGV3PersonV3 resultOfKGV3PersonV3 = kgV3.executeQueryForLive(ResultOfKGV3PersonV3.class, Queries.CONTRIBUTOR_QUERY_ID, id, dataStage);
        PersonV3 personV3 = resultOfKGV3PersonV3.getData().stream().findFirst().orElse(null);
        if (personV3 != null) {
            ContributorOfKgV3Translator translator = new ContributorOfKgV3Translator();
            return translator.translate(personV3, dataStage, liveMode);
        }
        throw new HttpClientErrorException(HttpStatus.NOT_FOUND, String.format("Person %s does not exist!", id));
    }

    private static class PersonV2Result extends ResultOfKGv2<PersonV2> {
    }

    public Contributor createContributorFromKGv2(DataStage dataStage, boolean liveMode, List<String> queries, String id, String authorization) {
        String personV1Query = queries.get(0);
        String personV2Query = queries.get(1);
        PersonSources personSource = new PersonSources();
        PersonV1 personV1 = kgV2.fetchInstance(PersonV1.class, personV1Query, id, authorization, dataStage);
        personSource.setPersonV1(personV1);
        PersonV2Result personV2Result = kgV2.fetchInstanceByIdentifier(PersonV2Result.class, personV2Query, personV1.getIdentifier(), authorization, dataStage);
        if (personV2Result != null && !CollectionUtils.isEmpty(personV2Result.getResults())) {
            PersonV2 personV2 = personV2Result.getResults().get(0);
            personSource.setPersonV2(personV2);
        }
        ContributorOfKGV2Translator translator = new ContributorOfKGV2Translator();
        return translator.translate(personSource, dataStage, liveMode);
    }

    private static class ResultOfKGV2SoftwareV2 extends ResultOfKGv2<SoftwareV2> {
    }

    public TargetInstances createSoftwareVersions(DataStage dataStage, boolean liveMode, String legacyAuthorization) {
        String query = "query/softwarecatalog/software/softwareproject/v1.0.0/search";
        logger.info("Starting to query software for v2");
        ResultOfKGV2SoftwareV2 softwareV2 = kgV2.executeQueryForIndexing(ResultOfKGV2SoftwareV2.class, query, legacyAuthorization, dataStage);
        logger.info(String.format("Queried %s softwares for v2", CollectionUtils.isEmpty(softwareV2.getResults()) ? 0 : softwareV2.getResults().size()));
        logger.info("Starting to query softwareVersions for v3");
        ResultOfKGv3<SoftwareVersionV3> softwareVersionsV3 = kgV3.executeQueryForIndexing(ResultOfKGV3SoftwareVersionV3.class, Queries.SOFTWARE_VERSION_QUERY_ID, dataStage);
        logger.info(String.format("Queried %s softwareVersions for v3", CollectionUtils.isEmpty(softwareVersionsV3.getData()) ? 0 : softwareVersionsV3.getData().size()));
        List<SoftwareSources> softwareSources = new ArrayList<>();

        Map<String, SoftwareSources> sourcesIdentifiers = new HashMap<>();

        softwareVersionsV3.getData().forEach(sv -> {
            SoftwareSources source = new SoftwareSources();
            softwareSources.add(source);
            source.setSoftwareVersionV3(sv);
            sv.getIdentifier().forEach(id -> sourcesIdentifiers.put(id, source));
        });

        softwareV2.getResults().forEach(d -> {
            String id = d.getIdentifier();
            if (!sourcesIdentifiers.containsKey(id)) {
                SoftwareSources source = new SoftwareSources();
                source.setSoftwareV2(d);
                softwareSources.add(source);
            }
        });

        TargetInstances targetInstances = new TargetInstances();
        softwareSources.forEach(s -> {
            SoftwareVersionV3 sV3 = s.getSoftwareVersionV3();
            if (sV3 != null) {
                SoftwareVersionOfKGV3Translator translator = new SoftwareVersionOfKGV3Translator();
                targetInstances.addInstance(translator.translate(sV3, dataStage, liveMode), true);
            } else {
                SoftwareVersionOfKGV2Translator translator = new SoftwareVersionOfKGV2Translator();
                targetInstances.addInstance(translator.translate(s.getSoftwareV2(), dataStage, liveMode), true);
            }
        });
        return targetInstances;
    }

    public SoftwareVersion createSoftwareVersionFromKGv2(DataStage dataStage, boolean liveMode, String query, String id, String authorization) {
        SoftwareV2 software = kgV2.fetchInstance(SoftwareV2.class, query, id, authorization, dataStage);
        SoftwareVersionOfKGV2Translator translator = new SoftwareVersionOfKGV2Translator();
        return translator.translate(software, dataStage, liveMode);
    }

    private static class ResultOfKGV2DatasetV1 extends ResultOfKGv2<DatasetV1> {}

    private static class ResultOfKGV3DatasetV3 extends ResultOfKGv3<DatasetV3> {}

    private static class ResultOfKGV3DatasetVersionV3 extends ResultOfKGv3<DatasetVersionV3> {}

    public TargetInstances createDatasetVersions(DataStage dataStage, boolean liveMode, String legacyAuthorization) {
        String query = "query/minds/core/dataset/v1.0.0/search";
        logger.info("Starting to query datasets for v1");
        ResultOfKGV2DatasetV1 datasetV1 = kgV2.executeQueryForIndexing(ResultOfKGV2DatasetV1.class, query, legacyAuthorization, dataStage);
        logger.info(String.format("Queried %s datasets for v1", CollectionUtils.isEmpty(datasetV1.getResults()) ? 0 : datasetV1.getResults().size()));
        logger.info("Starting to query datasetVersions for v3");
        ResultOfKGv3<DatasetVersionV3> datasetVersionsV3 = kgV3.executeQueryForIndexing(ResultOfKGV3DatasetVersionV3.class, Queries.DATASET_VERSION_QUERY_ID, dataStage);
        logger.info(String.format("Queried %s datasetVersions for v3", CollectionUtils.isEmpty(datasetVersionsV3.getData()) ? 0 : datasetVersionsV3.getData().size()));
        List<DatasetSources> datasetSources = new ArrayList<>();

        Map<String, DatasetSources> sourcesIdentifiers = new HashMap<>();

        datasetVersionsV3.getData().forEach(dv -> {
            DatasetSources source = new DatasetSources();
            datasetSources.add(source);
            source.setDatasetVersionV3(dv);
            dv.getIdentifier().forEach(id -> sourcesIdentifiers.put(id, source));
        });

        datasetV1.getResults().forEach(d -> {
            String id = d.getIdentifier();
            if (!sourcesIdentifiers.containsKey(id)) {
                DatasetSources source = new DatasetSources();
                source.setDatasetV1(d);
                datasetSources.add(source);
            }
        });

        TargetInstances targetInstances = new TargetInstances();
        datasetSources.forEach(d -> {
            DatasetVersionV3 dV3 = d.getDatasetVersionV3();
            if (dV3 != null) {
                DatasetVersionOfKGV3Translator translator = new DatasetVersionOfKGV3Translator();
                targetInstances.addInstance(translator.translate(dV3, dataStage, liveMode), true);
            } else {
                DatasetVersionOfKGV2Translator translator = new DatasetVersionOfKGV2Translator();
                targetInstances.addInstance(translator.translate(d.getDatasetV1(), dataStage, liveMode), true);
            }
        });
        return targetInstances;
    }

    public DatasetVersion createDatasetVersionFromKGv2(DataStage dataStage, boolean liveMode, String query, String id, String authorization) {
        DatasetV1 datasetV1 = kgV2.fetchInstance(DatasetV1.class, query, id, authorization, dataStage);
        DatasetVersionOfKGV2Translator translator = new DatasetVersionOfKGV2Translator();
        return translator.translate(datasetV1, dataStage, liveMode);
    }

    public DatasetVersion createDatasetVersionFromKGv3(DataStage dataStage, boolean liveMode, String id) {
        ResultOfKGV3DatasetVersionV3 result = kgV3.executeQueryForLive(ResultOfKGV3DatasetVersionV3.class, Queries.DATASET_VERSION_QUERY_ID, id, dataStage);
        DatasetVersionV3 datasetVersion = result.getData().stream().findFirst().orElse(null);
        if (datasetVersion != null) {
            DatasetVersionOfKGV3Translator translator = new DatasetVersionOfKGV3Translator();
            return translator.translate(datasetVersion, dataStage, liveMode);
        }
        throw new HttpClientErrorException(HttpStatus.NOT_FOUND, String.format("DatasetVersion %s does not exist!", id));
    }

    private static class ResultOfKGV3ModelVersionV3 extends ResultOfKGv3<ModelVersionV3> {}

    public ModelVersion createModelVersionFromKGv3(DataStage dataStage, boolean liveMode, String id) {
        ResultOfKGV3ModelVersionV3 result = kgV3.executeQueryForLive(ResultOfKGV3ModelVersionV3.class, Queries.MODEL_VERSION_QUERY_ID, id, dataStage);
        ModelVersionV3 modelVersion = result.getData().stream().findFirst().orElse(null);
        if (modelVersion != null) {
            ModelVersionOfKGV3Translator translator = new ModelVersionOfKGV3Translator();
            return translator.translate(modelVersion, dataStage, liveMode);
        }
        throw new HttpClientErrorException(HttpStatus.NOT_FOUND, String.format("ModelVersion %s does not exist!", id));
    }

    private static class ResultOfKGV3SoftwareVersionV3 extends ResultOfKGv3<SoftwareVersionV3> {}

    public SoftwareVersion createSoftwareVersionFromKGv3(DataStage dataStage, boolean liveMode, String id) {
        ResultOfKGV3SoftwareVersionV3 result = kgV3.executeQueryForLive(ResultOfKGV3SoftwareVersionV3.class, Queries.SOFTWARE_VERSION_QUERY_ID, id, dataStage);
        SoftwareVersionV3 softwareVersion = result.getData().stream().findFirst().orElse(null);
        if (softwareVersion != null) {
            SoftwareVersionOfKGV3Translator translator = new SoftwareVersionOfKGV3Translator();
            return translator.translate(softwareVersion, dataStage, liveMode);
        }
        throw new HttpClientErrorException(HttpStatus.NOT_FOUND, String.format("SoftwareVersion %s does not exist!", id));
    }

    public TargetInstances createDatasets(DataStage dataStage, boolean liveMode) {
        logger.info("Starting to query datasets for v3");
        ResultOfKGv3<DatasetV3> datasets = kgV3.executeQueryForIndexing(ResultOfKGV3DatasetV3.class, Queries.DATASET_QUERY_ID, dataStage);
        logger.info(String.format("Queried %d datasets for v3", CollectionUtils.isEmpty(datasets.getData()) ? 0 : datasets.getData().size()));
        DatasetOfKGV3Translator translator = new DatasetOfKGV3Translator();
        List<TargetInstance> list = datasets.getData().stream().map(s -> (TargetInstance) translator.translate(s, dataStage, liveMode)).collect(Collectors.toList());
        TargetInstances result = new TargetInstances();
        result.setNonSearchableInstances(list);
        return result;
    }

    public TargetInstances createModels(DataStage dataStage, boolean liveMode) {
        logger.info("Starting to query models for v3");
        ResultOfKGv3<ModelV3> models = kgV3.executeQueryForIndexing(ResultOfKGV3ModelV3.class, Queries.MODEL_QUERY_ID, dataStage);
        logger.info(String.format("Queried %d models for v3", CollectionUtils.isEmpty(models.getData()) ? 0 : models.getData().size()));
        ModelOfKGV3Translator translator = new ModelOfKGV3Translator();
        List<TargetInstance> list = models.getData().stream().map(s -> (TargetInstance) translator.translate(s, dataStage, liveMode)).collect(Collectors.toList());
        TargetInstances result = new TargetInstances();
        result.setNonSearchableInstances(list);
        return result;
    }


    public TargetInstances createSoftwares(DataStage dataStage, boolean liveMode) {
        logger.info("Starting to query softwares for v3");
        ResultOfKGv3<SoftwareV3> softwares = kgV3.executeQueryForIndexing(ResultOfKGV3SoftwareV3.class, Queries.SOFTWARE_QUERY_ID, dataStage);
        logger.info(String.format("Queried %d softwares for v3", CollectionUtils.isEmpty(softwares.getData()) ? 0 : softwares.getData().size()));
        SoftwareOfKGV3Translator translator = new SoftwareOfKGV3Translator();
        List<TargetInstance> list = softwares.getData().stream().map(s -> (TargetInstance) translator.translate(s, dataStage, liveMode)).collect(Collectors.toList());
        TargetInstances result = new TargetInstances();
        result.setNonSearchableInstances(list);
        return result;
    }

    public Dataset createDatasetFromKGv3(DataStage dataStage, boolean liveMode, String id) {
        ResultOfKGV3DatasetV3 result = kgV3.executeQueryForLive(ResultOfKGV3DatasetV3.class, Queries.DATASET_QUERY_ID, id, dataStage);
        DatasetV3 dataset = result.getData().stream().findFirst().orElse(null);
        if (dataset != null) {
            DatasetOfKGV3Translator translator = new DatasetOfKGV3Translator();
            return translator.translate(dataset, dataStage, liveMode);
        }
        throw new HttpClientErrorException(HttpStatus.NOT_FOUND, String.format("Dataset %s does not exist!", id));
    }

    private static class ResultOfKGV3ModelV3 extends ResultOfKGv3<ModelV3> {}

    public Model createModelFromKGv3(DataStage dataStage, boolean liveMode, String id) {
        ResultOfKGV3ModelV3 result = kgV3.executeQueryForLive(ResultOfKGV3ModelV3.class, Queries.MODEL_QUERY_ID, id, dataStage);
        ModelV3 model = result.getData().stream().findFirst().orElse(null);
        if (model != null) {
            ModelOfKGV3Translator translator = new ModelOfKGV3Translator();
            return translator.translate(model, dataStage, liveMode);
        }
        throw new HttpClientErrorException(HttpStatus.NOT_FOUND, String.format("Model %s does not exist!", id));
    }

    private static class ResultOfKGV3SoftwareV3 extends ResultOfKGv3<SoftwareV3> {}

    public Software createSoftwareFromKGv3(DataStage dataStage, boolean liveMode, String id) {
        ResultOfKGV3SoftwareV3 result = kgV3.executeQueryForLive(ResultOfKGV3SoftwareV3.class, Queries.SOFTWARE_QUERY_ID, id, dataStage);
        SoftwareV3 software = result.getData().stream().findFirst().orElse(null);
        if (software != null) {
            SoftwareOfKGV3Translator translator = new SoftwareOfKGV3Translator();
            return translator.translate(software, dataStage, liveMode);
        }
        throw new HttpClientErrorException(HttpStatus.NOT_FOUND, String.format("Software %s does not exist!", id));
    }

    private static class ResultOfKGV2ModelV2 extends ResultOfKGv2<ModelV2> { }


    public TargetInstances createModelVersions(DataStage dataStage, boolean liveMode, String legacyAuthorization) {
        String query = "query/uniminds/core/modelinstance/v1.0.0/search";
        logger.info("Starting to query model for v2");
        ResultOfKGV2ModelV2 modelV2 = kgV2.executeQueryForIndexing(ResultOfKGV2ModelV2.class, query, legacyAuthorization, dataStage);
        logger.info(String.format("Queried %s models for v2", CollectionUtils.isEmpty(modelV2.getResults()) ? 0 : modelV2.getResults().size()));
        logger.info("Starting to query modelVersions for v3");
        ResultOfKGv3<ModelVersionV3> modelVersionsV3 = kgV3.executeQueryForIndexing(ResultOfKGV3ModelVersionV3.class, Queries.MODEL_VERSION_QUERY_ID, dataStage);
        logger.info(String.format("Queried %s modelVersions for v3", CollectionUtils.isEmpty(modelVersionsV3.getData()) ? 0 : modelVersionsV3.getData().size()));
        List<ModelSources> modelSources = new ArrayList<>();

        Map<String, ModelSources> sourcesIdentifiers = new HashMap<>();

        modelVersionsV3.getData().forEach(mv -> {
            ModelSources source = new ModelSources();
            modelSources.add(source);
            source.setModelVersionV3(mv);
            mv.getIdentifier().forEach(id -> sourcesIdentifiers.put(id, source));
        });

        modelV2.getResults().forEach(m -> {
            String id = m.getIdentifier();
            if (!sourcesIdentifiers.containsKey(id)) {
                ModelSources source = new ModelSources();
                source.setModelV2(m);
                modelSources.add(source);
            }
        });

        TargetInstances targetInstances = new TargetInstances();
        modelSources.forEach(m -> {
            ModelVersionV3 mV3 = m.getModelVersionV3();
            if (mV3 != null) {
                ModelVersionOfKGV3Translator translator = new ModelVersionOfKGV3Translator();
                targetInstances.addInstance(translator.translate(mV3, dataStage, liveMode), true);
            } else {
                ModelVersionOfKGV2Translator translator = new ModelVersionOfKGV2Translator();
                targetInstances.addInstance(translator.translate(m.getModelV2(), dataStage, liveMode), true);
            }
        });
        return targetInstances;
    }


    public ModelVersion createModelVersionFromKGv2(DataStage dataStage, boolean liveMode, String query, String id, String authorization) {
        ModelV2 model = kgV2.fetchInstance(ModelV2.class, query, id, authorization, dataStage);
        ModelVersionOfKGV2Translator translator = new ModelVersionOfKGV2Translator();
        return translator.translate(model, dataStage, liveMode);
    }

    private static class ResultOfKGV2ProjectV1 extends ResultOfKGv2<ProjectV1> {
    }

    public TargetInstances createProjects(DataStage dataStage, boolean liveMode, String legacyAuthorization) {
        String query = "query/minds/core/placomponent/v1.0.0/search";
        logger.info("Starting to query projects for v1");
        ResultOfKGV2ProjectV1 project = kgV2.executeQueryForIndexing(ResultOfKGV2ProjectV1.class, query, legacyAuthorization, dataStage);
        logger.info(String.format("Queried %s projects for v1", CollectionUtils.isEmpty(project.getResults()) ? 0 : project.getResults().size()));
        ProjectTranslator translator = new ProjectTranslator();
        List<TargetInstance> list = project.getResults().stream().map(p -> (TargetInstance) translator.translate(p, dataStage, liveMode)).collect(Collectors.toList());
        TargetInstances result = new TargetInstances();
        result.setSearchableInstances(list);
        return result;
    }

    public Project createProjectFromKGv2(DataStage dataStage, boolean liveMode, String query, String id, String authorization) {
        ProjectV1 project = kgV2.fetchInstance(ProjectV1.class, query, id, authorization, dataStage);
        ProjectTranslator translator = new ProjectTranslator();
        return translator.translate(project, dataStage, liveMode);
    }

    private static class ResultOfKGV2SampleV1 extends ResultOfKGv2<SampleV1> {
    }

    public TargetInstances createSamples(DataStage dataStage, boolean liveMode, String legacyAuthorization) {
        String query = "query/minds/experiment/sample/v1.0.0/search";
        logger.info("Starting to query samples for v1");
        ResultOfKGV2SampleV1 sample = kgV2.executeQueryForIndexing(ResultOfKGV2SampleV1.class, query, legacyAuthorization, dataStage);
        logger.info(String.format("Queried %s samples for v1", CollectionUtils.isEmpty(sample.getResults()) ? 0 : sample.getResults().size()));
        SampleTranslator translator = new SampleTranslator();
        List<TargetInstance> list = sample.getResults().stream().map(s -> (TargetInstance) translator.translate(s, dataStage, liveMode)).collect(Collectors.toList());
        TargetInstances result = new TargetInstances();
        result.setSearchableInstances(list);
        return result;
    }

    public Sample createSampleFromKGv2(DataStage dataStage, boolean liveMode, String query, String id, String authorization) {
        SampleV1 sample = kgV2.fetchInstance(SampleV1.class, query, id, authorization, dataStage);
        SampleTranslator translator = new SampleTranslator();
        return translator.translate(sample, dataStage, liveMode);
    }

    private static class ResultOfKGV2SubjectV1 extends ResultOfKGv2<SubjectV1> {
    }

    public TargetInstances createSubjects(DataStage dataStage, boolean liveMode, String legacyAuthorization) {
        String query = "query/minds/experiment/subject/v1.0.0/search";
        logger.info("Starting to query subjects for v1");
        ResultOfKGV2SubjectV1 subject = kgV2.executeQueryForIndexing(ResultOfKGV2SubjectV1.class, query, legacyAuthorization, dataStage);
        logger.info(String.format("Queried %s subjects for v1", CollectionUtils.isEmpty(subject.getResults()) ? 0 : subject.getResults().size()));
        SubjectTranslator translator = new SubjectTranslator();
        List<TargetInstance> list = subject.getResults().stream().map(s -> (TargetInstance) translator.translate(s, dataStage, liveMode)).collect(Collectors.toList());
        TargetInstances result = new TargetInstances();
        result.setSearchableInstances(list);
        return result;
    }

    public Subject createSubjectFromKGv2(DataStage dataStage, boolean liveMode, String query, String id, String authorization) {
        SubjectV1 subject = kgV2.fetchInstance(SubjectV1.class, query, id, authorization, dataStage);
        SubjectTranslator translator = new SubjectTranslator();
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
        Map instance = kgV3.fetchInstanceForLive(id, dataStage);
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
                case Constants.SOURCE_MODEL_SUBJECT:
                case Constants.SOURCE_MODEL_SAMPLE:
                    //TODO to be implemented
                    return null;
            }
        }
        throw new HttpClientErrorException(HttpStatus.NOT_FOUND, String.format("Type %s is not recognized as a valid search resource!", type));
    }

    public TargetInstances createInstancesCombined(Class<?> clazz, DataStage dataStage, boolean liveMode, String legacyAuthorization) {
        if (clazz == DatasetVersion.class) {
            return this.createDatasetVersions(dataStage, liveMode, legacyAuthorization);
        }
        if (clazz == Dataset.class) {
            return this.createDatasets(dataStage, liveMode);
        }
        if (clazz == Contributor.class) {
            return this.createContributors(dataStage, liveMode, legacyAuthorization);
        }
        if (clazz == Project.class) {
            return this.createProjects(dataStage, liveMode, legacyAuthorization);
        }
        if (clazz == Subject.class) {
            return this.createSubjects(dataStage, liveMode, legacyAuthorization);
        }
        if (clazz == Sample.class) {
            return this.createSamples(dataStage, liveMode, legacyAuthorization);
        }
        if (clazz == ModelVersion.class) {
            return this.createModelVersions(dataStage, liveMode, legacyAuthorization);
        }
        if (clazz == Model.class) {
            return this.createModels(dataStage, liveMode);
        }
        if (clazz == SoftwareVersion.class) {
            return this.createSoftwareVersions(dataStage, liveMode, legacyAuthorization);
        }
        if (clazz == Software.class) {
            return this.createSoftwares(dataStage, liveMode);
        }
        return new TargetInstances();
    }
}
