package eu.ebrains.kg.common.model.source.openMINDSv3.commons;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnatomicalLocation extends FullNameRefForResearchProductVersion{
    private FullNameRefForResearchProductVersion brainAtlasVersion;
    private String brainAtlas;
}
