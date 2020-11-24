package eu.ebrains.kg.search.controller;

import eu.ebrains.kg.search.model.target.elasticsearch.instances.*;

import java.util.Arrays;
import java.util.List;

public class Constants {

    public final static List<Class<?>> TARGET_MODELS_ORDER = Arrays.asList(Project.class, Dataset.class, Subject.class, Sample.class, Model.class, Contributor.class, Software.class);

}
