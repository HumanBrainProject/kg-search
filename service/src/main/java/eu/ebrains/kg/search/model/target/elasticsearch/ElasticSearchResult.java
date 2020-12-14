package eu.ebrains.kg.search.model.target.elasticsearch;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ElasticSearchResult {
    private int took;

    @JsonProperty("timed_out")
    private boolean timedOut;

    @JsonProperty("_shards")
    private Shards shards;

    private Hits hits;

    public int getTook() { return took; }

    public void setTook(int took) { this.took = took; }

    public boolean getTimedOut() { return timedOut; }

    public void setTimedOut(boolean timed_out) { this.timedOut = timed_out; }

    public Shards getShards() { return shards; }

    public void setShards(Shards shards) { this.shards = shards; }

    public Hits getHits() { return hits; }

    public void setHits(Hits hits) { this.hits = hits; }

    private static class Shards {
        private int total;
        private int successful;
        private int skipped;
        private int failed;

        @JsonProperty("total")
        public int getTotal() {
            return total;
        }

        @JsonProperty("total")
        public void setTotal(int total) {
            this.total = total;
        }

        @JsonProperty("successful")
        public int getSuccessful() {
            return successful;
        }

        @JsonProperty("successful")
        public void setSuccessful(int successful) {
            this.successful = successful;
        }

        @JsonProperty("skipped")
        public int getSkipped() {
            return skipped;
        }

        @JsonProperty("skipped")
        public void setSkipped(int skipped) {
            this.skipped = skipped;
        }

        @JsonProperty("failed")
        public int getFailed() {
            return failed;
        }

        @JsonProperty("failed")
        public void setFailed(int failed) {
            this.failed = failed;
        }
    }

    public static class Hits {
        private Total total;

        @JsonProperty("max_score")
        private double maxScore;

        private List<ElasticSearchDocument> hits;

        public Total getTotal() { return total; }

        public void setTotal(Total total) { this.total = total; }

        public double getMaxScore() { return maxScore; }

        public void setMaxScore(double maxScore) { this.maxScore = maxScore; }

        public List<ElasticSearchDocument> getHits() { return hits; }

        public void setHits(List<ElasticSearchDocument> hits) { this.hits = hits; }

    }

    private static class Total {
        private int value;
        private String relation;

        public int getValue() { return value; }

        public void setValue(int value) { this.value = value; }

        public String getRelation() { return relation; }

        public void setRelation(String relation) { this.relation = relation; }
    }

}