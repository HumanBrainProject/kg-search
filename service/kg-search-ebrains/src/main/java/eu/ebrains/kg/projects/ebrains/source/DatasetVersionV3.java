/*
 *  Copyright 2018 - 2021 Swiss Federal Institute of Technology Lausanne (EPFL)
 *  Copyright 2021 - 2023 EBRAINS AISBL
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0.
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  This open source software code was developed in part or in whole in the
 *  Human Brain Project, funded from the European Union's Horizon 2020
 *  Framework Programme for Research and Innovation under
 *  Specific Grant Agreements No. 720270, No. 785907, and No. 945539
 *  (Human Brain Project SGA1, SGA2 and SGA3).
 */

package eu.ebrains.kg.projects.ebrains.source;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import eu.ebrains.kg.common.model.source.*;
import eu.ebrains.kg.common.model.source.ServiceLink;
import eu.ebrains.kg.projects.ebrains.source.commons.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
public class DatasetVersionV3 extends SourceInstance implements IsCiteable, HasMetrics, HasAccessibility, HasMetaBadges {
    private String doi;
    private String howToCite;
    private String description;
    private String fullName;
    private String homepage;
    @JsonDeserialize(using = ListOrSingleStringAsListDeserializer.class)
    private List<String> supportChannels;
    @JsonDeserialize(using = ListOrSingleStringAsListDeserializer.class)
    private List<String> keyword;
    @JsonDeserialize(using = ListOrSingleStringAsListDeserializer.class)
    private List<String> ethicsAssessment;
    private String version;
    private String versionInnovation;
    private Date releaseDate;
    private Date firstReleasedAt;
    private Date lastReleasedAt;
    private List<RelatedPublication> relatedPublications;
    private ExternalRef license;
    private List<PersonOrOrganizationRef> author;
    private List<FullNameRef> projects;
    private List<PersonOrOrganizationRef> custodians;
    private DatasetVersions dataset;
    private String fullDocumentationUrl;
    private String fullDocumentationDOI;
    private File fullDocumentationFile;
    private List<FullNameRef> experimentalApproach;
    private List<FullNameRef> technique;
    private List<Protocol> protocols;
    private NameWithIdentifier accessibility;
    private FileRepository fileRepository;
    private List<File> specialFiles;
    private List<FullNameRef> behavioralProtocol;
    @JsonDeserialize(using = ListOrSingleStringAsListDeserializer.class)
    private List<String> contentTypes;
    @JsonDeserialize(using = ListOrSingleStringAsListDeserializer.class)
    private List<String> repositoryContentTypes;
    private List<StudyTarget> studyTarget;
    private List<FullNameRef> preparationDesign;
    private List<ServiceLink> serviceLinks;
    private List<ServiceLink> serviceLinksFromFiles;
    private List<StudiedSpecimen> studiedSpecimen;
    private Integer last30DaysViews;
    private List<DOI> inputDOIs;
    private List<FullNameRefForResearchProductVersion> inputResearchProductsFromInputFileBundles;
    private List<FullNameRefForResearchProductVersion> inputResearchProductsFromInputFiles;
    private List<FullNameRefForResearchProductVersion> outputResearchProductsFromReverseInputDOIs;
    private List<FullNameRefForResearchProductVersion> outputResearchProductsFromReverseInputFileBundles;
    private List<FullNameRefForResearchProductVersion> outputResearchProductsFromReverseInputFiles;
    @JsonDeserialize(using = ListOrSingleStringAsListDeserializer.class)
    private List<String> inputURLs;
    private List<FullNameRefForResearchProductVersion> inputResearchProductsFromReverseOutputDOIs;
    private List<FullNameRefForResearchProductVersion> inputResearchProductsFromReverseOutputFileBundles;
    private List<FullNameRefForResearchProductVersion> inputResearchProductsFromReverseOutputFiles;
    private List<FullNameRefForResearchProductVersionTarget> inputResearchProductsFromInputBrainAtlasVersions;
    private String issueDate;
    private List<LearningResource> learningResource;

