package eu.ebrains.kg.projects.tefHealth.source.models;

import eu.ebrains.kg.common.model.source.FullNameRef;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class FullNameRefWithProvidedBy extends FullNameRef {

    private String providedBy;

    @Override
    public String getFullName(){
        if(StringUtils.isNotBlank(super.getFullName()) && StringUtils.isNotBlank(providedBy)){
            return String.format("%s (provided by %s)", super.getFullName(), providedBy);
        } else if (StringUtils.isNotBlank(super.getFullName())) {
            return super.getFullName();
        }
        return null;
    }

}
