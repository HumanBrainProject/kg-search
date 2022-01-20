package eu.ebrains.kg.search.model.source.openMINDSv3;

import eu.ebrains.kg.search.model.source.openMINDSv3.commons.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ParcellationEntityV3 extends SourceInstanceV3{
    private String name;
    private List<String> ontologyIdentifier;
    private List<FullNameRef> brainAtlas;
    private List<VersionWithServiceLink> versions;

    @Getter
    @Setter
    @EqualsAndHashCode
    public static class VersionWithServiceLink {
        private String id;
        private String fullName;
        private String versionIdentifier;
        private String versionInnovation;
        private ServiceLink viewer;
        private Version brainAtlasVersion;
    }



}