    private List<ExternalRef> livePapers;
    private List<ProtocolExecution> protocolExecutionByFile;
    private List<ProtocolExecution> protocolExecutionByFileBundle;
    private List<ProtocolExecution> protocolExecutionsBySpecimenState;



    @Override
    @JsonIgnore
    public Versions getParentOfVersion() {
        return getDataset();
    }

    @Getter
    @Setter
    @EqualsAndHashCode
    public static class SpecimenServiceLink {
        private String openDataIn;
        private String service;
        private String name;

        public String displayLabel() {
            return this.getName() != null && this.getService() != null ? String.format("Open %s in %s", this.getName(), this.getService()) : null;
        }
    }

    @Getter
    @Setter
    public static class SpecimenServiceLinkCollection {
        private List<SpecimenServiceLink> fromFileBundle;
        private List<SpecimenServiceLink> fromFile;

    }

    @Getter
    @Setter
    public static class StudiedSpecimen {
        private String id;
        private String internalIdentifier;
        private String lookupLabel;
        private Long numberOfSubjects;
        private Long numberOfTissueSamples;
        private String additionalRemarks;
        private FullNameRef origin;
        private List<SpeciesOrStrain> species;
        private List<AnatomicalLocation> anatomicalLocation;
        private List<FullNameRef> biologicalSex;
        private List<FullNameRef> laterality;
        private FullNameRef tissueSampleType;
        private List<StudiedState> studiedState;
        private List<String> isPartOf;
        private List<String> type;
        private List<StudiedSpecimen> subElements;
        private List<SpecimenServiceLinkCollection> serviceLinks;
    }


    @Getter
    @Setter
    public static class StudiedState {
        private String id;
        private List<String> descendedFrom;
        private List<String> type;
        private String additionalRemarks;
        private QuantitativeValueOrRange age;
        private List<FullNameRef> ageCategory;
        private List<FullNameRef> attribute;
        private FullNameRef handedness;
        private List<FullNameRef> pathology;
        private QuantitativeValueOrRange weight;
        private String lookupLabel;
        private transient StudiedSpecimen parent;
        private List<SpecimenServiceLinkCollection> serviceLinks;
    }


    @Getter
    @Setter
    public static class QuantitativeValueOrRange {
        private Double value;
        private List<Double> uncertainty;
        private FullNameRef typeOfUncertainty;
        private FullNameRef unit;
        private Double maxValue;
        private Double minValue;
        private FullNameRef maxValueUnit;
        private FullNameRef minValueUnit;

        private String getValueDisplay(Double value) {
            if (value == null) {
                return null;
            }
            if (value % 1 == 0) {
                //It's an integer -> let's remove the floats.
                return String.valueOf(value.intValue());
            } else {
                return String.format("%.2f", value);
            }
        }

        public String displayString() {
            String valueStr = getValueDisplay(value);
            if (valueStr != null) {
                //Single value
                String valueWithUnit = unit == null ? valueStr : String.format("%s %s", valueStr, unit.getFullName());
                if(!CollectionUtils.isEmpty(uncertainty)){
                    final String uncertaintyValues = uncertainty.stream().map(this::getValueDisplay).filter(Objects::nonNull).collect(Collectors.joining(", "));
                    if(!uncertaintyValues.isBlank() && typeOfUncertainty != null && typeOfUncertainty.getFullName()!=null){
                        if(unit == null){
                            return String.format("%s (%s: %s)", valueWithUnit, typeOfUncertainty.getFullName(), uncertaintyValues);
                        }
                        return String.format("%s (%s: %s %s)", valueWithUnit, typeOfUncertainty.getFullName(), uncertaintyValues, unit.getFullName());
                    }
                }
                return valueWithUnit;
            } else {
                //Value range
                boolean sameUnit = (minValueUnit == null && maxValueUnit == null) || (minValueUnit != null && minValueUnit.equals(maxValueUnit));
                String minValueStr = getValueDisplay(minValue);
                String maxValueStr = getValueDisplay(maxValue);
                return String.format("%s %s - %s %s",
                                StringUtils.defaultString(minValueStr, ""),
                                getString(sameUnit),
                                StringUtils.defaultString(maxValueStr, ""),
                                maxValueUnit != null ? StringUtils.defaultString(maxValueUnit.getFullName(), "") : "").trim()
                        .replaceAll(" {2,}", " ");
            }
        }

