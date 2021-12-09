/*
 * Copyright 2018 - 2021 Swiss Federal Institute of Technology Lausanne (EPFL)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This open source software code was developed in part or in whole in the
 * Human Brain Project, funded from the European Union's Horizon 2020
 * Framework Programme for Research and Innovation under
 * Specific Grant Agreements No. 720270, No. 785907, and No. 945539
 * (Human Brain Project SGA1, SGA2 and SGA3).
 *
 */

package eu.ebrains.kg.search.model.source.openMINDSv3;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.*;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Setter
public class DatasetVersionV3 extends SourceInstanceV3 {
    private String doi;
    private String howToCite;
    private String description;
    private String fullName;
    private List<String> homepage;
    private List<String> keyword;
    private List<String> ethicsAssessment;
    private String version;
    private String versionInnovation;
    private Date releaseDate;
    private List<String> relatedPublications;
    private ExternalRef license;
    private List<PersonOrOrganizationRef> author;
    private List<FullNameRef> projects;
    private List<PersonOrOrganizationRef> custodians;
    private List<SubjectOrSubjectGroup> subjects;
    private List<TissueSampleOrTissueSampleCollection> tissueSampleOrCollection;
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
    private List<StudyTarget> studyTarget;
    @Getter
    @Setter
    public static class StudyTarget extends FullNameRefForResearchProductVersion {
        private FullNameRefForResearchProductVersion brainAtlasVersion;
        private String brainAtlas;
    }
    @Getter
    @Setter
    public static class QuantitativeValueOrRange{
        private Double value;
        private FullNameRef unit;
        private Double maxValue;
        private Double minValue;
        private FullNameRef maxValueUnit;
        private FullNameRef minValueUnit;

        private String getValueDisplay(Double value){
            if(value==null){
                return null;
            }
            if(value%1==0){
                //It's an integer -> let's remove the floats.
                return String.valueOf(value.intValue());
            }
            else{
                return String.format("%.2f", value);
            }
        }

        public String displayString(){
            String valueStr = getValueDisplay(value);
            if(valueStr!=null){
                //Single value
                return unit == null ? valueStr : String.format("%s %s", valueStr, unit.getFullName());
            }
            else{
                //Value range
                boolean sameUnit = (minValueUnit == null && maxValueUnit == null) || (minValueUnit != null && minValueUnit.equals(maxValueUnit));
                String minValueStr = getValueDisplay(minValue);
                String maxValueStr = getValueDisplay(maxValue);
                return String.format("%s %s - %s %s",
                        StringUtils.defaultString(minValueStr, ""),
                        sameUnit ? "" : minValueUnit != null ? StringUtils.defaultString(minValueUnit.getFullName(), "") : "",
                        StringUtils.defaultString(maxValueStr, ""),
                        maxValueUnit!=null ? StringUtils.defaultString(maxValueUnit.getFullName(), "") : "").trim()
                        .replaceAll(" {2,}", " ");
            }
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
    }


    @Getter
    @Setter
    public static class AnatomicalLocation extends FullNameRef{
        private String fallbackName;
        private List<ParcellationTerminology> parcellationTerminology;
        private List<DataLocation> dataLocation;
        private List<ParcellationEntity> parcellationEntity;
    }

    @Getter
    @Setter
    public static class DataLocation{
        private String openDataIn;
        private FullNameRef service;
    }

    @Getter
    @Setter
    public static class ParcellationTerminology extends FullNameRef{
        private List<FullNameRef> brainAtlas;
    }

    @Getter
    @Setter
    public static class ParcellationEntity{
        private List<ParcellationTerminology> parcellationTerminology;
    }


    @Getter
    @Setter
    public static class TissueSampleOrTissueSampleCollection {
        private String id;
        private String internalIdentifier;
        private Integer quantity;
        private List<String> tissueSampleType;
        private List<FullNameRef> strain;
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
    public static class SubjectOrSubjectGroup {
        private String id;
        private List<String> subjectType;
        private String internalIdentifier;
        private Integer quantity;
        private List<FullNameRef> strain;
        private List<FullNameRef> species;
        private List<SpecimenOrSpecimenGroupState> states;
        private List<FullNameRef> biologicalSex;
        private List<SubjectOrSubjectGroup> children;


