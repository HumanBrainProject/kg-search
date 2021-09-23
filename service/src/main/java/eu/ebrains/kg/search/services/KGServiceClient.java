package eu.ebrains.kg.search.services;

import eu.ebrains.kg.search.configuration.OauthClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

public abstract class KGServiceClient {

    protected final WebClient serviceAccountWebClient;
    protected final WebClient userWebClient;

    public KGServiceClient(WebClient serviceAccountWebClient, WebClient userWebClient) {
        this.serviceAccountWebClient = serviceAccountWebClient;
        this.userWebClient = userWebClient;
    }

    protected <T> T executeCallForInstance(Class<T> clazz, String url, boolean asServiceAccount) {
        try {
            WebClient webClient = asServiceAccount ? this.serviceAccountWebClient : this.userWebClient;
            return webClient.get()
                    .uri(url)
                    .headers(h -> h.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                    .retrieve()
                    .bodyToMono(clazz)
                    .doOnSuccess(KGServiceUtils::parsingErrorHandler)
                    .doFinally(t -> OauthClient.ERROR_REPORTING_THREAD_LOCAL.remove())
                    .block();
        } catch (WebClientResponseException.NotFound e){
            return null;
        }
    }


    protected <T> T executeCallForIndexing(Class<T> clazz, String url) {
        return serviceAccountWebClient.get()
                .uri(url)
                .headers(h -> h.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .retrieve()
                .bodyToMono(clazz).doOnSuccess(KGServiceUtils::parsingErrorHandler)
                .doFinally(t -> OauthClient.ERROR_REPORTING_THREAD_LOCAL.remove())
                .block();
    }
}
