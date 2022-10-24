package eu.ebrains.kg.common.controller.translators.kgv3.helpers;

import eu.ebrains.kg.common.controller.translators.Helpers;
import eu.ebrains.kg.common.controller.translators.TranslatorBase;
import eu.ebrains.kg.common.model.source.openMINDSv3.DatasetVersionV3;
import eu.ebrains.kg.common.model.source.openMINDSv3.commons.FullNameRef;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.DatasetVersion;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.BasicHierarchyElement;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.TargetExternalReference;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.common.utils.IdUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SpecimenV3Translator extends TranslatorBase {

    private static final Map<String, String> UNKNOWN_COLOR_MAP = Map.of("#939393", "Unknown");

    private static final String DESCENDENT_FROM = "descendentFrom";
    private static final String PART_OF = "partOf";

    //fe3da318-c8aa-4c83-ba64-1f0c74b00700 tissue samples with single state attached to subjects with single state
    //0bf058d2-6bf7-4e0f-8067-345e07109bf8 nested tissue sample collections with single states attached to a subject
    //d71d369a-c401-4d7e-b97a-3fb78eed06c5 tissue sample collection from other tissue sample with subselection
    //7f6e14f0-ab5c-4328-9e0e-01b260edd357 tissue sample fully connected to a subject state
    //4660e79b-a731-40ac-905e-46d0d11c0dd5 subjects with two states and attached tissue sample collections
    //77aa9e4c-fd9c-493c-9af4-0988284ffa95 tissue sample collections with service links
    //TODO shall we support the indirect linking of subjects and subject groups or do we require them to be stated explictly?
    //Tissue sample only (indirect link to subjects)
    //89ddf976-e732-4eef-be48-08af62cfe40b TSC
    //65a1a3ca-a8e7-4bc0-a60b-3cffc9122b0e TS
    //51ad4f55-6fe2-4513-85fc-449fead4c998 TSC
    //98541109-aa4a-4813-85e2-2ad915659553
    //df91d605-e261-4c3b-a954-2acd9adec042
    //bb024285-ab31-48fa-86ff-5450b5e64cd7
    //1899b724-1100-43ba-940f-4aab44080e9f
    //0abb83f5-07f1-41b8-bdb3-f245f4c4e673
    //95f44822-6247-4d7a-a232-23a5247dd91d TS in TSC
    //103068ab-8993-4f0c-94a8-b55a6b99f109 2 TS in TSC
    //b4a37f80-e231-4a27-92ca-f47de7b2208d TSC with TS with 2 states
    //1cfea99c-b9ee-4748-b900-6ed6db944435
    //2d3757b5-afc8-470d-988e-f382884cf382
    //62bb226b-47f7-4294-bdff-66662d86e4d5 TS with multiple species and sex


    public SpecimenV3Translator(String datasetVersionId) {
        this.translators =  Arrays.asList(new SubjectTranslator(datasetVersionId), new SubjectGroupTranslator(datasetVersionId), new TissueSampleTranslator(datasetVersionId), new TissueSampleCollectionTranslator(datasetVersionId));
    }

    private final List<SpecimenTranslator<?>> translators;

    private <T> SpecimenTranslator<T> getTranslator(DatasetVersionV3.StudiedSpecimen specimen){
        return (SpecimenTranslator<T>) translators.stream().filter(t -> t.matches(specimen)).findFirst().orElse(null);
    }

    private <T> SpecimenTranslator<T> getTranslator(DatasetVersionV3.StudiedState state){
        return  (SpecimenTranslator<T>) translators.stream().filter(t -> t.matches(state)).findFirst().orElse(null);
    }

    /**
     * This method translates the list of studied specimen into a hierarchy to be represented in the KG Search.
     * The logic contains the following rules:
     *
     * - Root elements of the tree are those instances which are not connected (partOf / descendentFrom) of another instance in the list
     * - The list of studied specimen is taken from the research product version. If a subject group or tissue sample collection
     *   are available and at least one "partOf" instance of them is present in the list, this is interpreted as a "subselection"
     *   and therefore only those instances are shown which are explicitly defined. If no instance with "partOf" relation of a
     *   group/collection is explicitly stated, the group / collection is interpreted to be fully contained and its "partOf"
     *   instances are inferred automatically (if available).
     * - If a specimen only contains a single state, the state is merged into the specimen representation for simplification
     * - Since "descendentFrom" relations are state-to-state relations, this is normally reflected in the hierarchy and the specimen of
     *   the descendent instance is attached below the state (potentially with duplications). If however all states of a tissue sample /
     *   tissue sample collection are descendent from the same subject / subject group state, the specimen is shown BEFORE its states
     *   the reason is that it's a rather common use-case and way more intuitive to interpret. Additionally, the information is still valid
     *   since the tissue sample is in fact descendent from the above state as a whole.
     * - We aggregate some information on the various levels (e.g. the root level aims at giving some key information about the overall specimen)
     */




    public BasicHierarchyElement<DatasetVersion.DSVSpecimenOverview> translateToHierarchy(List<DatasetVersionV3.StudiedSpecimen> studiedSpecimen) {
        if(CollectionUtils.isEmpty(studiedSpecimen)){
            return null;
        }
        BasicHierarchyElement<DatasetVersion.DSVSpecimenOverview> e = new BasicHierarchyElement<>();
        e.setKey("root");
        e.setTitle("Specimen");
        e.setColor("#e3dcdc");
        final Set<String> explicitlyStatedSpecimen = studiedSpecimen.stream().map(DatasetVersionV3.StudiedSpecimen::getId).collect(Collectors.toSet());

        final Stream<DatasetVersionV3.StudiedSpecimen> implicitSubElements = studiedSpecimen.stream().map(DatasetVersionV3.StudiedSpecimen::getSubElements)
                // If one of the sub elements is stated explicitly, we interpret this as a specific subselection for a dataset and are
                // not taking into account any other potential elements which are part of the general collection.
                .filter(subElements -> !CollectionUtils.isEmpty(subElements) && subElements.stream().noneMatch(sub -> explicitlyStatedSpecimen.contains(sub.getId())))
                .flatMap(Collection::stream);
        final List<DatasetVersionV3.StudiedSpecimen> collectedSpecimen = Stream.concat(studiedSpecimen.stream(), implicitSubElements).collect(Collectors.toList());

        //Remove all "isPartOf" relationships for those studiedSpecimen which are not explicitly defined, since we want
        //all tissue sample collections and subject groups to be stated explicitly if they shall be visualized
        collectedSpecimen.forEach(s ->s.getIsPartOf().removeIf(partOf -> !explicitlyStatedSpecimen.contains(partOf)));

        //Also remove all descended from relations which are not resolved (e.g. because its counterpart is not mapped as a study target)
        final Set<String> allStateIds = collectedSpecimen.stream().map(s -> s.getStudiedState().stream().map(DatasetVersionV3.StudiedState::getId).collect(Collectors.toSet())).flatMap(Collection::stream).collect(Collectors.toSet());
        collectedSpecimen.forEach(s -> s.getStudiedState().forEach(st -> st.getDescendedFrom().removeIf(descFrom -> !allStateIds.contains(descFrom))));

        final Map<String, List<DatasetVersionV3.StudiedState>> descendentStates = collectedSpecimen.stream().map(s -> s.getStudiedState().stream().peek(st -> st.setParent(s)).collect(Collectors.toList())).flatMap(Collection::stream).collect(Collectors.toMap(DatasetVersionV3.StudiedState::getId, v -> findDescendentStates(v, collectedSpecimen), (s1, s2) -> s1));
        final Map<String, List<DatasetVersionV3.StudiedSpecimen>> partOfRelations = collectedSpecimen.stream().collect(Collectors.toMap(DatasetVersionV3.StudiedSpecimen::getId, v -> findPartOfRelations(v, collectedSpecimen), (s1, s2) -> s1));
        final GlobalTranslationContext translationContext = new GlobalTranslationContext(descendentStates, partOfRelations);

        //Start the recursive resolution with the root elements (the ones that either don't have states at all or whose states are disconnected)
        e.setChildren(studiedSpecimen.stream().filter(s -> CollectionUtils.isEmpty(s.getIsPartOf()) && s.getStudiedState().stream().allMatch(st -> CollectionUtils.isEmpty(st.getDescendedFrom()))).map(s -> translateToBasicHierarchyElement(s, translationContext, false, null, null)).sorted(Comparator.comparing(BasicHierarchyElement::getTitle)).collect(Collectors.toList()));
        e.setLegend(sortLegend(collectLegend(e, new HashMap<>())));
        e.setData(translationContext.overviewAggregator.flush());
        return e;
    }


    private List<DatasetVersionV3.StudiedState> findDescendentStates(DatasetVersionV3.StudiedState root, List<DatasetVersionV3.StudiedSpecimen> studiedSpecimen) {
        return studiedSpecimen.stream().map(DatasetVersionV3.StudiedSpecimen::getStudiedState).flatMap(Collection::stream).filter(st -> st.getDescendedFrom().contains(root.getId())).distinct().collect(Collectors.toList());
    }

    private List<DatasetVersionV3.StudiedSpecimen> findPartOfRelations(DatasetVersionV3.StudiedSpecimen root, List<DatasetVersionV3.StudiedSpecimen> studiedSpecimen) {
        return studiedSpecimen.stream().filter(s -> s.getIsPartOf().contains(root.getId())).collect(Collectors.toList());
    }

    private static FullNameRef undefinedFullnameRef(){
        final FullNameRef fullNameRef = new FullNameRef();
        fullNameRef.setFullName("Undefined");
        return fullNameRef;
    }

    private static String getLabel(int index){
        int base = 'A';
        //TODO fix for index > 26
        return String.valueOf((char)(base+index));
    }

    private static String getFullStateLabel(SpecimenTranslator translator, String label, DatasetVersionV3.StudiedState state){
        final String parentLabel = translator.calculateLabel(state.getParent());
        return String.format("State %s of %s", label, StringUtils.uncapitalize(StringUtils.defaultString(StringUtils.trimToNull(state.getParent().getInternalIdentifier()), parentLabel)));
    }



    private static class GlobalTranslationContext{
        private final Stack<String> handledInstances = new Stack<>();
        private final Map<String, List<DatasetVersionV3.StudiedState>> descendentStatesMap;
        private final Map<String, List<DatasetVersionV3.StudiedSpecimen>> partOfRelations;
        private final DatasetVersion.DSVSpecimenOverview overviewAggregator = new DatasetVersion.DSVSpecimenOverview();

        private final List<TargetInternalReference> anatomicalLocationsOfTissueSamples = new ArrayList<>();

        public GlobalTranslationContext(Map<String, List<DatasetVersionV3.StudiedState>> descendentStatesMap, Map<String, List<DatasetVersionV3.StudiedSpecimen>> partOfRelations) {
            this.descendentStatesMap = descendentStatesMap;
            this.partOfRelations = partOfRelations;
        }
    }

    private BasicHierarchyElement<Object> translateToBasicHierarchyElement(DatasetVersionV3.StudiedState studiedState, GlobalTranslationContext context, boolean attachRootElementAsChild, int order, String parentRelationType){
        final String uuid = IdUtils.getUUID(studiedState.getId());
        if(context.handledInstances.contains(uuid)){
            logger.error("Circular reference detected - breaking at instance {}", uuid);
            return null;
        }
        BasicHierarchyElement<Object> elState = new BasicHierarchyElement<>();
        elState.setKey(UUID.randomUUID().toString());
        context.handledInstances.push(uuid);
        elState.setParentRelationType(parentRelationType);
        final SpecimenTranslator<?> translator = getTranslator(studiedState);
        if(translator!=null){
            elState.setColor(translator.stateColor);
            final String parentLabel = translator.calculateLabel(studiedState.getParent());
            String label;
            if(attachRootElementAsChild && StringUtils.isNotBlank(parentLabel)){
                label = getFullStateLabel(translator, getLabel(order), studiedState);
            }
            else{
                label = String.format("State %s", getLabel(order));
            }
            elState.setTitle(label);
            elState.setData(translator.translateState(studiedState, label));
            final List<DatasetVersionV3.StudiedState> descendentStates = context.descendentStatesMap.get(studiedState.getId());
            List<BasicHierarchyElement<?>> children = new ArrayList<>();
            if(!CollectionUtils.isEmpty(descendentStates)){
                //There are descendent states. For those which are completely connected to the current state, we can visualize them more naturally by attaching the specimen directly to the state (since it's true that the whole specimen is descendent from this state).
                final List<DatasetVersionV3.StudiedSpecimen> fullyEnclosedDescendentSpecimen = descendentStates.stream().map(DatasetVersionV3.StudiedState::getParent).distinct().filter(d -> new HashSet<>(descendentStates).containsAll(d.getStudiedState())).collect(Collectors.toList());
                final Set<DatasetVersionV3.StudiedState> fullyEnclosedDescendentStates = fullyEnclosedDescendentSpecimen.stream().map(DatasetVersionV3.StudiedSpecimen::getStudiedState).flatMap(Collection::stream).collect(Collectors.toSet());
                children.addAll(fullyEnclosedDescendentSpecimen.stream().map(s->translateToBasicHierarchyElement(s, context, false, DESCENDENT_FROM, null)).filter(Objects::nonNull).collect(Collectors.toList()));

                //Let's also add those which are not fully connected by attaching their state first.
                final List<DatasetVersionV3.StudiedState> incompleteDescendentStates = descendentStates.stream().filter(d -> !fullyEnclosedDescendentStates.contains(d)).collect(Collectors.toList());
                children.addAll(IntStream.range(0, incompleteDescendentStates.size()).mapToObj(idx -> translateToBasicHierarchyElement(incompleteDescendentStates.get(idx), context, true, idx, DESCENDENT_FROM)).filter(Objects::nonNull).collect(Collectors.toList()));
            }

            if (attachRootElementAsChild && studiedState.getParent() != null) {
                final BasicHierarchyElement<Object> parent = translateToBasicHierarchyElement(studiedState.getParent(), context, true, PART_OF, null);
                    if(parent!=null){
                        if (canMergeHierarchyElements(studiedState.getParent())) {
                            elState = mergeBasicHierarchyElements(parent, elState, children);
                        } else {
                            children.add(parent);
                        }
                    }
            }
            if (!CollectionUtils.isEmpty(children)) {
                children.sort(Comparator.comparing(BasicHierarchyElement::getTitle));
                elState.setChildren(children);
            }
        }
        context.handledInstances.pop();
        return elState;
    }

    // If there is only a single state available for this studied specimen, we can simplify the structure by merging it.
    private boolean canMergeHierarchyElements(DatasetVersionV3.StudiedSpecimen specimen){
        return specimen.getStudiedState().size() == 1;
    }

    private <T> BasicHierarchyElement<T> mergeBasicHierarchyElements(BasicHierarchyElement<T> parent, BasicHierarchyElement<?> child, List<BasicHierarchyElement<?>> children){
        BasicHierarchyElement<T> el = new BasicHierarchyElement<>();
        el.setKey(parent.getKey());
        //If we can merge an element, it means that there is only a single state and we therefore don't need a "state label" to distinguish. We therefore can just use the parent label.
        el.setTitle(parent.getTitle());
        el.setColor(parent.getColor());
        el.setData(parent.getData());
        el.setParentRelationType(parent.getParentRelationType());
        if(parent.getChildren()!=null){
            children.addAll(parent.getChildren().stream().filter(c -> !children.contains(c)).collect(Collectors.toList()));
        }
        if(child.getChildren()!=null){
            children.addAll(child.getChildren().stream().filter(c -> !children.contains(c)).collect(Collectors.toList()));
        }
        return el;
    }


    private static abstract class SpecimenTranslator<T> extends TranslatorBase{
        protected final String prefix;
        private final String specimenType;
        private final String specimenColor;
        private final String stateType;
        private final String stateColor;

        protected final String datasetVersionId;

        public SpecimenTranslator(String prefix, String specimenType, String specimenColor, String stateType, String stateColor, String datasetVersionId) {
            this.prefix = prefix;
            this.specimenType = specimenType;
            this.specimenColor = specimenColor;
            this.stateType = stateType;
            this.stateColor = stateColor;
            this.datasetVersionId = datasetVersionId;
        }

        public abstract T translateSpecimen(DatasetVersionV3.StudiedSpecimen specimen, DatasetVersionV3.StudiedSpecimen parentSpecimen);

        public abstract Object translateState(DatasetVersionV3.StudiedState state, String label);

        public abstract void aggregateOverview(T source, DatasetVersion.DSVSpecimenOverview overviewAggregator);

        public void flush(T data, BasicHierarchyElement<?> element){}

        public boolean matches(DatasetVersionV3.StudiedSpecimen specimen){
            return specimen.getType().contains(specimenType);
        }

        public boolean matches(DatasetVersionV3.StudiedState state){
            return state.getType().contains(stateType);
        }

        public String calculateLabel(DatasetVersionV3.StudiedSpecimen specimen){
            return String.format("%s%s", prefix!=null ? String.format("%s ", prefix) : "", StringUtils.defaultString(StringUtils.trimToNull(specimen.getInternalIdentifier()), specimen.getLookupLabel()));
        }
    }

    private static List<TargetExternalReference> translateServiceLinks(List<DatasetVersionV3.SpecimenServiceLinkCollection> serviceLinks, List<TargetExternalReference> existingLinks){
        final Stream<TargetExternalReference> fromFiles = serviceLinks.stream().flatMap(s -> Stream.concat(s.getFromFile().stream(), s.getFromFileBundle().stream())).map(s -> new TargetExternalReference(s.getOpenDataIn(), s.displayLabel()));
        Stream<TargetExternalReference> referenceStream;
        if(!CollectionUtils.isEmpty(existingLinks)){
            referenceStream = Stream.concat(existingLinks.stream(), fromFiles);
        }
        else{
            referenceStream = fromFiles;
        }
        final List<TargetExternalReference> results = referenceStream.distinct().sorted(Comparator.comparing(TargetExternalReference::getValue)).collect(Collectors.toList());
        return CollectionUtils.isEmpty(results) ? null : results;
    }

    private static class SubjectTranslator extends SpecimenTranslator<DatasetVersion.DSVSubject> {

        public SubjectTranslator(String datasetVersionId) {
            super("Subject", "https://openminds.ebrains.eu/core/Subject", "#ffbe00", "https://openminds.ebrains.eu/core/SubjectState", "#e68d0d", datasetVersionId);
        }

        @Override
        public DatasetVersion.DSVSubject translateSpecimen(DatasetVersionV3.StudiedSpecimen specimen, DatasetVersionV3.StudiedSpecimen parentSpecimen) {
            final DatasetVersion.DSVSubject sub = new DatasetVersion.DSVSubject();
            sub.setTitle(value(specimen.getInternalIdentifier()));
            sub.setId(IdUtils.getUUID(specimen.getId()));
            fillSubjectInformation(sub, specimen, parentSpecimen);
            if(specimen.getStudiedState().size()==1){
                // The single state subjects will be merged -> we therefore add those properties which are state specific
                // directly to the subject
                fillSubjectStateInformation(sub, specimen.getStudiedState().get(0));
            }
            else{
                // We can aggregate the age categories since they are required for every state (the "undefined" state is therefore just to be sure)
                sub.setAgeCategory(ref(specimen.getStudiedState().stream().flatMap(ss -> {
                    if (CollectionUtils.isEmpty(ss.getAgeCategory())) {
                        return Stream.of(undefinedFullnameRef());
                    }
                    return ss.getAgeCategory().stream().map(a -> a == null ? undefinedFullnameRef() : a);
                }).distinct().collect(Collectors.toList())));
            }
            return sub;
        }

        @Override
        public Object translateState(DatasetVersionV3.StudiedState state, String label) {
            final DatasetVersion.DSVSubjectState subState = new DatasetVersion.DSVSubjectState();
            subState.setTitle(value(getFullStateLabel(this, label, state)));
            fillSubjectInformation(subState, state.getParent(), null);
            fillSubjectStateInformation(subState, state);
            return subState;
        }

        @Override
        public void aggregateOverview(DatasetVersion.DSVSubject source, DatasetVersion.DSVSpecimenOverview overviewAggregator){
            overviewAggregator.getSubjectIds().add(source.getId());
            overviewAggregator.collectSex(source.getId(), source.getSex(), this.prefix);
            overviewAggregator.collectStrains(source.getId(), source.getStrain(), this.prefix);
            overviewAggregator.collectSpecies(source.getId(), source.getSpecies(), this.prefix);
            overviewAggregator.collectGeneticStrainTypes(source.getId(), source.getGeneticStrainType(), this.prefix);
            overviewAggregator.collectPathology(source.getId(), source.getPathology(), this.prefix);
        }

        private void fillSubjectStateInformation(DatasetVersion.DSVSubject sub, DatasetVersionV3.StudiedState state){
            sub.setServiceLinks(translateServiceLinks(state.getServiceLinks(), sub.getServiceLinks()));
            sub.setAdditionalRemarks(value(state.getAdditionalRemarks()));
            sub.setAttributes(ref(state.getAttribute()));
            sub.setPathology(ref(state.getPathology()));
            sub.setHandedness(ref(state.getHandedness()));
            sub.setWeight(value(state.getWeight()!=null ? state.getWeight().displayString() : null));
            sub.setAge(value(state.getAge()!=null ? state.getAge().displayString() : null));
            if (!CollectionUtils.isEmpty(state.getAgeCategory())) {
                sub.setAgeCategory(ref(state.getAgeCategory()));
            }
        }

        private void fillSubjectInformation(DatasetVersion.DSVSubject sub, DatasetVersionV3.StudiedSpecimen specimen,  DatasetVersionV3.StudiedSpecimen parentSpecimen){
            sub.setServiceLinks(translateServiceLinks(specimen.getServiceLinks(), sub.getServiceLinks()));
            sub.setSex(ref(specimen.getBiologicalSex()));
            sub.setOtherPublications(translateOtherPublications(specimen, datasetVersionId, parentSpecimen));
            List<FullNameRef> indirectSpecies = specimen.getSpecies() != null ? specimen.getSpecies().stream().map(DatasetVersionV3.SpeciesOrStrain::getSpecies).filter(Objects::nonNull).collect(Collectors.toList()) : Collections.emptyList();
            sub.setGeneticStrainType(ref(CollectionUtils.isEmpty(specimen.getSpecies()) ? null : specimen.getSpecies().stream().map(DatasetVersionV3.SpeciesOrStrain::getGeneticStrainType).filter(Objects::nonNull).distinct().collect(Collectors.toList())));
            sub.setSpecies(ref(CollectionUtils.isEmpty(indirectSpecies) ? specimen.getSpecies() : indirectSpecies));
            //If there are indirect species, the first-level species is a strain
            sub.setStrain(ref(CollectionUtils.isEmpty(indirectSpecies) ? null : specimen.getSpecies()));
        }
    }

    private static List<TargetInternalReference> translateOtherPublications(DatasetVersionV3.StudiedSpecimen specimen, String datasetVersionId, DatasetVersionV3.StudiedSpecimen parentSpecimen){
        Set<DatasetVersionV3.RelatedProducts> allRelatedProducts = new HashSet<>();
        if(parentSpecimen!=null && !CollectionUtils.isEmpty(parentSpecimen.getUsedInDatasets())){
            for (DatasetVersionV3.RelatedProducts usedInDataset : parentSpecimen.getUsedInDatasets()) {
                if(CollectionUtils.isEmpty(usedInDataset.getGroupsWithIndividualSubElementSpecification()) || !usedInDataset.getGroupsWithIndividualSubElementSpecification().contains(parentSpecimen.getId())){
                   //The parent group doesn't have any additional restrictions by sub element. We therefore can interpret this specimen to be member of the related product by group association.
                    allRelatedProducts.add(usedInDataset);
                }
            }
        }
        //Additionally to the (potential) implicit links, we of course also take the explicit references into account
        if(!CollectionUtils.isEmpty(specimen.getUsedInDatasets())){
            allRelatedProducts.addAll(specimen.getUsedInDatasets());
        }
        final List<TargetInternalReference> otherPublications = allRelatedProducts.stream().filter(i -> !i.getId().equals(datasetVersionId) && i.getId()!=null && i.getDoi()!=null).map(i ->
                new TargetInternalReference(IdUtils.getUUID(i.getId()), Helpers.stripDOIPrefix(i.getDoi()), new TargetInternalReference.Context("Specimen", IdUtils.getUUID(specimen.getId())))).sorted(Comparator.comparing(TargetInternalReference::getValue)).collect(Collectors.toList());
        if(!CollectionUtils.isEmpty(otherPublications)) {
            return otherPublications;
        }
        else{
            return null;
        }
    }

    private static class SubjectGroupTranslator extends SpecimenTranslator<DatasetVersion.DSVSubjectGroup>{

        public SubjectGroupTranslator(String datasetVersionId) {
            super("Subject group", "https://openminds.ebrains.eu/core/SubjectGroup", "#8a1f0d", "https://openminds.ebrains.eu/core/SubjectGroupState", "#8a1f0d", datasetVersionId);
        }

        @Override
        public DatasetVersion.DSVSubjectGroup translateSpecimen(DatasetVersionV3.StudiedSpecimen specimen, DatasetVersionV3.StudiedSpecimen parentSpecimen) {
            final DatasetVersion.DSVSubjectGroup subGrp = new DatasetVersion.DSVSubjectGroup();
            subGrp.setId(IdUtils.getUUID(specimen.getId()));
            subGrp.setTitle(value(specimen.getInternalIdentifier()));
            subGrp.setOtherPublications(translateOtherPublications(specimen, datasetVersionId, parentSpecimen));
            if(specimen.getQuantity()!=null){
                subGrp.setNumberOfSubjects(value(String.valueOf(specimen.getQuantity())));
            }
            fillSubjectGroupInformation(subGrp, specimen);
            if(specimen.getStudiedState().size()==1) {
                fillSubjectGroupStateInformation(subGrp, specimen.getStudiedState().get(0));
            }
            return subGrp;
        }

        @Override
        public void aggregateOverview(DatasetVersion.DSVSubjectGroup source, DatasetVersion.DSVSpecimenOverview overviewAggregator) {
            overviewAggregator.getSubjectGroupIds().add(source.getId());
            overviewAggregator.collectSex(source.getId(), source.getSex(), this.prefix);
            overviewAggregator.collectStrains(source.getId(), source.getStrain(), this.prefix);
            overviewAggregator.collectSpecies(source.getId(), source.getSpecies(), this.prefix);
            overviewAggregator.collectGeneticStrainTypes(source.getId(), source.getGeneticStrainType(), this.prefix);
            overviewAggregator.collectPathology(source.getId(), source.getPathology(), this.prefix);
        }

        @Override
        public Object translateState(DatasetVersionV3.StudiedState state, String label) {
            final DatasetVersion.DSVSubjectGroupState subGrp = new DatasetVersion.DSVSubjectGroupState();
            subGrp.setTitle(value(state.getParent().getInternalIdentifier()));
            subGrp.setServiceLinks(translateServiceLinks(state.getServiceLinks(), subGrp.getServiceLinks()));
            fillSubjectGroupInformation(subGrp, state.getParent());
            fillSubjectGroupStateInformation(subGrp, state);
            return subGrp;
        }

        @Override
        public void flush(DatasetVersion.DSVSubjectGroup data, BasicHierarchyElement<?> element) {
            if(!CollectionUtils.isEmpty(element.getChildren()) && data.getNumberOfSubjects()!=null){
                final long count = element.getChildren().stream().filter(c -> c.getParentRelationType()!=null && c.getParentRelationType().equals(PART_OF)).count();
                final String quantity = data.getNumberOfSubjects().getValue();
                if(quantity!=null && count>0 && !String.valueOf(count).equals(quantity)){
                    data.setNumberOfSubjects(value(String.format("%d of %s used in this dataset", count, quantity)));
                }
            }
        }

        private void fillSubjectGroupStateInformation(DatasetVersion.DSVSubjectGroup subGrp, DatasetVersionV3.StudiedState state){
            subGrp.setServiceLinks(translateServiceLinks(state.getServiceLinks(), subGrp.getServiceLinks()));
            if(subGrp.getAdditionalRemarks()!=null && StringUtils.isNotBlank(state.getAdditionalRemarks())){
                //Merged additional remarks
                subGrp.setAdditionalRemarks(value(StringUtils.joinWith("\n\n", subGrp.getAdditionalRemarks(), state.getAdditionalRemarks())));
            }
            else {
                subGrp.setAdditionalRemarks(value(state.getAdditionalRemarks()));
            }
            subGrp.setAttributes(ref(state.getAttribute()));
            subGrp.setPathology(ref(state.getPathology()));
            subGrp.setHandedness(ref(state.getHandedness()));
            subGrp.setWeight(value(state.getWeight()!=null ? state.getWeight().displayString() : null));
            subGrp.setAge(value(state.getAge()!=null ? state.getAge().displayString() : null));
            if (!CollectionUtils.isEmpty(state.getAgeCategory())) {
                subGrp.setAgeCategory(ref(state.getAgeCategory()));
            }
        }

        private void fillSubjectGroupInformation(DatasetVersion.DSVSubjectGroup subGrp, DatasetVersionV3.StudiedSpecimen specimen){
            subGrp.setServiceLinks(translateServiceLinks(specimen.getServiceLinks(), subGrp.getServiceLinks()));
            subGrp.setSex(ref(specimen.getBiologicalSex()));
            List<FullNameRef> indirectSpecies = specimen.getSpecies() != null ? specimen.getSpecies().stream().map(DatasetVersionV3.SpeciesOrStrain::getSpecies).filter(Objects::nonNull).collect(Collectors.toList()) : Collections.emptyList();
            subGrp.setGeneticStrainType(ref(CollectionUtils.isEmpty(specimen.getSpecies()) ? null : specimen.getSpecies().stream().map(DatasetVersionV3.SpeciesOrStrain::getGeneticStrainType).filter(Objects::nonNull).distinct().collect(Collectors.toList())));
            subGrp.setSpecies(ref(CollectionUtils.isEmpty(indirectSpecies) ? specimen.getSpecies() : indirectSpecies));
            //If there are indirect species, the first-level species is a strain
            subGrp.setStrain(ref(CollectionUtils.isEmpty(indirectSpecies) ? null : specimen.getSpecies()));
            subGrp.setAdditionalRemarks(value(specimen.getAdditionalRemarks()));
        }
    }

    private static class TissueSampleTranslator extends SpecimenTranslator<DatasetVersion.DSVTissueSample> {

        public TissueSampleTranslator(String datasetVersionId) {
            super("Tissue sample", "https://openminds.ebrains.eu/core/TissueSample", "#3176e1", "https://openminds.ebrains.eu/core/TissueSampleState", "#393ac6", datasetVersionId);
        }

        @Override
        public DatasetVersion.DSVTissueSample translateSpecimen(DatasetVersionV3.StudiedSpecimen specimen, DatasetVersionV3.StudiedSpecimen parentSpecimen) {
            final DatasetVersion.DSVTissueSample tissueSample = new DatasetVersion.DSVTissueSample();
            tissueSample.setId(IdUtils.getUUID(specimen.getId()));
            tissueSample.setTitle(value(specimen.getInternalIdentifier()));
            fillTissueSampleInformation(tissueSample, specimen);
            if(specimen.getStudiedState().size()==1) {
                fillTissueSampleStateInformation(tissueSample, specimen.getStudiedState().get(0));
            }
            return tissueSample;
        }

        private void fillTissueSampleInformation(DatasetVersion.DSVTissueSample tissueSample, DatasetVersionV3.StudiedSpecimen specimen){
            tissueSample.setTissueSampleType(ref(specimen.getTissueSampleType()));
            tissueSample.setServiceLinks(translateServiceLinks(specimen.getServiceLinks(), tissueSample.getServiceLinks()));
            tissueSample.setSex(ref(specimen.getBiologicalSex()));
            tissueSample.setAnatomicalLocation(refAnatomical(specimen.getAnatomicalLocation(), true));
            tissueSample.setLaterality(ref(specimen.getLaterality()));
            List<FullNameRef> indirectSpecies = specimen.getSpecies() != null ? specimen.getSpecies().stream().map(DatasetVersionV3.SpeciesOrStrain::getSpecies).filter(Objects::nonNull).collect(Collectors.toList()) : Collections.emptyList();
            tissueSample.setGeneticStrainType(ref(CollectionUtils.isEmpty(specimen.getSpecies()) ? null : specimen.getSpecies().stream().map(DatasetVersionV3.SpeciesOrStrain::getGeneticStrainType).filter(Objects::nonNull).distinct().collect(Collectors.toList())));
            tissueSample.setSpecies(ref(CollectionUtils.isEmpty(indirectSpecies) ? specimen.getSpecies() : indirectSpecies));
            //If there are indirect species, the first-level species is a strain
            tissueSample.setStrain(ref(CollectionUtils.isEmpty(indirectSpecies) ? null : specimen.getSpecies()));
            tissueSample.setOrigin(ref(specimen.getOrigin()));
            tissueSample.setAdditionalRemarks(value(specimen.getAdditionalRemarks()));
        }

        private void fillTissueSampleStateInformation(DatasetVersion.DSVTissueSample tissueSample, DatasetVersionV3.StudiedState state){
            if(tissueSample.getAdditionalRemarks()!=null && StringUtils.isNotBlank(state.getAdditionalRemarks())){
                //Merged additional remarks
                tissueSample.setAdditionalRemarks(value(StringUtils.joinWith("\n\n", tissueSample.getAdditionalRemarks(), state.getAdditionalRemarks())));
            }
            else {
                tissueSample.setAdditionalRemarks(value(state.getAdditionalRemarks()));
            }
            tissueSample.setAdditionalRemarks(value(state.getAdditionalRemarks()));
            tissueSample.setAttributes(ref(state.getAttribute()));
            tissueSample.setServiceLinks(translateServiceLinks(state.getServiceLinks(), tissueSample.getServiceLinks()));
            tissueSample.setPathology(ref(state.getPathology()));
            tissueSample.setWeight(value(state.getWeight()!=null ? state.getWeight().displayString() : null));
            tissueSample.setAge(value(state.getAge()!=null ? state.getAge().displayString() : null));
            if (!CollectionUtils.isEmpty(state.getAgeCategory())) {
                tissueSample.setAgeCategory(ref(state.getAgeCategory()));
            }
        }

        @Override
        public void aggregateOverview(DatasetVersion.DSVTissueSample source, DatasetVersion.DSVSpecimenOverview overviewAggregator) {
            overviewAggregator.getTissueSampleIds().add(source.getId());
            overviewAggregator.collectSex(source.getId(), source.getSex(), this.prefix);
            overviewAggregator.collectStrains(source.getId(), source.getStrain(), this.prefix);
            overviewAggregator.collectSpecies(source.getId(), source.getSpecies(), this.prefix);
            overviewAggregator.collectGeneticStrainTypes(source.getId(), source.getGeneticStrainType(), this.prefix);
            overviewAggregator.collectPathology(source.getId(), source.getPathology(), this.prefix);
            if(source.getAnatomicalLocation()!=null) {
                overviewAggregator.getAnatomicalLocationsOfTissueSamples().addAll(source.getAnatomicalLocation());
            }
        }

        @Override
        public Object translateState(DatasetVersionV3.StudiedState state, String label) {
            final DatasetVersion.DSVTissueSampleState tissueSampleState = new DatasetVersion.DSVTissueSampleState();
            tissueSampleState.setTitle(value(getFullStateLabel(this, label, state)));
            fillTissueSampleInformation(tissueSampleState, state.getParent());
            fillTissueSampleStateInformation(tissueSampleState, state);
            return tissueSampleState;
        }
    }

    private static class TissueSampleCollectionTranslator extends SpecimenTranslator<DatasetVersion.DSVTissueSampleCollection> {

        public TissueSampleCollectionTranslator(String datasetVersionId) {
            super("Tissue sample collection", "https://openminds.ebrains.eu/core/TissueSampleCollection", "#78b5b5", "https://openminds.ebrains.eu/core/TissueSampleCollectionState", "#497d7d", datasetVersionId);
        }

        @Override
        public DatasetVersion.DSVTissueSampleCollection translateSpecimen(DatasetVersionV3.StudiedSpecimen specimen, DatasetVersionV3.StudiedSpecimen parentSpecimen) {
            final DatasetVersion.DSVTissueSampleCollection tissueSampleColl = new DatasetVersion.DSVTissueSampleCollection();
            tissueSampleColl.setId(IdUtils.getUUID(specimen.getId()));
            tissueSampleColl.setTitle(value(specimen.getInternalIdentifier()));
            if(specimen.getQuantity()!=null){
                tissueSampleColl.setTissueSamples(value(String.valueOf(specimen.getQuantity())));
            }
            fillTissueSampleCollectionInformation(tissueSampleColl, specimen);
            if(specimen.getStudiedState().size()==1) {
                fillTissueSampleCollectionStateInformation(tissueSampleColl, specimen.getStudiedState().get(0));
            }
            return tissueSampleColl;
        }

        @Override
        public void flush(DatasetVersion.DSVTissueSampleCollection data, BasicHierarchyElement<?> element) {
            if(!CollectionUtils.isEmpty(element.getChildren()) && data.getTissueSamples()!=null){
                final long count = element.getChildren().stream().filter(c -> c.getParentRelationType()!=null && c.getParentRelationType().equals(PART_OF)).count();
                final String quantity = data.getTissueSamples().getValue();
                if(quantity!=null && count>0 && !String.valueOf(count).equals(quantity)){
                    data.setTissueSamples(value(String.format("total: %s, used in this dataset: %d", quantity, count)));
                }
            }
        }

        @Override
        public void aggregateOverview(DatasetVersion.DSVTissueSampleCollection source, DatasetVersion.DSVSpecimenOverview overviewAggregator) {
            overviewAggregator.getTissueSampleCollectionIds().add(source.getId());
            overviewAggregator.collectSex(source.getId(), source.getSex(), this.prefix);
            overviewAggregator.collectStrains(source.getId(), source.getStrain(), this.prefix);
            overviewAggregator.collectSpecies(source.getId(), source.getSpecies(), this.prefix);
            overviewAggregator.collectGeneticStrainTypes(source.getId(), source.getGeneticStrainType(), this.prefix);
            overviewAggregator.collectPathology(source.getId(), source.getPathology(), this.prefix);
            if(source.getAnatomicalLocation()!=null) {
                overviewAggregator.getAnatomicalLocationsOfTissueSamples().addAll(source.getAnatomicalLocation());
            }
        }

        @Override
        public Object translateState(DatasetVersionV3.StudiedState state, String label) {
            final DatasetVersion.DSVTissueSampleCollectionState tissueSampleCollState = new DatasetVersion.DSVTissueSampleCollectionState();
            tissueSampleCollState.setTitle(value(getFullStateLabel(this, label, state)));
            fillTissueSampleCollectionInformation(tissueSampleCollState, state.getParent());
            fillTissueSampleCollectionStateInformation(tissueSampleCollState, state);
            return tissueSampleCollState;
        }

        private void fillTissueSampleCollectionInformation(DatasetVersion.DSVTissueSampleCollection tissueSampleCollection, DatasetVersionV3.StudiedSpecimen specimen){
            tissueSampleCollection.setTissueSampleType(ref(specimen.getTissueSampleType()));
            tissueSampleCollection.setSex(ref(specimen.getBiologicalSex()));
            tissueSampleCollection.setServiceLinks(translateServiceLinks(specimen.getServiceLinks(), tissueSampleCollection.getServiceLinks()));
            tissueSampleCollection.setAnatomicalLocation(refAnatomical(specimen.getAnatomicalLocation(), true));
            tissueSampleCollection.setLaterality(ref(specimen.getLaterality()));
            List<FullNameRef> indirectSpecies = specimen.getSpecies() != null ? specimen.getSpecies().stream().map(DatasetVersionV3.SpeciesOrStrain::getSpecies).filter(Objects::nonNull).collect(Collectors.toList()) : Collections.emptyList();
            tissueSampleCollection.setGeneticStrainType(ref(CollectionUtils.isEmpty(specimen.getSpecies()) ? null : specimen.getSpecies().stream().map(DatasetVersionV3.SpeciesOrStrain::getGeneticStrainType).filter(Objects::nonNull).distinct().collect(Collectors.toList())));
            tissueSampleCollection.setSpecies(ref(CollectionUtils.isEmpty(indirectSpecies) ? specimen.getSpecies() : indirectSpecies));
            //If there are indirect species, the first-level species is a strain
            tissueSampleCollection.setStrain(ref(CollectionUtils.isEmpty(indirectSpecies) ? null : specimen.getSpecies()));
            tissueSampleCollection.setOrigin(ref(specimen.getOrigin()));
        }

        private void fillTissueSampleCollectionStateInformation(DatasetVersion.DSVTissueSampleCollection tissueSampleCollectionState, DatasetVersionV3.StudiedState state){
            tissueSampleCollectionState.setAdditionalRemarks(value(state.getAdditionalRemarks()));
            tissueSampleCollectionState.setAttributes(ref(state.getAttribute()));
            tissueSampleCollectionState.setPathology(ref(state.getPathology()));
            tissueSampleCollectionState.setServiceLinks(translateServiceLinks(state.getServiceLinks(), tissueSampleCollectionState.getServiceLinks()));
            tissueSampleCollectionState.setWeight(value(state.getWeight()!=null ? state.getWeight().displayString() : null));
            tissueSampleCollectionState.setAge(value(state.getAge()!=null ? state.getAge().displayString() : null));
            if (!CollectionUtils.isEmpty(state.getAgeCategory())) {
                tissueSampleCollectionState.setAgeCategory(ref(state.getAgeCategory()));
            }
        }
    }


    private <T> BasicHierarchyElement<Object> translateToBasicHierarchyElement(DatasetVersionV3.StudiedSpecimen studiedSpecimen, GlobalTranslationContext context, boolean attachRootElementAsChild, String parentRelationType, DatasetVersionV3.StudiedSpecimen parentSpecimen){
        final String uuid = IdUtils.getUUID(studiedSpecimen.getId());
        if(context.handledInstances.contains(uuid)){
            logger.error("Circular reference detected - breaking at instance {}", uuid);
            return null;
        }
        context.handledInstances.push(uuid);
        BasicHierarchyElement<Object> el = new BasicHierarchyElement<>();
        logger.debug("translating {}", studiedSpecimen.getId());
        el.setKey(UUID.randomUUID().toString());
        el.setParentRelationType(parentRelationType);
        final SpecimenTranslator<T> specimenTranslator = getTranslator(studiedSpecimen);
        T data = null;
        if(specimenTranslator!=null){
            el.setColor(specimenTranslator.specimenColor);
            el.setTitle(specimenTranslator.calculateLabel(studiedSpecimen));
            data = specimenTranslator.translateSpecimen(studiedSpecimen, parentSpecimen);
            specimenTranslator.aggregateOverview(data, context.overviewAggregator);
        }
        List<BasicHierarchyElement<?>> children = new ArrayList<>();
        final List<DatasetVersionV3.StudiedSpecimen> partOfRelation = context.partOfRelations.get(studiedSpecimen.getId());
        if(!CollectionUtils.isEmpty(partOfRelation)){
            children.addAll(partOfRelation.stream().map(p -> translateToBasicHierarchyElement(p, context, false, PART_OF, studiedSpecimen)).filter(Objects::nonNull).collect(Collectors.toList()));
        }

        if(!attachRootElementAsChild){
            if(canMergeHierarchyElements(studiedSpecimen)){
                final BasicHierarchyElement<Object> singleState = translateToBasicHierarchyElement(studiedSpecimen.getStudiedState().get(0), context, false, 0, DESCENDENT_FROM);
                if(singleState!=null){
                    el = mergeBasicHierarchyElements(el, singleState, children);
                }
            }
            else {
                children.addAll(IntStream.range(0, studiedSpecimen.getStudiedState().size()).mapToObj(idx -> translateToBasicHierarchyElement(studiedSpecimen.getStudiedState().get(idx), context, false, idx, DESCENDENT_FROM)).filter(Objects::nonNull).collect(Collectors.toList()));
            }
        }
        if(!CollectionUtils.isEmpty(children)){
            children.sort(Comparator.nullsFirst(Comparator.comparing(BasicHierarchyElement::getTitle)));
            el.setChildren(children);
        }
        if(specimenTranslator!=null && data!=null){
            specimenTranslator.flush(data, el);
            el.setData(data);
        }
        context.handledInstances.pop();
        return el;
    }


    private String findLabelForColor(String color){
        if(color!=null) {
            final SpecimenTranslator<?> translatorByColor = translators.stream().filter(f -> f.stateColor.equals(color) || f.specimenColor.equals(color)).findFirst().orElse(null);
            if(translatorByColor!=null){
                if(translatorByColor.specimenColor.equals(color)){
                    return translatorByColor.prefix;
                }
                else if (translatorByColor.stateColor.equals(color)){
                    return String.format("%s state", translatorByColor.prefix);
                }
            }
        }
        return null;
    }

    private Map<String, String> sortLegend(Map<String, String> legend){
        Map<String, String> sortedMap = new LinkedHashMap<>();
        legend.values().stream().sorted().forEachOrdered(v -> {
            legend.keySet().stream().filter(k -> legend.get(k).equals(v)).findFirst().ifPresent(key -> sortedMap.put(key, legend.get(key)));
        });
        return sortedMap;

    }

    private Map<String, String> collectLegend(BasicHierarchyElement<?> el, Map<String, String> collector){
        final String color = el.getColor();
        if(color!=null) {
            final String labelForColor = findLabelForColor(color);
            if(labelForColor!=null) {
                collector.put(color, labelForColor);
            }
            if (el.getChildren() != null) {
                el.getChildren().forEach(e -> collectLegend(e, collector));
            }
        }
        return collector;
    }

}
