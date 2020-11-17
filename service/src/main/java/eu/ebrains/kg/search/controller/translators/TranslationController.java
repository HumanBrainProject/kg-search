package eu.ebrains.kg.search.controller.translators;


import eu.ebrains.kg.search.controller.kg.KGv2;
import eu.ebrains.kg.search.controller.kg.KGv3;
import eu.ebrains.kg.search.model.source.PersonSources;
import eu.ebrains.kg.search.model.source.ResultOfKGv2;
import eu.ebrains.kg.search.model.source.ResultOfKGv3;
import eu.ebrains.kg.search.model.source.openMINDSv1.PersonV1;
import eu.ebrains.kg.search.model.source.openMINDSv2.PersonV2;
import eu.ebrains.kg.search.model.source.openMINDSv2.SoftwareV2;
import eu.ebrains.kg.search.model.source.openMINDSv3.PersonV3;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Contributor;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Software;
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

    public void translateInstance(){
        createContributors();
        createSoftware();
    }


    private List<Software> createSoftware(){
        ResultOfKGv2<SoftwareV2> software = kgV2.fetchInstances(SoftwareV2.class);
        SoftwareTranslator translator = new SoftwareTranslator();
        return software.getResults().stream().map(translator::translate).collect(Collectors.toList());
    }

    private List<Contributor> createContributors(){
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
        return personSources.stream().map(translator::translate).collect(Collectors.toList());
    }




}