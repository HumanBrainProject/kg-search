package eu.ebrains.kg.search.controller.translators;

public class Stats {

    private int pageSize;
    private String info;

    public Stats(int pageSize, String info) {
        this.pageSize = pageSize;
        this.info = info;
    }

    public int getPageSize() { return pageSize; }

    public String getInfo() {  return info; }
}

