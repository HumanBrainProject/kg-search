package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.controller.translators.Translator;
import eu.ebrains.kg.search.model.source.PersonSources;
import eu.ebrains.kg.search.model.source.PersonV1andV2;
import eu.ebrains.kg.search.model.source.openMINDSv1.PersonV1;
import eu.ebrains.kg.search.model.source.openMINDSv2.PersonV2;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Contributor;

import java.util.stream.Collectors;

public class ContributorTranslator implements Translator<PersonSources, Contributor> {

    private final static String TYPE = "Contributor";


    public Contributor translate(PersonSources personSources) {
        Contributor c = new Contributor();
        PersonV1andV2 person = personSources.getPersonV2() != null ? personSources.getPersonV2() : personSources.getPersonV1();
        c.setFirstRelease(person.getFirstReleaseAt());
        c.setLastRelease(person.getLastReleaseAt());
        c.setTitle(person.getTitle());
        c.setContributions(person.getContributions().stream()
                .map(contribution ->
                        new Contributor.InternalReference(
                                String.format("Dataset/%s", contribution.getIdentifier()),
                                contribution.getName())).collect(Collectors.toList()));

        c.setCustodianOf(person.getCustodianOf().stream()
                .map(custodianOf ->
                        new Contributor.InternalReference(
                                String.format("Dataset/%s", custodianOf.getIdentifier()),
                                custodianOf.getName())).collect(Collectors.toList()));

        c.setModelContributions(person.getModelContributions().stream()
                .map(contribution -> new Contributor.InternalReference(
                        String.format("Model/%s", contribution.getIdentifier()),
                        contribution.getName()
                )).collect(Collectors.toList()));

        c.setType(TYPE);
        return c;
    }
}
