package eu.ebrains.kg.search.controller;

import eu.ebrains.kg.search.model.target.elasticsearch.instances.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Constants {

    public final static List<Class<?>> TARGET_MODELS_ORDER = Arrays.asList(Project.class, Dataset.class, Subject.class, Sample.class, Model.class, Contributor.class, Software.class);

    public final static Map<String, Class<?>> TARGET_MODELS_MAP = Map.of(
            "Project", Project.class,
            "Dataset", Dataset.class,
            "Subject", Subject.class,
            "Sample", Sample.class,
            "Model", Model.class,
            "Contributor", Contributor.class,
            "Software", Software.class
    );

}
