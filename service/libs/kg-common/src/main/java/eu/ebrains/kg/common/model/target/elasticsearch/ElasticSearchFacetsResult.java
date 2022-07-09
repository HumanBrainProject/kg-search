/*
 * Copyright 2018 - 2021 Swiss Federal Institute of Technology Lausanne (EPFL)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This open source software code was developed in part or in whole in the
 * Human Brain Project, funded from the European Union's Horizon 2020
 * Framework Programme for Research and Innovation under
 * Specific Grant Agreements No. 720270, No. 785907, and No. 945539
 * (Human Brain Project SGA1, SGA2 and SGA3).
 *
 */

package eu.ebrains.kg.common.model.target.elasticsearch;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ElasticSearchFacetsResult extends ElasticSearchResult {

    private Map<String, Aggregation> aggregations;

    @Getter
    @Setter
    public static class Aggregation extends ElasticSearchValueAgg {

        private KeywordsAgg keywords;
        private Total total;
        private Inner inner;
    }

    @Getter
    @Setter
    public static class KeywordsAgg extends ElasticSearchAgg {

        private List<KeywordsBucket> buckets;

        @Getter
        @Setter
        public static class KeywordsBucket extends Bucket {

            private Reverse reverse;
            private KeywordsAgg keywords;
        }

        @Getter
        @Setter
        public static class Reverse {

            @JsonProperty("doc_count")
            private int docCount;
        }
    }

    @Getter
    @Setter
    public static class Total {

        private Integer value;
    }

    @Getter
    @Setter
    public static class Inner {

        private KeywordsAgg keywords;
    }

    private Map<String, List<Suggestion>> suggest;

    @Getter
    @Setter
    public static class Suggestion {
        private String text;
        private Integer offset;
        private Integer length;
        private List<Option> options;
    }

    @Getter
    @Setter
    public static class Option {
        private String text;
        private Double score;
        private Integer freq;
    }
}