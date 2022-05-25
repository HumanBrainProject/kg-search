package eu.ebrains.kg.search.services;

import eu.ebrains.kg.search.configuration.GracefulDeserializationProblemHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

public abstract class KGServiceClient {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    protected final WebClient serviceAccountWebClient;
    protected final WebClient userWebClient;

    private static final int MAX_RETRIES = 5;

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
       return doExecuteCallForIndexing(clazz, url, 0);
    }

    private <T> T doExecuteCallForIndexing(Class<T> clazz, String url, int currentTry) {
        try{
            return serviceAccountWebClient.get()
                    .uri(url)
                    .headers(h -> h.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                    .retrieve()
                    .bodyToMono(clazz).doOnSuccess(GracefulDeserializationProblemHandler::parsingErrorHandler)
                    .doFinally(t -> GracefulDeserializationProblemHandler.ERROR_REPORTING_THREAD_LOCAL.remove())
                    .block();
        }
        catch (WebClientResponseException e){
            logger.warn("Was not able to execute call for indexing", e);
            if(currentTry<MAX_RETRIES){
                final long waitingTime = currentTry * currentTry * 10000L;
                logger.warn("Retrying to execute call for indexing for max {} more times - next time in {} seconds", MAX_RETRIES-currentTry, waitingTime/1000);
                try{
                    Thread.sleep(waitingTime);
                    return doExecuteCallForIndexing(clazz, url, currentTry+1);
                }
                catch (InterruptedException ie){
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
            else{
                logger.error("Was not able to execute the call for indexing. Going to skip it.", e);
                return null;
            }
        }
    }
}