        private String getString(boolean sameUnit) {
            if (sameUnit) {
                return "";
            }
            if (minValueUnit != null) {
                return StringUtils.defaultString(minValueUnit.getFullName(), "");
            }
            return "";
        }
    }

    @Getter
    @Setter
    public static class SpecimenOrSpecimenGroupState {
        private QuantitativeValueOrRange age;
        private List<FullNameRef> ageCategory;
        private List<FullNameRef> pathology;
        private QuantitativeValueOrRange weight;
        private List<String> fileRepositoryIds;
        private List<String> attribute;
        private String additionalRemarks;
    }

    @Getter
    @Setter
    public static class ParcellationTerminology extends FullNameRef {
        private List<FullNameRef> brainAtlas;
    }

    @Getter
    @Setter
    public static class ParcellationEntity {
        private List<ParcellationTerminology> parcellationTerminology;
    }


    @Getter
    @Setter
    public static class TissueSampleOrTissueSampleCollection {
        private String id;
        private String internalIdentifier;
        private Integer quantity;
        private List<String> tissueSampleType;
        private FullNameRef tsType;
        private List<Strain> strain;
        private List<FullNameRef> species;
        private List<SpecimenOrSpecimenGroupState> states;
        private List<FullNameRef> origin;
        private List<FullNameRef> biologicalSex;
        private List<FullNameRef> laterality;
        private List<AnatomicalLocation> anatomicalLocation;
        private List<TissueSampleOrTissueSampleCollection> children;

    }

    @Getter
    @Setter
    public static class Strain extends FullNameRef {
        private FullNameRef species;
        private FullNameRef geneticStrainType;
    }

    @Getter
    @Setter
    public static class SpeciesOrStrain extends FullNameRef {
        private FullNameRef species;
        private FullNameRef geneticStrainType;
    }


    @Getter
    @Setter
    public static class SubjectOrSubjectGroup {
        private String id;
        private List<String> subjectType;
        private String internalIdentifier;
        private Integer quantity;
        private List<Strain> strain;
        private List<FullNameRef> species;
        private List<SpecimenOrSpecimenGroupState> states;
        private List<FullNameRef> biologicalSex;
        private List<SubjectOrSubjectGroup> children;


