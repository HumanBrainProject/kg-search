/*
 *   Copyright (c) 2019, EPFL/Human Brain Project PCO
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package models.excel

object CommonVars {
  /*
   *   labels
   */

  // sections
  val mindsLabel = "MINDS"
  val plaSectionLabel = "PLA"
  val activitySectionLabel = "ACTIVITY"
  val datasetSectionLabel = "DATASET"
  val specimenGroupSectionLabel = "SPECIMENGROUP"
  val sampleSectionLabel = "SAMPLE"

  val sectionLabels = Seq(
    plaSectionLabel,
    activitySectionLabel,
    datasetSectionLabel,
    specimenGroupSectionLabel,
    specimenGroupSectionLabel
  )
  // ids
  val activityIdLabel = "ActivityID"
  val datasetIdLabel = "DatasetID"
  val specimenGroupIdLabel = "SpecimenGroupID"
  val subjectIdLabel = "SubjectID"
  val sampleIdLabel = "SampleID"
  val licenseIdLabel = "LicenseID"
  val componentIdLabel = "ComponentID"

  // others
  val specimenGroupLabel = "SpecimenGroup"
  val specimengroupLabel = "Specimengroup"
  val activityLabel = "Activity"
  val sampleLabel = "Sample"
  val samplesLabel = "Samples"
  val descriptionLabel = "Description"
  val datasetLabel = "Dataset"
  val methodLabel = "Method"
  val filesLabel = "Files"
  val speciesLabel = "Species"
  val regionLabel = "Region"
  val subjectLabel = "Subject"
  val subjectsLabel = "Subjects"
  val parcellationAtlasLabel = "ParcellationAtlas"
  val dataPathLabel = "DataPath"
  val formatLabel = "Format"
  val childrenLabel = "children"
  val releaseDateLabel = "ReleaseDate"
  val keyLabel = "name"
  val valueLabel = "value"

  /*
   *  indexes
   */

  val globalInfoFirstIdx = 2
  val globalInfoLastIdx = 10
  val globalInfoKeyIdx = 0
  val globalInfoValueIdx = 1
  val sectionTitleIdx = 1 // first row of a block contains section title only
  val columnKeyIdx = 1 // key is stored in column B
  val firstValueIdx = 2 // index where first value are put. Column C
  val nbColumnsBetweenChildren = 1 // there is one blank columns to separate children

  /*
   *  values
   */

  val cellContentSeparator = ";"
  val jsonSeparator = ","
  val empty = ""

  /*
   * Mapping
   */

  val speciesMapping = Map(
    "Rattus norvegicus" -> "Rodent",
    "Mus musculus"      -> "Rodent",
    "Homo sapiens"      -> "Human"
  )
}
