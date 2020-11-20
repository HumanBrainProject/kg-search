package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.DatabaseScope;
import eu.ebrains.kg.search.model.source.commons.SourceExternalReference;
import eu.ebrains.kg.search.model.source.openMINDSv2.ModelV2;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Model;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetExternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class ModelTranslator implements Translator<ModelV2, Model>{

    public Model translate(ModelV2 modelV2, DatabaseScope databaseScope, boolean liveMode) {
        Model m = new Model();
        String embargo = modelV2.getEmbargo().get(0);
        m.setIdentifier(modelV2.getIdentifier());
        m.setEditorId(modelV2.getEditorId());
        m.setEmbargo(embargo.equals("embargoed") ? "This model is temporarily under embargo. The data will become available for download after the embargo period.":null);
        m.setProducedDataset(modelV2.getProducedDataset().stream()
                .map(pd -> new TargetInternalReference(
                        String.format("Dataset/%s", pd.getIdentifier()),
                        pd.getName(),
                        null
                )).collect(Collectors.toList()));
        m.setAllFiles(modelV2.getFileBundle().stream()
                .map(fb -> {
                   if(embargo.equals("embargoed")) {
                       return null;
                   }
                   if(fb.getUrl().startsWith("https://object.cscs.ch")) {
                       return new TargetExternalReference(
                         String.format("https://kg.ebrains.eu/proxy/export?container=%s", fb.getUrl()),
                         "Download all related data as ZIP"
                       );
                   } else {
                       return new TargetExternalReference(
                               fb.getUrl(),
                               "Go to the data."
                       );
                   }
                }).collect(Collectors.toList()));
        m.setModelFormat(modelV2.getModelFormat());
        m.setDescription(modelV2.getDescription());

        SourceExternalReference license = modelV2.getLicense().get(0);
        m.setLicenseInfo(new TargetExternalReference(license.getUrl(), license.getName()));
        m.setOwners(modelV2.getContributors().stream()
                .map(o -> new TargetInternalReference(
                        String.format("Contributor/%s", o.getIdentifier()),
                        o.getName(),
                        null
                )).collect(Collectors.toList()));
        m.setAbstractionLevel(modelV2.getAbstractionLevel());
        m.setMainContact(modelV2.getMainContact().stream()
                .map(mc -> new TargetInternalReference(
                    String.format("Contributor/%s", mc.getIdentifier()),
                    mc.getName(),
                    null
                )).collect(Collectors.toList()));
        m.setBrainStructures(modelV2.getBrainStructure());
        m.setUsedDataset(modelV2.getUsedDataset().stream()
                .map(ud -> new TargetInternalReference(
                        String.format("Dataset/%s", ud.getIdentifier()),
                        ud.getName(),
                        null
                )).collect(Collectors.toList()));
        m.setVersion(modelV2.getVersion());
        m.setPublications(modelV2.getPublications().stream()
                .map(publication -> {
                    String publicationResult = "";
                    if (publication.getCitation() != null && publication.getDoi() != null) {
                        String url = URLEncoder.encode(publication.getDoi(), StandardCharsets.UTF_8);
                        publicationResult = publication.getCitation() + "\n" + String.format("[DOI: %s]\\n[DOI: %s]: https://doi.org/%s\"", publication.getDoi(), publication.getDoi(), url);
                    } else {
                        publicationResult = publication.getDoi();
                    }
                    return publicationResult;
                }).collect(Collectors.toList()));
        m.setStudyTarget(modelV2.getStudyTarget());
        m.setModelScope(modelV2.getModelScope());
        m.setTitle(modelV2.getTitle());
        m.setContributors(modelV2.getContributors().stream()
                .map(c -> new TargetInternalReference(
                        String.format("Contributor/%s", c.getIdentifier()),
                        c.getName(),
                        null
                )).collect(Collectors.toList()));
        m.setCellularTarget(modelV2.getCellularTarget());
        m.setFirstRelease(modelV2.getFirstReleaseAt());
        m.setLastRelease(modelV2.getLastReleaseAt());
        return m;
    }
}