        public void calculateSubjectGroupInformationFromChildren() {
            if (!CollectionUtils.isEmpty(children)) {
                if (quantity == null) {
                    setQuantity(children.size());
                }
                if (CollectionUtils.isEmpty(strain)) {
                    setStrain(children.stream().map(SubjectOrSubjectGroup::getStrain).filter(Objects::nonNull).flatMap(Collection::stream).filter(Objects::nonNull).distinct().sorted(FullNameRef.COMPARATOR).collect(Collectors.toList()));
                }
                if (CollectionUtils.isEmpty(species)) {
                    final List<FullNameRef> speciesFromStrain = children.stream().map(SubjectOrSubjectGroup::getStrain).filter(Objects::nonNull).flatMap(Collection::stream).filter(Objects::nonNull).map(Strain::getSpecies).filter(Objects::nonNull).distinct().sorted(FullNameRef.COMPARATOR).collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(speciesFromStrain)) {
                        setSpecies(speciesFromStrain);
                    } else {
                        setSpecies(children.stream().map(SubjectOrSubjectGroup::getSpecies).filter(Objects::nonNull).flatMap(Collection::stream).filter(Objects::nonNull).distinct().sorted(FullNameRef.COMPARATOR).collect(Collectors.toList()));
                    }
                }
                if (CollectionUtils.isEmpty(biologicalSex)) {
                    setBiologicalSex(children.stream().map(SubjectOrSubjectGroup::getBiologicalSex).filter(Objects::nonNull).flatMap(Collection::stream).filter(Objects::nonNull).distinct().sorted(FullNameRef.COMPARATOR).collect(Collectors.toList()));
                }
                if (CollectionUtils.isEmpty(states)) {
                    //There is no direct relation between subject group states and subject states -> we therefore can't tell which subject states belong together.
                    // If the states are therefore not explicitly stated, we create one "virtual" state to allow to show the aggregated information.
                    SpecimenOrSpecimenGroupState virtualGroupState = new SpecimenOrSpecimenGroupState();
                    final QuantitativeValueOrRange calculatedAgeRange = calculateRangeForGroup(SpecimenOrSpecimenGroupState::getAge, TIME_UNIT_ORDER, TIME_UNIT_TO_MS);
                    if (calculatedAgeRange != null) {
                        virtualGroupState.setAge(calculatedAgeRange);
                    }
                    final QuantitativeValueOrRange calculatedWeightRange = calculateRangeForGroup(SpecimenOrSpecimenGroupState::getWeight, WEIGHT_ORDER, WEIGHT_TO_GRAMS);
                    if (calculatedWeightRange != null) {
                        virtualGroupState.setWeight(calculatedWeightRange);
                    }
                    virtualGroupState.setAttribute(children.stream().map(SubjectOrSubjectGroup::getStates).filter(Objects::nonNull).flatMap(Collection::stream).filter(Objects::nonNull).map(SpecimenOrSpecimenGroupState::getAttribute).filter(Objects::nonNull).flatMap(Collection::stream).distinct().sorted().collect(Collectors.toList()));
                    virtualGroupState.setPathology(children.stream().map(SubjectOrSubjectGroup::getStates).filter(Objects::nonNull).flatMap(Collection::stream).filter(Objects::nonNull).map(SpecimenOrSpecimenGroupState::getPathology).filter(Objects::nonNull).flatMap(Collection::stream).distinct().sorted(FullNameRef.COMPARATOR).collect(Collectors.toList()));
                    virtualGroupState.setAgeCategory(children.stream().map(SubjectOrSubjectGroup::getStates).filter(Objects::nonNull).flatMap(Collection::stream).filter(Objects::nonNull).map(SpecimenOrSpecimenGroupState::getAgeCategory).filter(Objects::nonNull).flatMap(Collection::stream).distinct().sorted(FullNameRef.COMPARATOR).collect(Collectors.toList()));
                    setStates(Collections.singletonList(virtualGroupState));
                }
            }
        }


