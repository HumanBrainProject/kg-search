package models

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
  val sectionLabels = Seq(plaSectionLabel, activitySectionLabel, datasetSectionLabel, specimenGroupSectionLabel, specimenGroupSectionLabel)
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
    "Mus musculus" -> "Rodent",
    "Homo sapiens" -> "Human"
  )
}
