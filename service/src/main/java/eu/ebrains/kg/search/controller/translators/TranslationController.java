package eu.ebrains.kg.search.controller.translators;


import eu.ebrains.kg.search.controller.kg.KGv2;
import eu.ebrains.kg.search.controller.kg.KGv3;
import eu.ebrains.kg.search.model.DatabaseScope;
import eu.ebrains.kg.search.model.source.PersonSources;
import eu.ebrains.kg.search.model.source.ResultOfKGv2;
import eu.ebrains.kg.search.model.source.ResultOfKGv3;
import eu.ebrains.kg.search.model.source.openMINDSv1.*;
import eu.ebrains.kg.search.model.source.openMINDSv2.ModelV2;
import eu.ebrains.kg.search.model.source.openMINDSv2.PersonV2;
import eu.ebrains.kg.search.model.source.openMINDSv2.SoftwareV2;
import eu.ebrains.kg.search.model.source.openMINDSv3.PersonV3;
import eu.ebrains.kg.search.model.target.elasticsearch.TargetInstance;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Component
public class TranslationController {
    private final KGv2 kgV2;
    private final KGv3 kgV3;

    public TranslationController(KGv2 kgV2, KGv3 kgV3) {
        this.kgV2 = kgV2;
        this.kgV3 = kgV3;
    }

    private List<TargetInstance> createContributors(DatabaseScope databaseScope, boolean liveMode, String authorization) {
        String queryForV1 = "query/minds/core/person/v1.0.0/search";
        String queryForV2 = "query/uniminds/core/person/v1.0.0/search";
        ResultOfKGv2<PersonV1> personsFromV1 = kgV2.fetchInstances(PersonV1.class, queryForV1, authorization, databaseScope);
        ResultOfKGv2<PersonV2> personsFromV2 = kgV2.fetchInstances(PersonV2.class, queryForV2, authorization, databaseScope);
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
        return personSources.stream().map(p -> (TargetInstance) translator.translate(p, databaseScope, liveMode)).collect(Collectors.toList());
    }

    public Contributor createContributor(DatabaseScope databaseScope, boolean liveMode, String query, String id, String authorization) {
        PersonV2 personV2 = kgV2.fetchInstance(PersonV2.class, query, id, authorization, databaseScope);
        ContributorTranslator translator = new ContributorTranslator();
        PersonSources personSource = new PersonSources();
        personSource.setPersonV2(personV2);
        return translator.translate(personSource, databaseScope, liveMode);
    }

    private static class PersonV2Result extends ResultOfKGv2<PersonV2> {
    }

    public Contributor createContributorForV1(DatabaseScope databaseScope, boolean liveMode, List<String> queries, String id, String authorization) {
        String personV1Query = queries.get(0);
        String personV2Query = queries.get(1);
        PersonSources personSource = new PersonSources();
        PersonV1 personV1 = kgV2.fetchInstance(PersonV1.class, personV1Query, id, authorization, databaseScope);
        personSource.setPersonV1(personV1);
        PersonV2Result personV2Result = kgV2.fetchInstanceByIdentifier(PersonV2Result.class, personV2Query, personV1.getIdentifier(), authorization, databaseScope);
        if (personV2Result != null && !CollectionUtils.isEmpty(personV2Result.getResults())) {
            PersonV2 personV2 = personV2Result.getResults().get(0);
            personSource.setPersonV2(personV2);
        }
        ContributorTranslator translator = new ContributorTranslator();
        return translator.translate(personSource, databaseScope, liveMode);
    }

    public List<TargetInstance> createSoftwares(DatabaseScope databaseScope, boolean liveMode, String authorization) {
        String query = "query/softwarecatalog/software/softwareproject/v1.0.0/search";
        ResultOfKGv2<SoftwareV2> software = kgV2.fetchInstances(SoftwareV2.class, query, authorization, databaseScope);
        SoftwareTranslator translator = new SoftwareTranslator();
        return software.getResults().stream().map(s -> (TargetInstance) translator.translate(s, databaseScope, liveMode)).collect(Collectors.toList());
    }

    public Software createSoftware(DatabaseScope databaseScope, boolean liveMode, String query, String id, String authorization) {
        SoftwareV2 software = kgV2.fetchInstance(SoftwareV2.class, query, id, authorization, databaseScope);
        SoftwareTranslator translator = new SoftwareTranslator();
        return translator.translate(software, databaseScope, liveMode);
    }

    public List<TargetInstance> createDatasets(DatabaseScope databaseScope, boolean liveMode, String authorization) {
        String query = "query/minds/core/dataset/v1.0.0/search";
        ResultOfKGv2<DatasetV1> dataset = kgV2.fetchInstances(DatasetV1.class, query, authorization, databaseScope);
        DatasetTranslator translator = new DatasetTranslator();
        return dataset.getResults().stream().map(d -> (TargetInstance) translator.translate(d, databaseScope, liveMode)).collect(Collectors.toList());
    }

    public Dataset createDataset(DatabaseScope databaseScope, boolean liveMode, String query, String id, String authorization) {
        DatasetV1 dataset = kgV2.fetchInstance(DatasetV1.class, query, id, authorization, databaseScope);
        DatasetTranslator translator = new DatasetTranslator();
        return translator.translate(dataset, databaseScope, liveMode);
    }

