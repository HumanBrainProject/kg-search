package eu.ebrains.kg.search.model.source.commons;

public class ParcellationRegion {

    public ParcellationRegion() {}

    public ParcellationRegion(String name, String alias, String url) {
        this.name = name;
        this.alias = alias;
        this.url = url;
    }

    private String name;
    private String alias;
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}