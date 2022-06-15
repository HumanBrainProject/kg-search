package eu.ebrains.kg.common.model.source.openMINDSv3;

import eu.ebrains.kg.common.model.source.openMINDSv3.commons.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ParcellationEntityV3 extends SourceInstanceV3{
    private String name;
    private List<String> ontologyIdentifier;
    private FullNameRef brainAtlas;
    private List<VersionWithServiceLink> versions;
    private List<FullNameRef> parents;
    private List<FullNameRef> isParentOf;
    private FullNameRef relatedUBERONTerm;

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
        private List<FullNameRef> laterality;
        private List<FileWithDataset> inspiredBy;
        private List<FileWithDataset> visualizedIn;
    }

    @Getter
    @Setter
    @EqualsAndHashCode
    public static class FileWithDataset {
        private String IRI;
        private String name;
        private FullNameRefForResearchProductVersion dataset;
    }




}
