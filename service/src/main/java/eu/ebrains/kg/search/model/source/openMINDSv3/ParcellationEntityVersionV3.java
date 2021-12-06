package eu.ebrains.kg.search.model.source.openMINDSv3;

import eu.ebrains.kg.search.model.source.openMINDSv3.commons.FullNameRef;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.FullNameRefForResearchProductVersion;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Versions;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ParcellationEntityVersionV3 extends SourceInstanceV3{
    private String name;
    private String version;
    private Versions parcellationEntity;
    private List<String> ontologyIdentifier;
    private String versionIdentifier;
    //TODO relation assessment
    private List<ServiceLink> dataLocation;
    private List<ParcellationTerminology> parcellationTerminology;

    @Getter
    @Setter
    private static class ServiceLink {
        private String openDataIn;
        private String service;
        private String name;
    }

    @Getter
    @Setter
    private static class ParcellationTerminology {
        private FullNameRefForResearchProductVersion  brainAtlasVersion;
        private FullNameRef brainAtlas;
    }

}
