package eu.ebrains.kg.search.model.source;

import java.util.List;

public class ResultOfKGv2<E> {

    private List<E> results;
    private String apiName;
    private String importantMessage;
    private String databaseScope;
    private Integer total;
    private Integer size;
    private Integer start;

    public List<E> getResults() {
        return results;
    }

    public void setResults(List<E> results) {
        this.results = results;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getImportantMessage() {
        return importantMessage;
    }

    public void setImportantMessage(String importantMessage) {
        this.importantMessage = importantMessage;
    }

    public String getDatabaseScope() {
        return databaseScope;
    }

    public void setDatabaseScope(String databaseScope) {
        this.databaseScope = databaseScope;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }
}
