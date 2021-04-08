package eu.ebrains.kg.search.controller;

import eu.ebrains.kg.search.model.target.elasticsearch.instances.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Constants {

    public final static Integer esQuerySize = 10000;

    public final static String OPENMINDS_CORE_NAMESPACE = "https://openminds.ebrains.eu/core/";

    public final static String SOURCE_MODEL_DATASET = OPENMINDS_CORE_NAMESPACE + "Dataset";
    public final static String SOURCE_MODEL_DATASET_VERSIONS = OPENMINDS_CORE_NAMESPACE + "DatasetVersion";
    public final static String SOURCE_MODEL_PERSON = OPENMINDS_CORE_NAMESPACE + "Person";
    public final static String SOURCE_MODEL_PROJECT = OPENMINDS_CORE_NAMESPACE + "Project";
    public final static String SOURCE_MODEL_SUBJECT = OPENMINDS_CORE_NAMESPACE + "Subject";
    public final static String SOURCE_MODEL_SAMPLE = OPENMINDS_CORE_NAMESPACE + "Sample";
    public final static String SOURCE_MODEL_MODEL = OPENMINDS_CORE_NAMESPACE + "Model";
    public final static String SOURCE_MODEL_SOFTWARE = OPENMINDS_CORE_NAMESPACE + "Software";

    public final static List<String> SOURCE_MODELS = Arrays.asList(
            SOURCE_MODEL_DATASET,
            SOURCE_MODEL_DATASET_VERSIONS,
            SOURCE_MODEL_PERSON,
            SOURCE_MODEL_PROJECT,
            SOURCE_MODEL_SUBJECT,
            SOURCE_MODEL_SAMPLE,
            SOURCE_MODEL_MODEL,
            SOURCE_MODEL_SOFTWARE
    );

    public final static List<Class<?>> TARGET_MODELS_ORDER = Arrays.asList(
            Project.class,
            Dataset.class,
            Subject.class,
            Sample.class,
            Model.class,
            Software.class,
            Contributor.class,
            DatasetVersions.class
    );

    public final static List<Map<String, String>> GROUPS = Arrays.asList(
            Map.of("name", "curated",
                    "label", "in progress"),
            Map.of("name", "public",
                    "label", "publicly released")
    );
}
