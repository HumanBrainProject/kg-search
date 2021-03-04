package eu.ebrains.kg.search.controller.translators;


import eu.ebrains.kg.search.constants.Queries;
import eu.ebrains.kg.search.controller.Constants;
import eu.ebrains.kg.search.controller.kg.KGv2;
import eu.ebrains.kg.search.controller.kg.KGv3;
import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.DatasetSources;
import eu.ebrains.kg.search.model.source.PersonSources;
import eu.ebrains.kg.search.model.source.ResultOfKGv2;
import eu.ebrains.kg.search.model.source.ResultOfKGv3;
import eu.ebrains.kg.search.model.source.openMINDSv1.*;
import eu.ebrains.kg.search.model.source.openMINDSv2.ModelV2;
import eu.ebrains.kg.search.model.source.openMINDSv2.PersonV2;
import eu.ebrains.kg.search.model.source.openMINDSv2.SoftwareV2;
import eu.ebrains.kg.search.model.source.openMINDSv3.DatasetV3;
import eu.ebrains.kg.search.model.source.openMINDSv3.DatasetVersionV3;
import eu.ebrains.kg.search.model.target.elasticsearch.TargetInstance;
import eu.ebrains.kg.search.model.target.elasticsearch.TargetInstances;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.*;
import eu.ebrains.kg.search.utils.ESHelper;
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

    private static class ResultOfKGV2PersonV1 extends ResultOfKGv2<PersonV1> {
    }

    private static class ResultOfKGV2PersonV2 extends ResultOfKGv2<PersonV2> {
    }

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
//        ResultOfKGv3<PersonV3> personsFromV3 = kgV3.fetchInstances(PersonV3.class); //TODO v3

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

        //TODO add v3

        ContributorTranslator translator = new ContributorTranslator();
        List<TargetInstance> list = personSources.stream().map(p -> (TargetInstance) translator.translate(p, dataStage, liveMode)).collect(Collectors.toList());
        TargetInstances result = new TargetInstances();
        result.setSearchableInstances(list);
        result.setAllInstances(list);
        return result;
    }

    public Contributor createContributorFromKGv2(DataStage dataStage, boolean liveMode, String query, String id, String authorization) {
        PersonV2 personV2 = kgV2.fetchInstance(PersonV2.class, query, id, authorization, dataStage);
        ContributorTranslator translator = new ContributorTranslator();
        PersonSources personSource = new PersonSources();
        personSource.setPersonV2(personV2);
        return translator.translate(personSource, dataStage, liveMode);
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
        ContributorTranslator translator = new ContributorTranslator();
        return translator.translate(personSource, dataStage, liveMode);
    }

    private static class ResultOfKGV2SoftwareV2 extends ResultOfKGv2<SoftwareV2> {
    }

    public TargetInstances createSoftwares(DataStage dataStage, boolean liveMode, String legacyAuthorization) {
        String query = "query/softwarecatalog/software/softwareproject/v1.0.0/search";
        logger.info("Starting to query software for v1");
        ResultOfKGV2SoftwareV2 software = kgV2.executeQueryForIndexing(ResultOfKGV2SoftwareV2.class, query, legacyAuthorization, dataStage);
        logger.info(String.format("Queried %s software for v1", CollectionUtils.isEmpty(software.getResults()) ? 0 : software.getResults().size()));
        SoftwareTranslator translator = new SoftwareTranslator();
        List<TargetInstance> list = software.getResults().stream().map(s -> (TargetInstance) translator.translate(s, dataStage, liveMode)).collect(Collectors.toList());
        TargetInstances result = new TargetInstances();
        result.setSearchableInstances(list);
        result.setAllInstances(list);
        return result;
    }

    public Software createSoftwareFromKGv2(DataStage dataStage, boolean liveMode, String query, String id, String authorization) {
        SoftwareV2 software = kgV2.fetchInstance(SoftwareV2.class, query, id, authorization, dataStage);
        SoftwareTranslator translator = new SoftwareTranslator();
        return translator.translate(software, dataStage, liveMode);
    }

    private static class ResultOfKGV2DatasetV1 extends ResultOfKGv2<DatasetV1> {
    }

    private static class ResultOfKGV3DatasetV3 extends ResultOfKGv3<DatasetV3> {
    }

    public TargetInstances createDatasets(DataStage dataStage, boolean liveMode, String legacyAuthorization) {
        String query = "query/minds/core/dataset/v1.0.0/search";
        logger.info("Starting to query datasets for v1");
        ResultOfKGV2DatasetV1 datasetV1 = kgV2.executeQueryForIndexing(ResultOfKGV2DatasetV1.class, query, legacyAuthorization, dataStage);
        logger.info(String.format("Queried %s datasets for v1", CollectionUtils.isEmpty(datasetV1.getResults()) ? 0 : datasetV1.getResults().size()));
        logger.info("Starting to query datasets for v3");
        ResultOfKGv3<DatasetV3> datasetV3 = kgV3.executeQueryForIndexing(ResultOfKGV3DatasetV3.class, Queries.DATASET_QUERY_ID, dataStage);
        logger.info(String.format("Queried %s datasets for v3", CollectionUtils.isEmpty(datasetV3.getData()) ? 0 : datasetV3.getData().size()));
        List<DatasetSources> datasetSources = new ArrayList<>();
        datasetV1.getResults().forEach(p -> {
            DatasetSources source = new DatasetSources();
            datasetSources.add(source);
            source.setDatasetV1(p);
        });

        Map<String, DatasetSources> datasetSourcesByV1Identifier = datasetSources.stream().collect(Collectors.toMap(k -> k.getDatasetV1().getIdentifier(), v -> v));

        datasetV3.getData().forEach(d -> {
            String id = d.getIdentifier().stream().reduce(null, (found, i) -> {
                if (found != null) {
                    String uuid = ESHelper.getUUID(i);
                    DatasetSources source = datasetSourcesByV1Identifier.get(uuid);
                    if (source != null) {
                        source.setDatasetV3(d);
                        return uuid;
                    }
                }
                return null;
            });
            if (id == null) {
                DatasetSources source = new DatasetSources();
                source.setDatasetV3(d);
                datasetSources.add(source);
            }
        });

        DatasetTranslator translator = new DatasetTranslator();
        VersionedDatasetTranslator versionedDatasetTranslator = new VersionedDatasetTranslator();
        TargetInstances targetInstances = new TargetInstances();
        datasetSources.forEach(d -> {
            if (d.getDatasetV3() != null) {
                targetInstances.addInstance(versionedDatasetTranslator.translate(d.getDatasetV3(), dataStage, liveMode, null), true);
                List<DatasetVersionV3> datasetVersions = d.getDatasetV3().getDatasetVersions();
                for (DatasetVersionV3 datasetVersion : datasetVersions) {
                    String versionIdentifiers = datasetVersion.getVersionIdentifier();
                    Dataset translate = versionedDatasetTranslator.translate(d.getDatasetV3(), dataStage, liveMode, versionIdentifiers);
                    targetInstances.addInstance(translate, false);
                }
            } else {
                targetInstances.addInstance(translator.translate(d.getDatasetV1(), dataStage, liveMode), true);
            }
        });
        return targetInstances;
    }

    public Dataset createDatasetFromKGv2(DataStage dataStage, boolean liveMode, String query, String id, String authorization) {
        DatasetV1 datasetV1 = kgV2.fetchInstance(DatasetV1.class, query, id, authorization, dataStage);
        DatasetTranslator translator = new DatasetTranslator();
        return translator.translate(datasetV1, dataStage, liveMode);
    }

    public Dataset createDatasetV3(DataStage dataStage, boolean liveMode, String id) {
        ResultOfKGV3DatasetV3 resultOfKGV3DatasetV3 = kgV3.executeQueryForLive(ResultOfKGV3DatasetV3.class, Queries.DATASET_QUERY_ID, id, dataStage);
        DatasetV3 datasetV3 = resultOfKGV3DatasetV3.getData().stream().findFirst().orElse(null);
        if (datasetV3 != null) {
            VersionedDatasetTranslator translator = new VersionedDatasetTranslator();
            return translator.translate(datasetV3, dataStage, liveMode, null);
        }
        return null;
    }

    private static class ResultOfKGV2ModelV2 extends ResultOfKGv2<ModelV2> {
    }

    public TargetInstances createModels(DataStage dataStage, boolean liveMode, String legacyAuthorization) {
        String query = "query/uniminds/core/modelinstance/v1.0.0/search";
        logger.info("Starting to query models for v1");
        ResultOfKGV2ModelV2 model = kgV2.executeQueryForIndexing(ResultOfKGV2ModelV2.class, query, legacyAuthorization, dataStage);
        logger.info(String.format("Queried %s models for v1", CollectionUtils.isEmpty(model.getResults()) ? 0 : model.getResults().size()));
        ModelTranslator translator = new ModelTranslator();
        List<TargetInstance> list = model.getResults().stream().map(m -> (TargetInstance) translator.translate(m, dataStage, liveMode)).collect(Collectors.toList());
        TargetInstances result = new TargetInstances();
        result.setSearchableInstances(list);
        result.setAllInstances(list);
        return result;
    }

    public Model createModelFromKGv2(DataStage dataStage, boolean liveMode, String query, String id, String authorization) {
        ModelV2 model = kgV2.fetchInstance(ModelV2.class, query, id, authorization, dataStage);
        ModelTranslator translator = new ModelTranslator();
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
        result.setAllInstances(list);
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
        result.setAllInstances(list);
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
        result.setAllInstances(list);
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
                return this.createDatasetFromKGv2(dataStage, liveMode, query, id, legacyAuthorization);
            case "minds/core/person/v1.0.0":
                return this.createContributorFromKGv2(dataStage, liveMode, Arrays.asList(query, "query/uniminds/core/person/v1.0.0/search"), id, legacyAuthorization);
            case "uniminds/core/person/v1.0.0":
                return this.createContributorFromKGv2(dataStage, liveMode, query, id, legacyAuthorization);
            case "minds/core/placomponent/v1.0.0":
                return this.createProjectFromKGv2(dataStage, liveMode, query, id, legacyAuthorization);
            case "uniminds/core/modelinstance/v1.0.0":
                return this.createModelFromKGv2(dataStage, liveMode, query, id, legacyAuthorization);
            case "softwarecatalog/software/softwareproject/v1.0.0":
                return this.createSoftwareFromKGv2(dataStage, liveMode, query, id, legacyAuthorization);
            case "minds/experiment/subject/v1.0.0":
                return this.createSubjectFromKGv2(dataStage, liveMode, query, id, legacyAuthorization);
            case "minds/experiment/sample/v1.0.0":
                return this.createSampleFromKGv2(dataStage, liveMode, query, id, legacyAuthorization);
        }
        throw new HttpClientErrorException(HttpStatus.NOT_FOUND, String.format("Type %s is not recognized as a valid search resource!", type));
    }

    public TargetInstance createLiveInstanceFromKGv3(DataStage dataStage, boolean liveMode, String id) {
        Map instance = kgV3.fetchInstanceForLive(id, dataStage);
        Map data = (Map) instance.get("data");
        List<String> types = (List<String>) data.get("@type");
        String type = types.stream().filter(Constants.TYPES_FOR_LIVE::contains).collect(Collectors.toList()).stream().findFirst().orElse(null);
        if (type != null) {
            switch (type) {
                case "https://openminds.ebrains.eu/core/Dataset":
                    return this.createDatasetV3(dataStage, liveMode, id);
                case "https://openminds.ebrains.eu/core/Person":
                case "https://openminds.ebrains.eu/core/Project":
                case "https://openminds.ebrains.eu/core/Model":
                case "https://openminds.ebrains.eu/core/Software":
                case "https://openminds.ebrains.eu/core/Subject":
                case "https://openminds.ebrains.eu/core/Sample":
                    //TODO to be implemented
                    return null;
            }
        }
        throw new HttpClientErrorException(HttpStatus.NOT_FOUND, String.format("Type %s is not recognized as a valid search resource!", type));
    }

    public TargetInstances createInstancesCombined(DataStage dataStage, boolean liveMode, String type, String legacyAuthorization) {
        switch (type) {
            case "Dataset":
                return this.createDatasets(dataStage, liveMode, legacyAuthorization);
            case "Contributor":
                return this.createContributors(dataStage, liveMode, legacyAuthorization);
            case "Project":
                return this.createProjects(dataStage, liveMode, legacyAuthorization);
            case "Model":
                return this.createModels(dataStage, liveMode, legacyAuthorization);
            case "Subject":
                return this.createSubjects(dataStage, liveMode, legacyAuthorization);
            case "Sample":
                return this.createSamples(dataStage, liveMode, legacyAuthorization);
            case "Software":
                return this.createSoftwares(dataStage, liveMode, legacyAuthorization);
        }
        return new TargetInstances();
    }

}
