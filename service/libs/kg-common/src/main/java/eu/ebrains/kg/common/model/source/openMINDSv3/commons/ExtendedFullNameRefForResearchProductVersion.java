package eu.ebrains.kg.common.model.source.openMINDSv3.commons;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ExtendedFullNameRefForResearchProductVersion extends FullNameRefForResearchProductVersion{
    private List<FullNameRefForResearchProductVersion> researchProductVersions;

    public FullNameRefForResearchProductVersion getRelevantReference(){
        if(researchProductVersions!=null && researchProductVersions.size()==1){
            return researchProductVersions.get(0);
        }
        return this;
    }
}