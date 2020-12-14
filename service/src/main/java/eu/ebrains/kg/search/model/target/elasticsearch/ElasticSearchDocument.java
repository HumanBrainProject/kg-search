package eu.ebrains.kg.search.model.target.elasticsearch;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class ElasticSearchDocument {

    @JsonProperty("_index")
    private String index;

    @JsonProperty("_type")
    private String type;

    @JsonProperty("_id")
    private String id;

    @JsonProperty("_version")
    private Integer version;

    @JsonProperty("_seq_no")
    private Integer seqNo;

    @JsonProperty("_primary_term")
    private Integer _primary_term;

    private boolean found;

    @JsonProperty("_source")
    private Map<String, Object> source;

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public void setSource(Map<String, Object> source) {
        this.source = source;
    }

    public Map<String, Object> getSource() {
        return source;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(Integer seqNo) {
        this.seqNo = seqNo;
    }

    public Integer get_primary_term() {
        return _primary_term;
    }

    public void set_primary_term(Integer _primary_term) {
        this._primary_term = _primary_term;
    }

    public boolean isFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
    }
}

