package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.openMINDSv3.ModelV3;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Version;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Model;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetExternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.utils.IdUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static eu.ebrains.kg.search.controller.translators.TranslatorCommons.firstItemOrNull;

public class ModelOfKGV3Translator implements Translator<ModelV3, Model>{

    public Model translate(ModelV3 model, DataStage dataStage, boolean liveMode) {
        Model m = new Model();
        m.setId(IdUtils.getUUID(model.getId()));
        m.setIdentifier(IdUtils.getUUID(model.getIdentifier()));
        m.setDescription(model.getDescription());
        m.setTitle(model.getTitle());
        String homepage = model.getHomepage();
        if (StringUtils.isNotBlank(homepage)) {
            m.setHomepage(new TargetExternalReference(homepage, homepage));
        }
        if (!CollectionUtils.isEmpty(model.getStudyTarget())) {
            m.setStudyTarget(model.getStudyTarget());
        }
        if (!CollectionUtils.isEmpty(model.getModelScope())) {
            m.setScope(model.getModelScope());
        }
        if (!CollectionUtils.isEmpty(model.getAbstractionLevel())) {
            m.setAbstractionLevel(model.getAbstractionLevel());
        }
        if (!CollectionUtils.isEmpty(model.getDeveloper())) {
            m.setContributors(model.getDeveloper().stream()
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        }
        if (!CollectionUtils.isEmpty(model.getCustodian())) {
            m.setOwners(model.getCustodian().stream()
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        }

        String citation = model.getHowToCite();
        String digitalIdentifier = firstItemOrNull(model.getDigitalIdentifier());
        if (digitalIdentifier != null) {
            if (StringUtils.isNotBlank(citation) && StringUtils.isNotBlank(digitalIdentifier)) {
                String url = URLEncoder.encode(digitalIdentifier, StandardCharsets.UTF_8);
                m.setCitation(citation + String.format(" [DOI: %s]\n[DOI: %s]: https://doi.org/%s", digitalIdentifier, digitalIdentifier, url));
            }
            if (StringUtils.isNotBlank(digitalIdentifier)) {
                m.setDoi(digitalIdentifier);
            }
        }
        if (!CollectionUtils.isEmpty(model.getVersions())) {
            List<Version> sortedVersions = Helpers.sort(model.getVersions());                                         //v.getFullName()
            List<TargetInternalReference> references = sortedVersions.stream().map(v -> new TargetInternalReference(IdUtils.getUUID(v.getId()), v.getVersionIdentifier())).collect(Collectors.toList());
            m.setVersions(references);
        }

        return m;
    }
}