    public List<TargetInstance> createModels(DatabaseScope databaseScope, boolean liveMode, String authorization) {
        String query = "query/uniminds/core/modelinstance/v1.0.0/search";
        ResultOfKGv2<ModelV2> model = kgV2.fetchInstances(ModelV2.class, query, authorization, databaseScope);
        ModelTranslator translator = new ModelTranslator();
        return model.getResults().stream().map(m -> (TargetInstance) translator.translate(m, databaseScope, liveMode)).collect(Collectors.toList());
    }

    public Model createModel(DatabaseScope databaseScope, boolean liveMode, String query, String id, String authorization) {
        ModelV2 model = kgV2.fetchInstance(ModelV2.class, query, id, authorization, databaseScope);
        ModelTranslator translator = new ModelTranslator();
        return translator.translate(model, databaseScope, liveMode);
    }

    public List<TargetInstance> createProjects(DatabaseScope databaseScope, boolean liveMode, String authorization) {
        String query = "query/minds/core/placomponent/v1.0.0/search";
        ResultOfKGv2<ProjectV1> project = kgV2.fetchInstances(ProjectV1.class, query, authorization, databaseScope);
        ProjectTranslator translator = new ProjectTranslator();
        return project.getResults().stream().map(p -> (TargetInstance) translator.translate(p, databaseScope, liveMode)).collect(Collectors.toList());
    }

    public Project createProject(DatabaseScope databaseScope, boolean liveMode, String query, String id, String authorization) {
        ProjectV1 project = kgV2.fetchInstance(ProjectV1.class, query, id, authorization, databaseScope);
        ProjectTranslator translator = new ProjectTranslator();
        return translator.translate(project, databaseScope, liveMode);
    }

    public List<TargetInstance> createSamples(DatabaseScope databaseScope, boolean liveMode, String authorization) {
        String query = "query/minds/experiment/sample/v1.0.0/search";
        ResultOfKGv2<SampleV1> sample = kgV2.fetchInstances(SampleV1.class, query, authorization, databaseScope);
        SampleTranslator translator = new SampleTranslator();
        return sample.getResults().stream().map(s -> (TargetInstance) translator.translate(s, databaseScope, liveMode)).collect(Collectors.toList());
    }

    public Sample createSample(DatabaseScope databaseScope, boolean liveMode, String query, String id, String authorization) {
        SampleV1 sample = kgV2.fetchInstance(SampleV1.class, query, id, authorization, databaseScope);
        SampleTranslator translator = new SampleTranslator();
        return translator.translate(sample, databaseScope, liveMode);
    }

    public List<TargetInstance> createSubjects(DatabaseScope databaseScope, boolean liveMode, String authorization) {
        String query = "query/minds/experiment/subject/v1.0.0/search";
        ResultOfKGv2<SubjectV1> subject = kgV2.fetchInstances(SubjectV1.class, query, authorization, databaseScope);
        SubjectTranslator translator = new SubjectTranslator();
        return subject.getResults().stream().map(s -> (TargetInstance) translator.translate(s, databaseScope, liveMode)).collect(Collectors.toList());
    }

    public Subject createSubject(DatabaseScope databaseScope, boolean liveMode, String query, String id, String authorization) {
        SubjectV1 subject = kgV2.fetchInstance(SubjectV1.class, query, id, authorization, databaseScope);
        SubjectTranslator translator = new SubjectTranslator();
        return translator.translate(subject, databaseScope, liveMode);
    }

    public TargetInstance createInstance(DatabaseScope databaseScope, boolean liveMode, String org, String domain, String schema, String version, String id, String authorization) {
        String type = String.format("%s/%s/%s/%s", org, domain, schema, version);
        String query = String.format("query/%s/search", type);
        switch (type) {
            case "minds/core/dataset/v1.0.0":
                return this.createDataset(databaseScope, liveMode, query, id, authorization);
            case "minds/core/person/v1.0.0":
                List<String> queries = Arrays.asList(query, "query/uniminds/core/person/v1.0.0/search");
                return this.createContributorForV1(databaseScope, liveMode, queries, id, authorization);
            case "uniminds/core/person/v1.0.0":
                return this.createContributor(databaseScope, liveMode, query, id, authorization);
            case "minds/core/placomponent/v1.0.0":
                return this.createProject(databaseScope, liveMode, query, id, authorization);
            case "uniminds/core/modelinstance/v1.0.0":
                return this.createModel(databaseScope, liveMode, query, id, authorization);
            case "softwarecatalog/software/softwareproject/v1.0.0":
                return this.createSoftware(databaseScope, liveMode, type, id, authorization);
            case "minds/experiment/subject/v1.0.0":
                return this.createSubject(databaseScope, liveMode, query, id, authorization);
            case "minds/experiment/sample/v1.0.0":
                return this.createSample(databaseScope, liveMode, query, id, authorization);
        }
        throw new HttpClientErrorException(HttpStatus.NOT_FOUND, String.format("Type %s is not recognized as a valid search resource!", type));
    }

    public List<TargetInstance> createInstances(DatabaseScope databaseScope, boolean liveMode, String type, String authorization) {
        switch (type) {
            case "Dataset":
                return this.createDatasets(databaseScope, liveMode, authorization);
            case "Contributor":
                return this.createContributors(databaseScope, liveMode, authorization);
            case "Project":
                return this.createProjects(databaseScope, liveMode, authorization);
            case "Model":
                return this.createModels(databaseScope, liveMode, authorization);
            case "Subject":
                return this.createSubjects(databaseScope, liveMode, authorization);
            case "Sample":
                return this.createSamples(databaseScope, liveMode, authorization);
            case "Software":
                return this.createSoftwares(databaseScope, liveMode, authorization);
        }
        return Collections.emptyList();
    }

}
