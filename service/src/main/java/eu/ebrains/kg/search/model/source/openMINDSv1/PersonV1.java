package eu.ebrains.kg.search.model.source.openMINDSv1;

import eu.ebrains.kg.search.model.source.PersonV1andV2;


public class PersonV1 extends PersonV1andV2 {
    private String id;
    private String description;

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