        public void calculateSubjectGroupInformationFromChildren(){
            if(!CollectionUtils.isEmpty(children)) {
                if(quantity==null){
                    setQuantity(children.size());
                }
                if(CollectionUtils.isEmpty(strain)){
                    setStrain(children.stream().map(SubjectOrSubjectGroup::getStrain).filter(Objects::nonNull).flatMap(Collection::stream).filter(Objects::nonNull).distinct().sorted(FullNameRef.COMPARATOR).collect(Collectors.toList()));
                }
                if(CollectionUtils.isEmpty(species)){
                    setSpecies(children.stream().map(SubjectOrSubjectGroup::getSpecies).filter(Objects::nonNull).flatMap(Collection::stream).filter(Objects::nonNull).distinct().sorted(FullNameRef.COMPARATOR).collect(Collectors.toList()));
                }
                if(CollectionUtils.isEmpty(biologicalSex)){
                    setBiologicalSex(children.stream().map(SubjectOrSubjectGroup::getBiologicalSex).filter(Objects::nonNull).flatMap(Collection::stream).filter(Objects::nonNull).distinct().sorted(FullNameRef.COMPARATOR).collect(Collectors.toList()));
                }
                if(CollectionUtils.isEmpty(states)){
                    //There is no direct relation between subject group states and subject states -> we therefore can't tell which subject states belong together.
                    // If the states are therefore not explicitly stated, we create one "virtual" state to allow to show the aggregated information.
                    SpecimenOrSpecimenGroupState virtualGroupState = new SpecimenOrSpecimenGroupState();
                    final QuantitativeValueOrRange calculatedAgeRange = calculateRangeForGroup(SpecimenOrSpecimenGroupState::getAge, TIME_UNIT_ORDER, TIME_UNIT_TO_MS);
                    if(calculatedAgeRange!=null){
                        virtualGroupState.setAge(calculatedAgeRange);
                    }
                    final QuantitativeValueOrRange calculatedWeightRange = calculateRangeForGroup(SpecimenOrSpecimenGroupState::getWeight, WEIGHT_ORDER, WEIGHT_TO_GRAMS);
                    if(calculatedWeightRange!=null){
                        virtualGroupState.setWeight(calculatedWeightRange);
                    }
                    virtualGroupState.setPathology(children.stream().map(SubjectOrSubjectGroup::getStates).filter(Objects::nonNull).flatMap(Collection::stream).filter(Objects::nonNull).map(SpecimenOrSpecimenGroupState::getPathology).flatMap(Collection::stream).distinct().sorted(FullNameRef.COMPARATOR).collect(Collectors.toList()));
                    virtualGroupState.setAgeCategory(children.stream().map(SubjectOrSubjectGroup::getStates).filter(Objects::nonNull).flatMap(Collection::stream).filter(Objects::nonNull).map(SpecimenOrSpecimenGroupState::getAgeCategory).flatMap(Collection::stream).distinct().sorted(FullNameRef.COMPARATOR).collect(Collectors.toList()));
                    setStates(Collections.singletonList(virtualGroupState));
                }
            }
        }


