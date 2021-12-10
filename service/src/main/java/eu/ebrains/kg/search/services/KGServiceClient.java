package eu.ebrains.kg.search.services;

import eu.ebrains.kg.search.configuration.GracefulDeserializationProblemHandler;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

public abstract class KGServiceClient {

    protected final WebClient serviceAccountWebClient;
    protected final WebClient userWebClient;

    public KGServiceClient(WebClient serviceAccountWebClient, WebClient userWebClient) {
        this.serviceAccountWebClient = serviceAccountWebClient;
        this.userWebClient = userWebClient;
    }

    protected <T> T executeCallForInstance(Class<T> clazz, String url, boolean asServiceAccount) {
        WebClient webClient = asServiceAccount ? this.serviceAccountWebClient : this.userWebClient;
        return webClient.get()
                .uri(url)
                .headers(h -> h.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .retrieve()
                .bodyToMono(clazz)
                .doOnSuccess(GracefulDeserializationProblemHandler::parsingErrorHandler)
                .doFinally(t -> GracefulDeserializationProblemHandler.ERROR_REPORTING_THREAD_LOCAL.remove())
                .block();
    }

    protected <T> T executeCallForIndexing(Class<T> clazz, String url) {
        return serviceAccountWebClient.get()
                .uri(url)
                .headers(h -> h.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .retrieve()
                .bodyToMono(clazz).doOnSuccess(GracefulDeserializationProblemHandler::parsingErrorHandler)
                .doFinally(t -> GracefulDeserializationProblemHandler.ERROR_REPORTING_THREAD_LOCAL.remove())
                .block();
    }
}
