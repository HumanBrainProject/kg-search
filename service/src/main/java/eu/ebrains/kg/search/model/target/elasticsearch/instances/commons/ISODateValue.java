package eu.ebrains.kg.search.model.target.elasticsearch.instances.commons;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class ISODateValue extends  Value<Date>{
    public ISODateValue() { }

    public ISODateValue(Date value) {
        super(value);
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    public Date getValue() {
        return super.getValue();
    }
}
