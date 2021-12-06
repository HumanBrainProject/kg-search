package eu.ebrains.kg.search.model.source.openMINDSv3;

import eu.ebrains.kg.search.model.source.openMINDSv3.commons.FullNameRef;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.FullNameRefForResearchProductVersion;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Version;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Versions;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ParcellationEntityV3 extends SourceInstanceV3{
    private String name;
    private List<String> ontologyIdentifier;
    private List<FullNameRef> brainAtlas;
    private List<Version> versions;
}
