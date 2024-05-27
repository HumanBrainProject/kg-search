package eu.ebrains.kg.projects.tefHealth.source.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class NameRefWithAbbreviation extends NameRef {

    private String abbreviation;

    @Override
    public String getName(){
        if(StringUtils.isNotBlank(super.getName()) && StringUtils.isNotBlank(abbreviation)){
            return String.format("%s (%s)", super.getName(), abbreviation);
        } else if (StringUtils.isNotBlank(super.getName())) {
            return super.getName();
        }
        return null;
    }

}
