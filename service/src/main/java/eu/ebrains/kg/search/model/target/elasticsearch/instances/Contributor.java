package eu.ebrains.kg.search.model.target.elasticsearch.instances;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ebrains.kg.search.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.MetaInfo;

import java.util.Date;
import java.util.List;

@MetaInfo(orderId = 6, identifier = "uniminds/core/person/v1.0.0/search")
public class Contributor {

    @JsonProperty("first_release")
    @FieldInfo(label = "First release", type="date", layout = FieldInfo.Layout.GROUP)
    private Value<Date> firstRelease;
    @JsonProperty("last_release")
    private Value<Date> lastRelease;
    private List<InternalReference> custodianOf;
    private List<InternalReference> contributions;
    private List<InternalReference> modelContributions;
    private Value<String> title;
    private Value<String> type;

    public void setFirstRelease(Date firstRelease){
        setFirstRelease(firstRelease!=null ? new Value<>(firstRelease) : null);
    }

    public void setLastRelease(Date lastRelease){
        setLastRelease(lastRelease!=null ? new Value<>(lastRelease) : null);
    }

    public void setTitle(String title){
        setTitle(title!=null ? new Value<>(title) : null);
    }

    public void setType(String type){
        setType(type!=null ? new Value<>(type): null);
    }


    public Value<Date> getFirstRelease() {
        return firstRelease;
    }

    public void setFirstRelease(Value<Date> firstRelease) {
        this.firstRelease = firstRelease;
    }

    public Value<Date> getLastRelease() {
        return lastRelease;
    }

    public void setLastRelease(Value<Date> lastRelease) {
        this.lastRelease = lastRelease;
    }

    public List<InternalReference> getCustodianOf() {
        return custodianOf;
    }

    public void setCustodianOf(List<InternalReference> custodianOf) {
        this.custodianOf = custodianOf;
    }

    public List<InternalReference> getContributions() {
        return contributions;
    }

    public void setContributions(List<InternalReference> contributions) {
        this.contributions = contributions;
    }

    public List<InternalReference> getModelContributions() {
        return modelContributions;
    }

    public void setModelContributions(List<InternalReference> modelContributions) {
        this.modelContributions = modelContributions;
    }

    public Value<String> getTitle() {
        return title;
    }

    public void setTitle(Value<String> title) {
        this.title = title;
    }

    public Value<String> getType() {
        return type;
    }

    public void setType(Value<String> type) {
        this.type = type;
    }

    public static class InternalReference {

        public InternalReference() {
        }

        public InternalReference(String reference, String value) {
            this.reference = reference;
            this.value = value;
        }

        private String reference;
        private String value;

        public String getReference() {
            return reference;
        }

        public void setReference(String reference) {
            this.reference = reference;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class Value<T>{

        public Value() {
        }

        public Value(T value) {
            this.value = value;
        }

        private T value;

        public T getValue() {
            return value;
        }
    }

}
