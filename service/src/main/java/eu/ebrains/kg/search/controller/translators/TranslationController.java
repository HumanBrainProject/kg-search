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
import eu.ebrains.kg.search.model.target.elasticsearch.instances.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TranslationController {
    private final KGv2 kgV2;
    private final KGv3 kgV3;

    public TranslationController(KGv2 kgV2, KGv3 kgV3) {
        this.kgV2 = kgV2;
        this.kgV3 = kgV3;
    }

    public void translateInstances(DatabaseScope databaseScope, boolean liveMode) {
        createContributors(databaseScope, liveMode);
        createSoftwares(databaseScope, liveMode);
        createDatasets(databaseScope, liveMode);
        createModels(databaseScope, liveMode);
        createProjects(databaseScope, liveMode);
        createSamples(databaseScope, liveMode);
        createSubjects(databaseScope, liveMode);
    }

    private List<Contributor> createContributors(DatabaseScope databaseScope, boolean liveMode){
        ResultOfKGv2<PersonV1> personsFromV1 = kgV2.fetchInstances(PersonV1.class);
        ResultOfKGv2<PersonV2> personsFromV2 = kgV2.fetchInstances(PersonV2.class);
        ResultOfKGv3<PersonV3> personsFromV3 = kgV3.fetchInstances(PersonV3.class);

        List<PersonSources> personSources = new ArrayList<>();
        personsFromV1.getResults().forEach(p -> {
            PersonSources source = new PersonSources();
            personSources.add(source);
            source.setPersonV1(p);
        });

        Map<String, PersonSources> personSourcesByV1Identifier = personSources.stream().collect(Collectors.toMap(k -> k.getPersonV1().getIdentifier(), v -> v));

        personsFromV2.getResults().forEach(p ->{
            PersonSources source = personSourcesByV1Identifier.get(p.getIdentifier());
            if(source == null){
                source = new PersonSources();
                personSources.add(source);
            }
            source.setPersonV2(p);
        });

        //TODO add v3

        ContributorTranslator translator = new ContributorTranslator();
        return personSources.stream().map(p -> translator.translate(p, databaseScope, liveMode)).collect(Collectors.toList());
    }

    public Contributor createContributor(DatabaseScope databaseScope, boolean liveMode, String query, String id, String authorization) {
        return null;
    }

    public List<Software> createSoftwares(DatabaseScope databaseScope, boolean liveMode){
        ResultOfKGv2<SoftwareV2> software =  kgV2.fetchInstances(SoftwareV2.class);
        SoftwareTranslator translator = new SoftwareTranslator();
        return software.getResults().stream().map(s -> translator.translate(s, databaseScope, liveMode)).collect(Collectors.toList());
    }

    public Software createSoftware(DatabaseScope databaseScope, boolean liveMode, String query, String id, String authorization) {
        SoftwareV2 software =  kgV2.fetchInstance(SoftwareV2.class, query, id, authorization, databaseScope);
        SoftwareTranslator translator = new SoftwareTranslator();
        return translator.translate(software, databaseScope, liveMode);
    }

    public List<Dataset> createDatasets(DatabaseScope databaseScope, boolean liveMode){
        ResultOfKGv2<DatasetV1> dataset = kgV2.fetchInstances(DatasetV1.class);
        DatasetTranslator translator = new DatasetTranslator();
        return dataset.getResults().stream().map(d -> translator.translate(d, databaseScope, liveMode)).collect(Collectors.toList());
    }

    public Dataset createDataset(DatabaseScope databaseScope, boolean liveMode, String query, String id, String authorization) {
        DatasetV1 dataset =  kgV2.fetchInstance(DatasetV1.class, query, id, authorization, databaseScope);
        DatasetTranslator translator = new DatasetTranslator();
        return translator.translate(dataset, databaseScope, liveMode);
    }

    public List<Model> createModels(DatabaseScope databaseScope, boolean liveMode){
        ResultOfKGv2<ModelV2> model = kgV2.fetchInstances(ModelV2.class);
        ModelTranslator translator = new ModelTranslator();
        return model.getResults().stream().map(m -> translator.translate(m, databaseScope, liveMode)).collect(Collectors.toList());
    }

    public Model createModel(DatabaseScope databaseScope, boolean liveMode, String query, String id, String authorization){
        ModelV2 model = kgV2.fetchInstance(ModelV2.class, query, id, authorization, databaseScope);
        ModelTranslator translator = new ModelTranslator();
        return translator.translate(model, databaseScope, liveMode);
    }

    public List<Project> createProjects(DatabaseScope databaseScope, boolean liveMode){
        ResultOfKGv2<ProjectV1> project = kgV2.fetchInstances(ProjectV1.class);
        ProjectTranslator translator = new ProjectTranslator();
        return project.getResults().stream().map(p -> translator.translate(p, databaseScope, liveMode)).collect(Collectors.toList());
    }

    public Project createProject(DatabaseScope databaseScope, boolean liveMode, String query, String id, String authorization) {
        ProjectV1 project = kgV2.fetchInstance(ProjectV1.class, query, id, authorization, databaseScope);
        ProjectTranslator translator = new ProjectTranslator();
        return translator.translate(project, databaseScope, liveMode);
    }

    public List<Sample> createSamples(DatabaseScope databaseScope, boolean liveMode){
        ResultOfKGv2<SampleV1> sample = kgV2.fetchInstances(SampleV1.class);
        SampleTranslator translator = new SampleTranslator();
        return sample.getResults().stream().map(s -> translator.translate(s, databaseScope, liveMode)).collect(Collectors.toList());
    }

    public Sample createSample(DatabaseScope databaseScope, boolean liveMode, String query, String id, String authorization){
        SampleV1 sample = kgV2.fetchInstance(SampleV1.class, query, id, authorization, databaseScope);
        SampleTranslator translator = new SampleTranslator();
        return translator.translate(sample, databaseScope, liveMode);
    }

    public List<Subject> createSubjects(DatabaseScope databaseScope, boolean liveMode){
        ResultOfKGv2<SubjectV1> subject = kgV2.fetchInstances(SubjectV1.class);
        SubjectTranslator translator = new SubjectTranslator();
        return subject.getResults().stream().map(s -> translator.translate(s, databaseScope, liveMode)).collect(Collectors.toList());
    }

    public Subject createSubject(DatabaseScope databaseScope, boolean liveMode, String query, String id, String authorization){
        SubjectV1 subject = kgV2.fetchInstance(SubjectV1.class, query, id, authorization, databaseScope);
        SubjectTranslator translator = new SubjectTranslator();
        return translator.translate(subject, databaseScope, liveMode);
    }

}
