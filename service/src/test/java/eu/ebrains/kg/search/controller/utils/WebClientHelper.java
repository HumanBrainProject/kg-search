package eu.ebrains.kg.search.controller.utils;

import eu.ebrains.kg.search.model.DatabaseScope;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

public class WebClientHelper {
    private static final ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 10000)).build();
    public static final WebClient webClient = WebClient.builder().exchangeStrategies(exchangeStrategies).build();

    private static final String token = "";
    private static final String vocab = "https://schema.hbp.eu/search/";
    private static final String hbpUrl = "https://kg.humanbrainproject.eu";
    private static final String ebrainsUrl = "https://kg.ebrains.eu";

    public static <T> T executeQuery(String query, DatabaseScope databaseScope, Class<T> clazz) {
        return WebClientHelper.webClient.get()
                .uri(String.format("%s/%s/instances?databaseScope=%s&vocab=%s", hbpUrl, query, databaseScope, vocab))
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
