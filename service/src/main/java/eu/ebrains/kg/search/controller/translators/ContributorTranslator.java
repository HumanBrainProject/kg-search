package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.DatabaseScope;
import eu.ebrains.kg.search.model.source.PersonSources;
import eu.ebrains.kg.search.model.source.PersonV1andV2;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Contributor;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;

import java.util.stream.Collectors;

public class ContributorTranslator implements Translator<PersonSources, Contributor> {

    public Contributor translate(PersonSources personSources, DatabaseScope databaseScope, boolean liveMode) {
        Contributor c = new Contributor();
        PersonV1andV2 person = personSources.getPersonV2() != null ? personSources.getPersonV2() : personSources.getPersonV1();
        c.setFirstRelease(person.getFirstReleaseAt());
        c.setLastRelease(person.getLastReleaseAt());
        c.setTitle(person.getTitle());
        c.setContributions(person.getContributions().stream()
                .map(contribution ->
                        new TargetInternalReference(
                                liveMode ? contribution.getRelativeUrl() : String.format("Dataset/%s", contribution.getIdentifier()),
                                contribution.getName(), null)).collect(Collectors.toList()));

        c.setCustodianOf(person.getCustodianOf().stream()
                .map(custodianOf ->
                        new TargetInternalReference(
                                liveMode ? custodianOf.getRelativeUrl() : String.format("Dataset/%s", custodianOf.getIdentifier()),
                                custodianOf.getName(), null)).collect(Collectors.toList()));

        c.setModelContributions(person.getModelContributions().stream()
                .map(contribution -> new TargetInternalReference(
                        liveMode ? contribution.getRelativeUrl() : String.format("Model/%s", contribution.getIdentifier()),
                        contribution.getName(), null
                )).collect(Collectors.toList()));
        if (databaseScope == DatabaseScope.INFERRED) {
            c.setEditorId(person.getEditorId());
        }
        return c;
    }
}
