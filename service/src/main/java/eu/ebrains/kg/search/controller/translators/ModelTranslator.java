package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.DatabaseScope;
import eu.ebrains.kg.search.model.source.commons.SourceExternalReference;
import eu.ebrains.kg.search.model.source.openMINDSv2.ModelV2;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Model;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetExternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetFile;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import org.apache.commons.lang3.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static eu.ebrains.kg.search.controller.translators.TranslatorCommons.*;

public class ModelTranslator implements Translator<ModelV2, Model> {

    public Model translate(ModelV2 modelV2, DatabaseScope databaseScope, boolean liveMode) {
        Model m = new Model();
        m.setIdentifier(modelV2.getIdentifier());
        if (databaseScope == DatabaseScope.INFERRED) {
            m.setEditorId(modelV2.getEditorId());
        }
        if (hasEmbargoStatus(modelV2, EMBARGOED)) {
            if (databaseScope == DatabaseScope.RELEASED) {
                m.setEmbargo("This model is temporarily under embargo. The data will become available for download after the embargo period.");
            } else {
                SourceExternalReference fileBundle = firstItemOrNull(modelV2.getFileBundle());
                if (fileBundle != null) {
                    String fileUrl = fileBundle.getUrl();
                    if (StringUtils.isNotBlank(fileUrl) && fileUrl.startsWith("https://object.cscs.ch")) {
                        m.setEmbargo(String.format("This model is temporarily under embargo. The data will become available for download after the embargo period.<br/><br/>If you are an authenticated user, <a href=\"https://kg.ebrains.eu/files/cscs/list?url=%s\" target=\"_blank\"> you should be able to access the data here</a>", fileUrl));
                    } else {
                        m.setEmbargo("This model is temporarily under embargo. The data will become available for download after the embargo period.");
                    }
                }
            }
        }
        if (modelV2.getProducedDataset() != null) {
            m.setProducedDataset(modelV2.getProducedDataset().stream()
                    .map(pd -> new TargetInternalReference(
                            liveMode ? pd.getRelativeUrl() : String.format("Dataset/%s", pd.getIdentifier()),
                            pd.getName()
                    )).collect(Collectors.toList()));
        }
        if (databaseScope == DatabaseScope.INFERRED || (databaseScope == DatabaseScope.RELEASED && !hasEmbargoStatus(modelV2, EMBARGOED))) {
            if (modelV2.getFiles() != null) {
                m.setFiles(modelV2.getFiles().stream()
                        .filter(v -> v.getAbsolutePath() != null && v.getName() != null)
                        .map(f ->
                                new TargetFile(
                                        f.getPrivateAccess() ? String.format("%s/files/cscs?url=%s", Translator.fileProxy, f.getAbsolutePath()) : f.getAbsolutePath(),
                                        f.getPrivateAccess() ? String.format("ACCESS PROTECTED: %s", f.getName()) : f.getName(),
                                        f.getHumanReadableSize()
                                )
                        ).collect(Collectors.toList()));
            }
        }
        if (!hasEmbargoStatus(modelV2, EMBARGOED) && modelV2.getFileBundle() != null) {
            m.setAllFiles(modelV2.getFileBundle().stream()
                    .map(fb -> {
                        if (fb.getUrl().startsWith("https://object.cscs.ch")) {
                            return new TargetExternalReference(
                                    String.format("https://kg.ebrains.eu/proxy/export?container=%s", fb.getUrl()),
                                    "download all related data as ZIP" // TODO: Capitalize the value
                            );
                        } else {
                            return new TargetExternalReference(
                                    fb.getUrl(),
                                    "Go to the data."
                            );
                        }
                    }).collect(Collectors.toList()));
        }
        m.setModelFormat(modelV2.getModelFormat());
        m.setDescription(modelV2.getDescription());

        SourceExternalReference license = firstItemOrNull(modelV2.getLicense());
        if (license != null) {
            m.setLicenseInfo(new TargetExternalReference(license.getUrl(), license.getName()));
        }

        if (modelV2.getCustodian() != null) {
            m.setOwners(modelV2.getCustodian().stream()
                    .map(o -> new TargetInternalReference(
                            liveMode ? o.getRelativeUrl() : String.format("Contributor/%s", o.getIdentifier()),
                            o.getName()
                    )).collect(Collectors.toList()));
        }

        m.setAbstractionLevel(modelV2.getAbstractionLevel());

        if (modelV2.getMainContact() != null) {
            m.setMainContact(modelV2.getMainContact().stream()
                    .map(mc -> new TargetInternalReference(
                            liveMode ? mc.getRelativeUrl() : String.format("Contributor/%s", mc.getIdentifier()),
                            mc.getName()
                    )).collect(Collectors.toList()));
        }
        m.setBrainStructures(modelV2.getBrainStructure());

        if (modelV2.getUsedDataset() != null) {
            m.setUsedDataset(modelV2.getUsedDataset().stream()
                    .map(ud -> new TargetInternalReference(
                            liveMode ? ud.getRelativeUrl() : String.format("Dataset/%s", ud.getIdentifier()),
                            ud.getName()
                    )).collect(Collectors.toList()));
        }
        m.setVersion(modelV2.getVersion());
        if (modelV2.getPublications() != null) {
            m.setPublications(modelV2.getPublications().stream()
                    .filter(p -> StringUtils.isNotBlank(p.getDoi()))
                    .map(p -> {
                        if (StringUtils.isNotBlank(p.getCitation())) {
                            String url = URLEncoder.encode(p.getDoi(), StandardCharsets.UTF_8);
                            return p.getCitation() + "\n" + String.format("[DOI: %s]\\n[DOI: %s]: https://doi.org/%s\"", p.getDoi(), p.getDoi(), url);
                        } else {
                            return p.getDoi();
                        }
                    }).collect(Collectors.toList()));
        }
        m.setStudyTarget(modelV2.getStudyTarget());
        m.setModelScope(modelV2.getModelScope());
        m.setTitle(modelV2.getTitle());
        if (modelV2.getContributors() != null) {
            m.setContributors(modelV2.getContributors().stream()
                    .map(c -> new TargetInternalReference(
                            liveMode ? c.getRelativeUrl() : String.format("Contributor/%s", c.getIdentifier()),
                            c.getName()
                    )).collect(Collectors.toList()));
        }
        m.setCellularTarget(modelV2.getCellularTarget());
        m.setFirstRelease(modelV2.getFirstReleaseAt());
        m.setLastRelease(modelV2.getLastReleaseAt());
        return m;
    }
}
