package eu.ebrains.kg.search.model.source;

import eu.ebrains.kg.search.model.ErrorReport;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ResultsOfKG<E> {

    List<E> getData();
    Integer getTotal();
    Integer getFrom();
    Integer getSize();
    ErrorReport getErrors();
    void setErrors(ErrorReport errors);

}
