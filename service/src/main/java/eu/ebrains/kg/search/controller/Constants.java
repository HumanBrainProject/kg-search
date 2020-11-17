package eu.ebrains.kg.search.controller;

import eu.ebrains.kg.search.model.target.elasticsearch.instances.Contributor;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Software;

import java.util.Arrays;
import java.util.List;

public class Constants {

    public final static List<Class<?>> TARGET_MODELS_ORDER = Arrays.asList(Contributor.class, Software.class);

}
