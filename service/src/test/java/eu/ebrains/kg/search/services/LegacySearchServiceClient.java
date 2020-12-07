package eu.ebrains.kg.search.services;

import eu.ebrains.kg.search.model.DatabaseScope;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LegacySearchServiceClient {
    private static final ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1000000)).build();
    public static final WebClient webClient = WebClient.builder().exchangeStrategies(exchangeStrategies).build();

    private static final String token = "";
    private static final String ebrainsUrl = "https://kg.ebrains.eu";

    private static final Pattern editorIdPattern = Pattern.compile("(.+)/(.+)/(.+)/(.+)/(.+)");

    public static <T> T getDocument(DatabaseScope databaseScope, String type, String id, Class<T> clazz) {
        String group = databaseScope.equals(DatabaseScope.RELEASED)?"public":"curated";
        return getDocument(String.format("/search/api/groups/%s/types/%s/documents/%s", group, type, id), clazz);
    }

    public static <T> T getLiveDocument(String editorId, Class<T> clazz) {
        Matcher m = editorIdPattern.matcher(editorId);
        if (!m.find( )) {
            return null;
        }
        String org = m.group(1);
        String domain = m.group(2);
        String schema = m.group(3);
        String version = m.group(4);
        String id= m.group(5);
        return getDocument(String.format("/search/api/types/%s/%s/%s/%s/documents/%s/preview", org, domain, schema, version, id), clazz);
    }

    private static <T> T getDocument(String uri, Class<T> clazz) {
        return webClient.get()
                .uri(String.format("%s%s", ebrainsUrl, uri))
                .headers(h ->
                {
                    h.add("Authorization", "Bearer " + token);
                    h.add("Accept", "application/json");
                })
                .retrieve()
                .bodyToMono(clazz)
                .block();
    }
}
