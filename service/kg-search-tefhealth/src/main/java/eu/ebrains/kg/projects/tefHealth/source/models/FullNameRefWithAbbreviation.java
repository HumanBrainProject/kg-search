package eu.ebrains.kg.projects.tefHealth.source.models;

import eu.ebrains.kg.common.model.source.FullNameRef;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class FullNameRefWithAbbreviation extends FullNameRef {

    private String abbreviation;

    @Override
    public String getFullName(){
        if(StringUtils.isNotBlank(super.getFullName()) && StringUtils.isNotBlank(abbreviation)){
            return String.format("%s (%s)", super.getFullName(), abbreviation);
        } else if (StringUtils.isNotBlank(super.getFullName())) {
            return super.getFullName();
        }
        return null;
    }

}
