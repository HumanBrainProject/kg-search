package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.PersonSources;
import eu.ebrains.kg.search.model.source.PersonV1andV2;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Contributor;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.utils.ESHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static eu.ebrains.kg.search.controller.translators.TranslatorCommons.*;

public class ContributorTranslator implements Translator<PersonSources, Contributor> {


    public Contributor translate(PersonSources personSources, DataStage dataStage, boolean liveMode) {
        Contributor c = new Contributor();
        PersonV1andV2 person = personSources.getPersonV2() != null ? personSources.getPersonV2() : personSources.getPersonV1();
        String uuid = ESHelper.getUUID(person.getId());
        c.setId(uuid);
        List<String> identifiers = Arrays.asList(String.format("Contributor/%s", person.getIdentifier()), uuid);
        c.setIdentifier(identifiers);
        c.setFirstRelease(person.getFirstReleaseAt());
        c.setLastRelease(person.getLastReleaseAt());
        c.setTitle(person.getTitle());
        if(!CollectionUtils.isEmpty(person.getContributions())) {
            c.setContributions(person.getContributions().stream()
                    .map(contribution ->
                            new TargetInternalReference(
                                    liveMode ? contribution.getRelativeUrl() : String.format("Dataset/%s", contribution.getIdentifier()),
                                    contribution.getName(), null)).collect(Collectors.toList()));
        }

        if(!CollectionUtils.isEmpty(person.getCustodianOf())) {
            c.setCustodianOf(person.getCustodianOf().stream()
                    .map(custodianOf ->
                            new TargetInternalReference(
                                    liveMode ? custodianOf.getRelativeUrl() : String.format("Dataset/%s", custodianOf.getIdentifier()),
                                    custodianOf.getName(), null)).collect(Collectors.toList()));
        }

        if(!CollectionUtils.isEmpty(person.getCustodianOfModel())) {
            c.setCustodianOfModel(person.getCustodianOfModel().stream()
                    .map(custodianOfModel ->
                            new TargetInternalReference(
                                    liveMode ? custodianOfModel.getRelativeUrl() : String.format("Model/%s", custodianOfModel.getIdentifier()),
                                    custodianOfModel.getName(), null)).collect(Collectors.toList()));
        }

        if(!CollectionUtils.isEmpty(person.getModelContributions())) {
            c.setModelContributions(person.getModelContributions().stream()
                    .map(contribution -> new TargetInternalReference(
                            liveMode ? contribution.getRelativeUrl() : String.format("Model/%s", contribution.getIdentifier()),
                            contribution.getName(), null
                    )).collect(Collectors.toList()));
        }
        if(!CollectionUtils.isEmpty(person.getPublications())) {
            c.setPublications(emptyToNull(person.getPublications().stream()
                    .map(publication -> {
                        String publicationResult = null;
                        if (StringUtils.isNotBlank(publication.getCitation()) && StringUtils.isNotBlank(publication.getDoi())) {
                            String url = URLEncoder.encode(publication.getDoi(), StandardCharsets.UTF_8);
                            publicationResult = publication.getCitation() + "\n" + String.format("[DOI: %s]\n[DOI: %s]: https://doi.org/%s", publication.getDoi(), publication.getDoi(), url);
                        } else if (StringUtils.isNotBlank(publication.getCitation()) && StringUtils.isBlank(publication.getDoi())) {
                            publicationResult = publication.getCitation().trim().replaceAll(",$", "");
                        } else if (StringUtils.isBlank(publication.getCitation()) && StringUtils.isNotBlank(publication.getDoi())) {
                            String url = URLEncoder.encode(publication.getDoi(), StandardCharsets.UTF_8);
                            publicationResult = String.format("[DOI: %s]\n[DOI: %s]: https://doi.org/%s", publication.getDoi(), publication.getDoi(), url);
                        }
                        return publicationResult;
                    }).filter(Objects::nonNull).collect(Collectors.toList())));
        }
        if (dataStage == DataStage.IN_PROGRESS) {
            c.setEditorId(person.getEditorId());
        }
        return c;
    }
}
