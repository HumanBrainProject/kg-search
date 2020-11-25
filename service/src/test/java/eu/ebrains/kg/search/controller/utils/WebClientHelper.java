package eu.ebrains.kg.search.controller.utils;

import eu.ebrains.kg.search.model.DatabaseScope;
import eu.ebrains.kg.search.model.source.ResultOfKGv2;
import eu.ebrains.kg.search.model.source.openMINDSv1.SubjectV1;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

public class WebClientHelper {
    private static final ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 10000)).build();
    public static final WebClient webClient = WebClient.builder().exchangeStrategies(exchangeStrategies).build();

    private static final String token = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImJicC1vaWRjIn0.eyJleHAiOjE2MDYzMjExODAsInN1YiI6IjMwNzQ4NyIsImF1ZCI6WyJuZXh1cy1rZy1zZWFyY2giXSwiaXNzIjoiaHR0cHM6XC9cL3NlcnZpY2VzLmh1bWFuYnJhaW5wcm9qZWN0LmV1XC9vaWRjXC8iLCJqdGkiOiJmOWI0YTMxMS01ZGQ5LTRiNWUtOTAzOC1hYjQ5M2ZjZjBkOTAiLCJpYXQiOjE2MDYzMDY3ODAsImhicF9rZXkiOiI2OWY2MjQ5NzViMTk1ZjJlM2FjZDVkYjIwMzlmZmE5NDYzMDQ1NjZlIn0.RqrP-iJU-cqvSzchBZoGaa1obWxHcByM4XJKywOxNYU0Ff5n0297fizoLmWBF5fy8YJkdkRFQOHeMJ4UR5cUgI-MMp8N9cli9jVB3TpsZrZJesuctRhYJyM7BUt-bL9xTedLX1L088kLxThcpjHwg60nWcjEowgIwK0Azz9f6Pk";
    private static final String vocab = "https://schema.hbp.eu/search/";
    private static final String hbpUrl = "https://kg.humanbrainproject.eu";
    private static final String ebrainsUrl = "https://kg.ebrains.eu";

    public static <T> T executeQuery(String query, DatabaseScope databaseScope, Class<T> clazz) {
        return WebClientHelper.webClient.get()
                .uri(String.format("%s/%s/instances?databaseScope=%s&vocab=%s&size=1", hbpUrl, query, databaseScope, vocab))
                .headers(h ->
                {
                    h.add("Authorization", token);
                    h.add("Accept", "application/json");
                })
                .retrieve()
                .bodyToMono(clazz)
                .block();
    }

    public static <T> T getDocument(String group, String type, String id, Class<T> clazz) {
        return WebClientHelper.webClient.get()
                .uri(String.format("%s/search/api/groups/%s/types/%s/documents/%s", ebrainsUrl, group, type, id))
                .headers(h ->
                {
                    h.add("Authorization", token);
                    h.add("Accept", "application/json");
                })
                .retrieve()
                .bodyToMono(clazz)
                .block();
    }

}