        private QuantitativeValueOrRange calculateRangeForGroup(Function<SpecimenOrSpecimenGroupState, QuantitativeValueOrRange> f, List<String> orderList, List<Long> translate){
            long minValueInMinimalUnit = Long.MAX_VALUE;
            long maxValueInMinimalUnit = Long.MIN_VALUE;
            QuantitativeValueOrRange range = new QuantitativeValueOrRange();
            for (SubjectOrSubjectGroup child : children) {
                if(!CollectionUtils.isEmpty(child.states)){
                    for (SpecimenOrSpecimenGroupState state : child.states) {
                        final QuantitativeValueOrRange val = f.apply(state);
                        if(val!=null){
                            if(val.unit !=null && val.value!=null){
                                int indexOfUnit = orderList.indexOf(val.unit.getFullName());
                                if(indexOfUnit!=-1){
                                    Long toMinimalUnit = translate.get(indexOfUnit);
                                    final long valueInMinimalUnit = Double.valueOf(val.value * toMinimalUnit).longValue();
                                    if(valueInMinimalUnit<minValueInMinimalUnit){
                                        minValueInMinimalUnit = valueInMinimalUnit;
                                        range.setMinValue(val.value);
                                        range.setMinValueUnit(val.unit);
                                    }
                                    if(valueInMinimalUnit>maxValueInMinimalUnit){
                                        maxValueInMinimalUnit = valueInMinimalUnit;
                                        range.setMaxValue(val.value);
                                        range.setMaxValueUnit(val.unit);
                                    }
                                }
                                else{
                                    //Insufficient information -> we have to skip
                                    return null;
                                }
                            }
                            else if(val.minValueUnit != null && val.maxValueUnit!=null && val.minValue!=null && val.maxValue!=null){
                                int indexOfMinUnit = orderList.indexOf(val.minValueUnit.getFullName());
                                int indexOfMaxUnit = orderList.indexOf(val.maxValueUnit.getFullName());
                                if(indexOfMinUnit!=-1 && indexOfMaxUnit!=-1){
                                    final long minInMinimalUnit = Double.valueOf(val.minValue * translate.get(indexOfMinUnit)).longValue();
                                    final long maxInMinimalUnit = Double.valueOf(val.maxValue * translate.get(indexOfMaxUnit)).longValue();
                                    if(minInMinimalUnit<minValueInMinimalUnit){
                                        minValueInMinimalUnit = minInMinimalUnit;
                                        range.setMinValue(val.minValue);
                                        range.setMinValueUnit(val.minValueUnit);
                                    }
                                    if(maxInMinimalUnit>maxValueInMinimalUnit){
                                        maxValueInMinimalUnit = maxInMinimalUnit;
                                        range.setMaxValue(val.maxValue);
                                        range.setMaxValueUnit(val.maxValueUnit);
                                    }
                                }
                                else{
                                    //Insufficient information -> we have to skip
                                    return null;
                                }
                            }
                        }
                    }
                }
            }
            if(range.getMinValueUnit()!=null && range.getMinValue()!=null && range.getMinValueUnit()!=null && range.getMaxValue()!=null){
                return range;
            }
            else{
                return null;
            }
        }
    }



    //TODO this information should come from openMINDS
    private final static List<String> TIME_UNIT_ORDER = Arrays.asList("millisecond", "second", "minute", "hour", "day", "week", "month", "year");
    //Months are slightly problematic since they ar not stable (so are years) -> let's try to do it with good-enough approximations.
    private final static List<Long> TIME_UNIT_TO_MS = Arrays.asList(1L, 1000L, 60L*1000L, 60*60*1000L, 24*60*60*1000L, 7*24*60*60*1000L, 28*24*60*60*1000L, 365*24*60*60*1000L);
    private final static List<String> WEIGHT_ORDER = Arrays.asList("gram", "kilogram");
    private final static List<Long> WEIGHT_TO_GRAMS = Arrays.asList(1L, 1000L);


    @Getter
    @Setter
    public static class NameWithIdentifier {
        private List<String> identifier;
        private String name;
    }

    @Getter
    @Setter
    public static class OntologicalTerm {
        private String ontologyIdentifier;
        private String name;
    }

    @Getter
    @Setter
    public static class FileRepository extends FullNameRef{
        private String iri;
    }

    @Getter
    @Setter
    public static class Protocol {
        private List<OntologicalTerm> behavioralTask;
        private List<String> studyOption;
    }

    @Getter
    @Setter
    public static class File {
        private List<String> roles;
        private String iri;
        private List<String> formats;
        private String name;
    }


    @Getter
    @Setter
    public static class DatasetVersions extends Versions {

        @JsonProperty("datasetAuthor")
        private List<PersonOrOrganizationRef> author;

        @JsonProperty("datasetCustodian")
        private List<PersonOrOrganizationRef> custodians;
    }


    @Getter
    @Setter
    public static class ParcellationEntityFromStudyTarget extends FullNameRefForResearchProductVersion{
        private List<FullNameRefForResearchProductVersion> brainAtlasVersionForParcellationTerminologyVersion;
        private List<FullNameRef> brainAtlasForParcellationEntity;
    }
}