        private QuantitativeValueOrRange calculateRangeForGroup(Function<SpecimenOrSpecimenGroupState, QuantitativeValueOrRange> f, List<String> orderList, List<Long> translate) {
            long minValueInMinimalUnit = Long.MAX_VALUE;
            long maxValueInMinimalUnit = Long.MIN_VALUE;
            QuantitativeValueOrRange range = new QuantitativeValueOrRange();
            for (SubjectOrSubjectGroup child : children) {
                if (!CollectionUtils.isEmpty(child.states)) {
                    for (SpecimenOrSpecimenGroupState state : child.states) {
                        final QuantitativeValueOrRange val = f.apply(state);
                        if (val != null) {
                            if (val.unit != null && val.value != null) {
                                int indexOfUnit = orderList.indexOf(val.unit.getFullName());
                                if (indexOfUnit != -1) {
                                    Long toMinimalUnit = translate.get(indexOfUnit);
                                    final long valueInMinimalUnit = (long) (val.value * toMinimalUnit);
                                    if (valueInMinimalUnit < minValueInMinimalUnit) {
                                        minValueInMinimalUnit = valueInMinimalUnit;
                                        range.setMinValue(val.value);
                                        range.setMinValueUnit(val.unit);
                                    }
                                    if (valueInMinimalUnit > maxValueInMinimalUnit) {
                                        maxValueInMinimalUnit = valueInMinimalUnit;
                                        range.setMaxValue(val.value);
                                        range.setMaxValueUnit(val.unit);
                                    }
                                } else {
                                    //Insufficient information -> we have to skip
                                    return null;
                                }
                            } else if (val.minValueUnit != null && val.maxValueUnit != null && val.minValue != null && val.maxValue != null) {
                                int indexOfMinUnit = orderList.indexOf(val.minValueUnit.getFullName());
                                int indexOfMaxUnit = orderList.indexOf(val.maxValueUnit.getFullName());
                                if (indexOfMinUnit != -1 && indexOfMaxUnit != -1) {
                                    final long minInMinimalUnit = (long) (val.minValue * translate.get(indexOfMinUnit));
                                    final long maxInMinimalUnit = (long) (val.maxValue * translate.get(indexOfMaxUnit));
                                    if (minInMinimalUnit < minValueInMinimalUnit) {
                                        minValueInMinimalUnit = minInMinimalUnit;
                                        range.setMinValue(val.minValue);
                                        range.setMinValueUnit(val.minValueUnit);
                                    }
                                    if (maxInMinimalUnit > maxValueInMinimalUnit) {
                                        maxValueInMinimalUnit = maxInMinimalUnit;
                                        range.setMaxValue(val.maxValue);
                                        range.setMaxValueUnit(val.maxValueUnit);
                                    }
                                } else {
                                    //Insufficient information -> we have to skip
                                    return null;
                                }
                            }
                        }
                    }
                }
            }
            if (range.getMinValue() != null && range.getMinValueUnit() != null && range.getMaxValue() != null) {
                return range;
            } else {
                return null;
            }
        }
    }


    //TODO this information should come from openMINDS
    private final static List<String> TIME_UNIT_ORDER = Arrays.asList("millisecond", "second", "minute", "hour", "day", "week", "month", "year");
    //Months are slightly problematic since they ar not stable (so are years) -> let's try to do it with good-enough approximations.
    private final static List<Long> TIME_UNIT_TO_MS = Arrays.asList(1L, 1000L, 60L * 1000L, 60 * 60 * 1000L, 24 * 60 * 60 * 1000L, 7 * 24 * 60 * 60 * 1000L, 28 * 24 * 60 * 60 * 1000L, 365 * 24 * 60 * 60 * 1000L);
    private final static List<String> WEIGHT_ORDER = Arrays.asList("gram", "kilogram");
    private final static List<Long> WEIGHT_TO_GRAMS = Arrays.asList(1L, 1000L);


    @Getter
    @Setter
    public static class OntologicalTerm {
        private String ontologyIdentifier;
        private String name;
    }

    @Getter
    @Setter
    public static class Protocol {
        private List<OntologicalTerm> behavioralTask;
        private List<String> studyOption;
    }


    @Getter
    @Setter
    public static class DatasetVersions extends Versions {

        @JsonProperty("datasetAuthor")
        private List<PersonOrOrganizationRef> author;

        @JsonProperty("datasetCustodian")
        private List<PersonOrOrganizationRef> custodians;

        private List<FullNameRef> datasetProjects;
    }


    @Getter
    @Setter
    public static class ParcellationEntityFromStudyTarget extends FullNameRefForResearchProductVersion {
        private List<FullNameRefForResearchProductVersion> brainAtlasVersionForParcellationTerminologyVersion;
        private List<FullNameRef> brainAtlasForParcellationEntity;
    }

    @JsonIgnore
    @Override
    public List<ServiceLink> getAllServiceLinks() {
        return Stream.concat(getServiceLinks() != null ? getServiceLinks().stream(): Stream.empty(), getServiceLinksFromFiles()!=null ? getServiceLinksFromFiles().stream() : Stream.empty()).collect(Collectors.toList());
    }

    @Override
    @JsonIgnore
    public List<String> getAllContentTypes() {
        return Stream.concat(getContentTypes() != null ? getContentTypes().stream() : Stream.empty(), getRepositoryContentTypes()!=null ? getRepositoryContentTypes().stream() : Stream.empty()).distinct().sorted().collect(Collectors.toList());
    }


    @Getter
    @Setter
    public static class ProtocolExecution extends FullNameRef {
        private List<FullNameRef> protocol;
        private List<FullNameRef> behavioralProtocol;
    }

    @Override
    @JsonIgnore
    public boolean hasProtocolExecutions() {
        return !CollectionUtils.isEmpty(protocolExecutionByFile) || !CollectionUtils.isEmpty(protocolExecutionByFileBundle) || !CollectionUtils.isEmpty(protocolExecutionsBySpecimenState);
    }
}


