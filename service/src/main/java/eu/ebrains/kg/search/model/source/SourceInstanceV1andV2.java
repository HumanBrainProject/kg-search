package eu.ebrains.kg.search.model.source;

import eu.ebrains.kg.search.model.source.SourceInstance;

import java.util.Date;

public class SourceInstanceV1andV2 implements SourceInstance {
    private String id;
    private String identifier;
    private String editorId;

    private Date firstReleaseAt;
    private Date lastReleaseAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIdentifier() {
        return identifier;
    }
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getEditorId() { return editorId; }
    public void setEditorId(String editorId) { this.editorId = editorId; }

    public Date getFirstReleaseAt() {
        return firstReleaseAt;
    }

    public void setFirstReleaseAt(Date firstReleaseAt) {
        this.firstReleaseAt = firstReleaseAt;
    }

    public Date getLastReleaseAt() {
        return lastReleaseAt;
    }

    public void setLastReleaseAt(Date lastReleaseAt) {
        this.lastReleaseAt = lastReleaseAt;
    }
}
