# How to create a new model for KG Search

To create a new model, you need to follow these steps:

1. Define a KG query and store it in `services/kg-indexing/src/main/resources/queries`. Name the file as `yourType.json`
2. Create a "source model" for your query results in `libs/kg-common/src/main/java/eu/ebrains/kg/common/model/source/openMINDSv3` and call it `YourTypeV3.java`. Make sure it extends `SourceInstanceV3`. A source model is a simple POJO KG Search uses to deserialize the results of the above defined query to.
3. Create a "target model" in `libs/kg-common/src/main/java/eu/ebrains/kg/common/model/target/elasticsearch/instances` and call it `YourType.java`. It defines the way a model is represented in the KG Search. Please note, that this target model also contains directives for the UI to guide its representation - for further details see "Configure a target model".
4. Create a translator in `libs/kg-common/src/main/java/eu/ebrains/kg/common/controller/translators/kgv3` and call it `YourTypeV3Translator.java`. See below how you need to configure a translator specifically.
5. Register the translator in `libs/kg-common/src/main/java/eu/ebrains/kg/common/model/TranslatorModel.java`.
6. Run the `KGIndexingApplication` to upload the queries at least once (or multiple times if you have updates)
7. Run the `KGSearchApplication` together with the UI (make sure it points to your development instance). Now you should be able to see the card in live mode and adapt to your needs.
8. At deployment time: Make sure, you rebuild the index for the newly created type


## Configure a target model
A target model is - again in the first place a POJO carrying the information which is going to be serialized and stored in ElasticSearch ready to be consumed by the UI.
Please note, that a target model should always implement the `TargetInstance` interface. 
However, additional annotations allow the system to automatically create the ElasticSearch mappings and give directives to the UI on how the information shall be displayed:

### Type-based 
The following annotations allow to specify information about the type used to organize the types amongst themselves in KG Search.

#### @MetaInfo
* name: the technical name of the type as it's going to be represented in ElasticSearch

### Property based

#### @ElasticSearchInfo
* mapping: allows to exclude a property from the mapping
* ignore_above: don't index properties with more characters than defined
* type: hint to explicitly specify the ElasticSearch type on how to index (e.g. "keyword")

#### @FieldInfo
* label: The label to be shown for the value of this field
* hint: A possibility to add information on how the field has to be interpreted
* icon: A possibility to add an icon to the label
* fieldType: giving hints on how this value shall be interpreted giving hints to the UI what widget to use for
* visible: If the field is visible to an end-user or if it is just provided in the code (e.g. for search indexing)
* labelHidden: Don't show the label in the UI
* overview: Show this value in the result representation of the KG Search
* ignoreForSearch: Don't use this value for searching
* termsOfUse: Show the terms of use notification next to the field
* isFilterableFacet: The facet filter for this field can be filtered (intended for facets with many entries)
* useForSuggestion: Use the values of these fields to do search term suggestions
* layout: Allowing to specify a "grouping name" represented as different sections in the search representation
* linkIcon: An icon 
... (to be completed)



## Configure a translator
The easiest way to implement a translator is by copying over an already existing one and adapt it. 

1. Make sure, your translator is correctly typed and therefore change the extension of your new class to `TranslatorV3<YourTypeV3, YourType, YourTypeV3Translator.Result>`.
2. Several overridden methods will need to be updated now (the compiler is going to tell you which ones).
3. Update the `semanticTypes()` method to return all types, this translator is supposed to handle.
4. Override the `getQueryIds()` method to return one stable (!!!) UUID (you can use any UUID generator for this) for each returned `semanticType`. This is the UUID the system will store the query for this type with in the KG. Usually, there is only a single semantic type and therefore only one UUID - there are special cases where the same query can be used for multiple types though (compare `ControlledTermV3Translator`). 
5. Write the logic on how to translate from the source model to the target model. 

Please note that you have the additional possibilities to define the search behavior:
- 
