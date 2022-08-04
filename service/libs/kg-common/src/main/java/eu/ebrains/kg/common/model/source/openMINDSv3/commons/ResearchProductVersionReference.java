package eu.ebrains.kg.common.model.source.openMINDSv3.commons;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResearchProductVersionReference extends ExtendedFullNameRefForResearchProductVersion{
    private String doi;
    private String howToCite;
}