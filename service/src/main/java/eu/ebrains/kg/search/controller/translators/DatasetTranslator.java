package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.DatabaseScope;
import eu.ebrains.kg.search.model.source.commons.File;
import eu.ebrains.kg.search.model.source.openMINDSv1.DatasetV1;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Dataset;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.ExternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.InternalReference;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class DatasetTranslator implements Translator<DatasetV1, Dataset> {

    public Dataset translate(DatasetV1 datasetV1, DatabaseScope databaseScope, boolean liveMode) {
        Dataset d = new Dataset();
        String embargo = datasetV1.getEmbargo().get(0);
        String containerUrl = datasetV1.getContainerUrl();
        Boolean containerUrlAsZIP = datasetV1.getContainerUrlAsZIP();
        List<File> files = datasetV1.getFiles();
        if (!embargo.equals("Embargoed") &&
                !embargo.equals("Under review") &&
                (!containerUrl.isEmpty() && (containerUrlAsZIP || !files.isEmpty()))) {
            d.setZip(new ExternalReference(
                    String.format("https://kg.ebrains.eu/proxy/export?container=%s", containerUrl),
                    "Download all related data as ZIP"
            ));
        }
        d.setIdentifier(datasetV1.getIdentifier());
        d.setEditorId(datasetV1.getEditorId());
        d.setMethods(datasetV1.getMethods());
        d.setDescription(datasetV1.getDescription());

        eu.ebrains.kg.search.model.source.commons.ExternalReference license = datasetV1.getLicense().get(0);
        d.setLicenseInfo(new ExternalReference(license.getUrl(), license.getName()));
        d.setOwners(datasetV1.getOwners().stream()
                .map(o -> new InternalReference(
                        String.format("Contributor/%s", o.getIdentifier()),
                        o.getName(),
                        null
                )).collect(Collectors.toList()));
        d.setDataDescriptor(new ExternalReference(datasetV1.getDataDescriptorURL(), datasetV1.getDataDescriptorURL()));
        d.setSpeciesFilter(datasetV1.getSpeciesFilter());
        //d.setFiles(); TODO: implement the files translation
        d.setPublications(datasetV1.getPublications().stream()
                .map(publication -> {
                    String publicationResult = "";
                    if (publication.getCitation() != null && publication.getDoi() != null) {
                        String url = URLEncoder.encode(publication.getDoi(), StandardCharsets.UTF_8);
                        publicationResult = publication.getCitation() + "\n" + String.format("[DOI: %s]\\n[DOI: %s]: https://doi.org/%s\"", publication.getDoi(), publication.getDoi(), url);
                    } else if (publication.getCitation() != null && publication.getDoi() == null) {
                        publicationResult = publication.getCitation().trim().replaceAll(", $", "");
                        ;
                    } else {
                        publicationResult = publication.getDoi();
                    }
                    return publicationResult;
                }).collect(Collectors.toList()));
        d.setAtlas(datasetV1.getParcellationAtlas());
        d.setExternalDatalink(datasetV1.getExternalDatalink());
        d.setRegion(datasetV1.getParcellationRegion().stream()
                .map(r -> new ExternalReference(r.getUrl(), r.getAlias().isEmpty() ? r.getName() : r.getAlias() ))
                .collect(Collectors.toList()));
        d.setTitle(datasetV1.getTitle());
        d.setModalityForFilter(datasetV1.getModalityForFilter());
        d.setDoi(datasetV1.getDoi().get(0));
        d.setContributors(datasetV1.getContributors().stream()
                .map(c -> new InternalReference(
                        String.format("Contributor/%s", c.getIdentifier()),
                        c.getName(),
                        null
                )).collect(Collectors.toList()));
        d.setPreparation(datasetV1.getPreparation());
        d.setComponent(datasetV1.getComponent().stream()
                .map(c -> new InternalReference(
                        String.format("Project/%s", c.getIdentifier()),
                        c.getName(),
                        null
                )).collect(Collectors.toList()));
        d.setProtocol(datasetV1.getProtocols());
        d.setViewer(datasetV1.getBrainViewer().stream()
                .map(v -> new ExternalReference(
                     v.getUrl(),
                     v.getName().isEmpty() ? "Show in brain atlas viewer":String.format("Show %s in brain atlas viewer", v.getName())
                )).collect(Collectors.toList()));
        d.setSubjects(datasetV1.getSubjects().stream()
                .map(s ->
                        new Dataset.Subject(
                                new InternalReference(
                                        false?s.getRelativeUrl():String.format("Subject/%s", d.getIdentifier()), // TODO: replace false by isLive
                                        s.getName(),
                                        null
                                ),
                                s.getSpecies(),
                                s.getSex(),
                                s.getAge(),
                                s.getAgeCategory(),
                                s.getWeight(),
                                s.getStrain()!=null?s.getStrain():s.getStrains(),
                                s.getGenotype(),
                                s.getSamples().stream().map(sample -> new InternalReference(
                                        String.format("Sample/%s", sample.getIdentifier()),
                                        sample.getName(),
                                        null
                                )).collect(Collectors.toList())
                        )
                ).collect(Collectors.toList()));
        d.setFirstRelease(datasetV1.getFirstReleaseAt());
        d.setLastRelease(datasetV1.getLastReleaseAt());
        return d;
    }
}
