package eu.ebrains.kg.search.model.source.openMINDSv3.commons;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileRepository extends FullNameRef{
    private String iri;
    private String firstFile;
}

