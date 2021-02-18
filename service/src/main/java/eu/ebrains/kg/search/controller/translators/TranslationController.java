package eu.ebrains.kg.search.controller.translators;


import eu.ebrains.kg.search.constants.Queries;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class TranslationController {
    private final KGv2 kgV2;
    private final KGv3 kgV3;

    public TranslationController(KGv2 kgV2, KGv3 kgV3) {
        this.kgV2 = kgV2;
        this.kgV3 = kgV3;
    }

    private static class ResultOfKGV2PersonV1 extends ResultOfKGv2<PersonV1> {}
    private static class ResultOfKGV2PersonV2 extends ResultOfKGv2<PersonV2> {}

    private List<TargetInstance> createContributors(DataStage dataStage, boolean liveMode, String authorization, String legacyAuthorization) {
        String queryForV1 = "query/minds/core/person/v1.0.0/search";
        String queryForV2 = "query/uniminds/core/person/v1.0.0/search";
        ResultOfKGV2PersonV1 personsFromV1 = kgV2.fetchInstances(ResultOfKGV2PersonV1.class, queryForV1, legacyAuthorization, dataStage);
        ResultOfKGv2<PersonV2> personsFromV2 = kgV2.fetchInstances(ResultOfKGV2PersonV2.class, queryForV2, legacyAuthorization, dataStage);
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
        return personSources.stream().map(p -> (TargetInstance) translator.translate(p, dataStage, liveMode)).collect(Collectors.toList());
    }

    public Contributor createContributor(DataStage dataStage, boolean liveMode, String query, String id, String authorization) {
        PersonV2 personV2 = kgV2.fetchInstance(PersonV2.class, query, id, authorization, dataStage);
        ContributorTranslator translator = new ContributorTranslator();
        PersonSources personSource = new PersonSources();
        personSource.setPersonV2(personV2);
        return translator.translate(personSource, dataStage, liveMode);
    }

    private static class PersonV2Result extends ResultOfKGv2<PersonV2> {
    }

    public Contributor createContributorForV1(DataStage dataStage, boolean liveMode, List<String> queries, String id, String authorization) {
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

    private static class ResultOfKGV2SoftwareV2 extends ResultOfKGv2<SoftwareV2> {}

    public List<TargetInstance> createSoftwares(DataStage dataStage, boolean liveMode, String authorization, String legacyAuthorization) {
        String query = "query/softwarecatalog/software/softwareproject/v1.0.0/search";
        ResultOfKGV2SoftwareV2 software = kgV2.fetchInstances(ResultOfKGV2SoftwareV2.class, query, legacyAuthorization, dataStage);
        SoftwareTranslator translator = new SoftwareTranslator();
        return software.getResults().stream().map(s -> (TargetInstance) translator.translate(s, dataStage, liveMode)).collect(Collectors.toList());
    }

    public Software createSoftware(DataStage dataStage, boolean liveMode, String query, String id, String authorization) {
        SoftwareV2 software = kgV2.fetchInstance(SoftwareV2.class, query, id, authorization, dataStage);
        SoftwareTranslator translator = new SoftwareTranslator();
        return translator.translate(software, dataStage, liveMode);
    }

    private static class ResultOfKGV2DatasetV1 extends ResultOfKGv2<DatasetV1> {}
    private static class ResultOfKGV3DatasetV3 extends ResultOfKGv3<DatasetV3> {}

    public TargetInstances createDatasets(DataStage dataStage, boolean liveMode, String authorization, String legacyAuthorization) {
        String query = "query/minds/core/dataset/v1.0.0/search";
        ResultOfKGV2DatasetV1 datasetV1 = kgV2.fetchInstances(ResultOfKGV2DatasetV1.class, query, legacyAuthorization, dataStage);
        ResultOfKGv3<DatasetV3> datasetV3 = kgV3.fetchInstances(ResultOfKGV3DatasetV3.class, Queries.DATASET_ID, authorization, dataStage);

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
                    String[] splitId = i.split("/");
                    String lookupId = splitId[splitId.length - 1];
                    DatasetSources source = datasetSourcesByV1Identifier.get(lookupId);
                    if (source != null) {
                        source.setDatasetV3(d);
                        return lookupId;
                    }
                }
                return null;
            });
            if(id == null) {
                DatasetSources source = new DatasetSources();
                source.setDatasetV3(d);
                datasetSources.add(source);
            }
        });

        DatasetTranslator translator = new DatasetTranslator();
        TargetInstances targetInstances = new TargetInstances();
        datasetSources.forEach(d -> {
            if(d.getDatasetV3() != null) {
                List<String> versionIdentifiers = sortDatasetVersions(d.getDatasetV3().getHasVersion());
                for(int i = 0; i < versionIdentifiers.size(); i++) {
                    targetInstances.addInstance(translator.translate(d.getDatasetV3(), dataStage, liveMode, versionIdentifiers.get(i)), i==0);
                }
            } else {
                targetInstances.addInstance(translator.translate(d.getDatasetV1(), dataStage, liveMode), true);
            }
        });
        return targetInstances;
    }

    private List<String> sortDatasetVersions(List<DatasetVersionV3> datasetVersions) {
        LinkedList<String> result = new LinkedList<>();
        datasetVersions.forEach(dv -> {
            if(result.isEmpty()) {
                result.add(dv.getVersionIdentifier());
            } else {
                String previousVersionIdentifier = dv.getPreviousVersionIdentifier();
                if(previousVersionIdentifier != null) {
                    int i = result.indexOf(previousVersionIdentifier);
                    if(i == -1) {
                        result.addLast(dv.getVersionIdentifier());
                    } else {
                        result.add(i-1, dv.getVersionIdentifier());
                    }
                } else {
                    result.addFirst(dv.getVersionIdentifier());
                }
            }
        });
        return result;
    }

    public Dataset createDataset(DataStage dataStage, boolean liveMode, String query, String id, String authorization) {
        DatasetV1 datasetV1 = kgV2.fetchInstance(DatasetV1.class, query, id, authorization, dataStage);
        DatasetTranslator translator = new DatasetTranslator();
        return translator.translate(datasetV1, dataStage, liveMode);
    }

    public Dataset createDataset(DataStage dataStage, boolean liveMode, String id, String authorization) {
        //TODO: Remove the comments. It is just for test and will be removed.
//        ResultOfKGV3DatasetV3 datasetV31 = kgV3.fetchInstance(ResultOfKGV3DatasetV3.class, Queries.DATASET_ID, id, authorization, dataStage);
//        DatasetV3 datasetV32 = datasetV31.getData().get(0);
//        String latestDatasetVersionIdentifier = getLatestDatasetVersionIdentifier(datasetV32.getHasVersion());
//        DatasetTranslator translator = new DatasetTranslator();
//        return translator.translate(datasetV32, dataStage, liveMode, latestDatasetVersionIdentifier);

        DatasetV3 datasetV3 = kgV3.fetchInstance(DatasetV3.class, Queries.DATASET_ID, id, authorization, dataStage);
        String latestDatasetVersionIdentifier = getLatestDatasetVersionIdentifier(datasetV3.getHasVersion());
        DatasetTranslator translator = new DatasetTranslator();
        return translator.translate(datasetV3, dataStage, liveMode, latestDatasetVersionIdentifier);
    }

    private String getLatestDatasetVersionIdentifier(List<DatasetVersionV3> datasetVersions){
        List<String> versionIdentifiers = sortDatasetVersions(datasetVersions);
        return versionIdentifiers.stream().findFirst().orElse(null);
    }

    private static class ResultOfKGV2ModelV2 extends ResultOfKGv2<ModelV2> {}
    public List<TargetInstance> createModels(DataStage dataStage, boolean liveMode, String authorization, String legacyAuthorization) {
        String query = "query/uniminds/core/modelinstance/v1.0.0/search";
        ResultOfKGV2ModelV2 model = kgV2.fetchInstances(ResultOfKGV2ModelV2.class, query, legacyAuthorization, dataStage);
        ModelTranslator translator = new ModelTranslator();
        return model.getResults().stream().map(m -> (TargetInstance) translator.translate(m, dataStage, liveMode)).collect(Collectors.toList());
    }

    public Model createModel(DataStage dataStage, boolean liveMode, String query, String id, String authorization) {
        ModelV2 model = kgV2.fetchInstance(ModelV2.class, query, id, authorization, dataStage);
        ModelTranslator translator = new ModelTranslator();
        return translator.translate(model, dataStage, liveMode);
    }

    private static class ResultOfKGV2ProjectV1 extends ResultOfKGv2<ProjectV1> {}
    public List<TargetInstance> createProjects(DataStage dataStage, boolean liveMode, String authorization, String legacyAuthorization) {
        String query = "query/minds/core/placomponent/v1.0.0/search";
        ResultOfKGV2ProjectV1 project = kgV2.fetchInstances(ResultOfKGV2ProjectV1.class, query, legacyAuthorization, dataStage);
        ProjectTranslator translator = new ProjectTranslator();
        return project.getResults().stream().map(p -> (TargetInstance) translator.translate(p, dataStage, liveMode)).collect(Collectors.toList());
    }

    public Project createProject(DataStage dataStage, boolean liveMode, String query, String id, String authorization) {
        ProjectV1 project = kgV2.fetchInstance(ProjectV1.class, query, id, authorization, dataStage);
        ProjectTranslator translator = new ProjectTranslator();
        return translator.translate(project, dataStage, liveMode);
    }

    private static class ResultOfKGV2SampleV1 extends ResultOfKGv2<SampleV1> {}
    public List<TargetInstance> createSamples(DataStage dataStage, boolean liveMode, String authorization, String legacyAuthorization) {
        String query = "query/minds/experiment/sample/v1.0.0/search";
        ResultOfKGV2SampleV1 sample = kgV2.fetchInstances(ResultOfKGV2SampleV1.class, query, legacyAuthorization, dataStage);
        SampleTranslator translator = new SampleTranslator();
        return sample.getResults().stream().map(s -> (TargetInstance) translator.translate(s, dataStage, liveMode)).collect(Collectors.toList());
    }

    public Sample createSample(DataStage dataStage, boolean liveMode, String query, String id, String authorization) {
        SampleV1 sample = kgV2.fetchInstance(SampleV1.class, query, id, authorization, dataStage);
        SampleTranslator translator = new SampleTranslator();
        return translator.translate(sample, dataStage, liveMode);
    }

    private static class ResultOfKGV2SubjectV1 extends ResultOfKGv2<SubjectV1> {}
    public List<TargetInstance> createSubjects(DataStage dataStage, boolean liveMode, String authorization, String legacyAuthorization) {
        String query = "query/minds/experiment/subject/v1.0.0/search";
        ResultOfKGV2SubjectV1 subject = kgV2.fetchInstances(ResultOfKGV2SubjectV1.class, query, legacyAuthorization, dataStage);
        SubjectTranslator translator = new SubjectTranslator();
        return subject.getResults().stream().map(s -> (TargetInstance) translator.translate(s, dataStage, liveMode)).collect(Collectors.toList());
    }

    public Subject createSubject(DataStage dataStage, boolean liveMode, String query, String id, String authorization) {
        SubjectV1 subject = kgV2.fetchInstance(SubjectV1.class, query, id, authorization, dataStage);
        SubjectTranslator translator = new SubjectTranslator();
        return translator.translate(subject, dataStage, liveMode);
    }

    public TargetInstance createInstance(DataStage dataStage, boolean liveMode, String org, String domain, String schema, String version, String id, String legacyAuthorization) {
        String type = String.format("%s/%s/%s/%s", org, domain, schema, version);
        String query = String.format("query/%s/search", type);
        switch (type) {
            case "minds/core/dataset/v1.0.0":
                return this.createDataset(dataStage, liveMode, query, id, legacyAuthorization);
            case "minds/core/person/v1.0.0":
                List<String> queries = Arrays.asList(query, "query/uniminds/core/person/v1.0.0/search");
                return this.createContributorForV1(dataStage, liveMode, queries, id, legacyAuthorization);
            case "uniminds/core/person/v1.0.0":
                return this.createContributor(dataStage, liveMode, query, id, legacyAuthorization);
            case "minds/core/placomponent/v1.0.0":
                return this.createProject(dataStage, liveMode, query, id, legacyAuthorization);
            case "uniminds/core/modelinstance/v1.0.0":
                return this.createModel(dataStage, liveMode, query, id, legacyAuthorization);
            case "softwarecatalog/software/softwareproject/v1.0.0":
                return this.createSoftware(dataStage, liveMode, query, id, legacyAuthorization);
            case "minds/experiment/subject/v1.0.0":
                return this.createSubject(dataStage, liveMode, query, id, legacyAuthorization);
            case "minds/experiment/sample/v1.0.0":
                return this.createSample(dataStage, liveMode, query, id, legacyAuthorization);
        }
        throw new HttpClientErrorException(HttpStatus.NOT_FOUND, String.format("Type %s is not recognized as a valid search resource!", type));
    }

    public TargetInstance createInstance(DataStage dataStage, boolean liveMode, String id, String type, String authorization) {
        switch (type) {
            case "https://openminds.ebrains.eu/core/Dataset":
                return this.createDataset(dataStage, liveMode, id, authorization);
            case "https://openminds.ebrains.eu/core/Person":
                return this.createContributor(dataStage, liveMode, Queries.CONTRIBUTOR_ID, id, authorization);
            case "https://openminds.ebrains.eu/core/Project":
                return this.createProject(dataStage, liveMode, Queries.PROJECT_ID, id, authorization);
            case "https://openminds.ebrains.eu/core/Model":
                return this.createModel(dataStage, liveMode, Queries.MODEL_ID, id, authorization);
            case "https://openminds.ebrains.eu/core/Software":
                return this.createSoftware(dataStage, liveMode, Queries.SOFTWARE_ID, id, authorization);
            case "https://openminds.ebrains.eu/core/Subject":
                return this.createSubject(dataStage, liveMode, Queries.SUBJECT_ID, id, authorization);
            case "https://openminds.ebrains.eu/core/Sample":
                return this.createSample(dataStage, liveMode, Queries.SAMPLE_ID, id, authorization);
        }
        throw new HttpClientErrorException(HttpStatus.NOT_FOUND, String.format("Type %s is not recognized as a valid search resource!", type));
    }

    public TargetInstances createInstances(DataStage dataStage, boolean liveMode, String type, String authorization, String legacyAuthorization) {
        switch (type) {
            case "Dataset":
                return this.createDatasets(dataStage, liveMode, authorization, legacyAuthorization);
//            case "Contributor":
//                return this.createContributors(dataStage, liveMode, authorization, legacyAuthorization);
//            case "Project":
//                return this.createProjects(dataStage, liveMode, authorization, legacyAuthorization);
//            case "Model":
//                return this.createModels(dataStage, liveMode, authorization, legacyAuthorization);
//            case "Subject":
//                return this.createSubjects(dataStage, liveMode, authorization, legacyAuthorization);
//            case "Sample":
//                return this.createSamples(dataStage, liveMode, authorization, legacyAuthorization);
//            case "Software":
//                return this.createSoftwares(dataStage, liveMode, authorization, legacyAuthorization);
        }
        return new TargetInstances();
    }

}
