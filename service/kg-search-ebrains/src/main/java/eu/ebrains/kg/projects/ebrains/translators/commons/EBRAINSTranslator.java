package eu.ebrains.kg.projects.ebrains.translators.commons;

import eu.ebrains.kg.common.controller.translation.models.Translator;
import eu.ebrains.kg.common.model.source.FullNameRef;
import eu.ebrains.kg.common.model.source.IsCiteable;
import eu.ebrains.kg.common.model.source.ResultsOfKG;
import eu.ebrains.kg.common.model.target.HasCitation;
import eu.ebrains.kg.common.model.target.TargetExternalReference;
import eu.ebrains.kg.common.model.target.TargetInternalReference;
import eu.ebrains.kg.common.utils.IdUtils;
import eu.ebrains.kg.projects.ebrains.EBRAINSTranslatorUtils;
import eu.ebrains.kg.projects.ebrains.source.commons.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class EBRAINSTranslator<Source, Target, ListResult extends ResultsOfKG<Source>> extends Translator<Source, Target, ListResult> {
    protected TargetInternalReference ref(FullNameRefWithVersion ref) {
        if (ref != null) {
            final String uuid = IdUtils.getUUID(ref.getId());
            return new TargetInternalReference(uuid,  StringUtils.isNotBlank(ref.getFullName()) && StringUtils.isNotBlank(ref.getVersionIdentifier()) ? String.format("%s (%s)", ref.getFullName(), ref.getVersionIdentifier()) : StringUtils.defaultIfBlank(ref.getFullName(), uuid));
        }
        return null;
    }
    protected List<TargetInternalReference> refVersion(List<? extends FullNameRefForResearchProduct> refs, boolean sorted) {
        if (!CollectionUtils.isEmpty(refs)) {
            Stream<TargetInternalReference> targetInternalReferenceStream = refs.stream().map(r -> {
                if (r instanceof FullNameRefForResearchProductVersion) {
                    return this.ref((FullNameRefForResearchProductVersion) r);
                }
                if (r instanceof FullNameRefForResearchProductVersionTarget) {
                    return this.ref((FullNameRefForResearchProductVersionTarget) r);
                }
                return null;
            }).filter(Objects::nonNull);
            if (sorted) {
                targetInternalReferenceStream = targetInternalReferenceStream.sorted();
            }
            return targetInternalReferenceStream.collect(Collectors.toList());
        }
        return null;
    }
    protected List<TargetInternalReference> refExtendedVersion(List<? extends ExtendedFullNameRefForResearchProductVersion> refs, boolean sort) {
        if (!CollectionUtils.isEmpty(refs)) {
            Stream<TargetInternalReference> targetInternalReferenceStream = refs.stream().filter(r -> r.getGrouping() == null).map(this::ref).filter(Objects::nonNull);
            if (sort) {
                targetInternalReferenceStream = targetInternalReferenceStream.sorted();
            }
            List<TargetInternalReference> result = targetInternalReferenceStream.collect(Collectors.toList());
            final Map<String, ? extends List<? extends ExtendedFullNameRefForResearchProductVersion>> byGrouping = refs.stream().filter(r -> r.getGrouping() != null).collect(Collectors.groupingBy(r -> r.getGrouping()));
            byGrouping.keySet().stream().sorted().forEach(group -> {
                if(!result.isEmpty()) {
                    result.add(new TargetInternalReference(null, ""));
                }
                result.add(new TargetInternalReference(null, group));
                final List<? extends ExtendedFullNameRefForResearchProductVersion> instances = byGrouping.get(group);
                Stream<TargetInternalReference> targetInternalReferenceStreamForGroups = instances.stream().map(this::ref).filter(Objects::nonNull);
                if (sort) {
                    targetInternalReferenceStreamForGroups = targetInternalReferenceStreamForGroups.sorted();
                }
                result.addAll(targetInternalReferenceStreamForGroups.collect(Collectors.toList()));
            });
            if(!CollectionUtils.isEmpty(result)){
                return result;
            }
        }
        return null;
    }

    protected TargetInternalReference ref(FullNameRefForResearchProductVersionTarget ref) {
        if (ref != null) {
            FullNameRef researchProduct = ref.getResearchProduct();
            String name = StringUtils.defaultIfBlank(ref.getFullName(), researchProduct.getFullName());
            String uuid = IdUtils.getUUID(researchProduct.getId());
            if (name == null) {
                name = uuid;
            }
            String versionedName = StringUtils.isNotBlank(ref.getVersionIdentifier()) ? String.format("%s %s", name, ref.getVersionIdentifier()) : name;
            //TODO enable link when BrainAtals card should be enable
            return new TargetInternalReference(null, versionedName);
            //return new TargetInternalReference(uuid, versionedName, new TargetInternalReference.Context(ref.getTab(), IdUtils.getUUID(ref.getId())));
        }
        return null;
    }

    protected TargetInternalReference ref(ExtendedFullNameRefForResearchProductVersion ref) {
        if (ref != null) {
            return ref(ref.getRelevantReference());
        }
        return null;
    }

    protected TargetExternalReference link(FileRepository ref) {
        if (ref != null && StringUtils.isNotBlank(ref.getIri())) {
            return new TargetExternalReference(ref.getIri(), ref.getIri() != null ? ref.getFullName() : ref.getIri());
        }
        return null;
    }

    protected void handleCitation(IsCiteable source, HasCitation target) {
        String doi = source.getDoi();
        String citation = source.getHowToCite();
        if (StringUtils.isNotBlank(citation)) {
            target.setCustomCitation(value(citation));
        }
        if (StringUtils.isNotBlank(doi)) {
            final String doiWithoutPrefix = EBRAINSTranslatorUtils.stripDOIPrefix(doi);
            target.setDoi(value(doiWithoutPrefix));
            if (StringUtils.isBlank(citation)) {
                target.setCitation(value(doiWithoutPrefix));
            }
        }
    }

}
