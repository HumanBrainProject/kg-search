package eu.ebrains.kg.search.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Configuration
public class OauthClient {

    private final ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1000000)).build();

    @Bean
    @Primary
    WebClient standardWebClient(){
        return WebClient.builder().exchangeStrategies(exchangeStrategies).build();
    }

    @Bean
    @Qualifier("asServiceAccount")
    WebClient serviceAccountWebClient(ClientRegistrationRepository clientRegistrations, OAuth2AuthorizedClientService authorizedClientService) {
        AuthorizedClientServiceOAuth2AuthorizedClientManager clientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrations, authorizedClientService);
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2 =  new ServletOAuth2AuthorizedClientExchangeFilterFunction(clientManager);
        oauth2.setDefaultClientRegistrationId("kg");
        return WebClient.builder().exchangeStrategies(exchangeStrategies).apply(oauth2.oauth2Configuration()).filter((clientRequest, nextFilter) ->{
            ClientRequest updatedHeaders = ClientRequest.from(clientRequest).headers(h -> {
                h.put("Client-Authorization", h.get("Authorization"));
            }).build();
            return nextFilter.exchange(updatedHeaders);
        }).build();
    }

    @Bean
    @Qualifier("asUser")
    WebClient userWebClient(ClientRegistrationRepository clientRegistrations, OAuth2AuthorizedClientService authorizedClientService, HttpServletRequest request) {
        AuthorizedClientServiceOAuth2AuthorizedClientManager clientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrations, authorizedClientService);
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2 =  new ServletOAuth2AuthorizedClientExchangeFilterFunction(clientManager);
        oauth2.setDefaultClientRegistrationId("kg");
        return WebClient.builder().exchangeStrategies(exchangeStrategies).apply(oauth2.oauth2Configuration()).filter((clientRequest, nextFilter) ->{
            ClientRequest updatedHeaders = ClientRequest.from(clientRequest).headers(h -> {
                //Spring adds the oauth2 bearer token to the standard "Authorization" header -> we want it to be sent as
                // "Client-Authorization" though to let the user token be handed in properly.
                h.put("Client-Authorization", h.get("Authorization"));
                List<String> userAuth = h.get("User-Authorization");
                h.put("Authorization", userAuth);
                h.remove("User-Authorization");
            }).build();
            return nextFilter.exchange(updatedHeaders);
        }).defaultRequest(r -> {
            /**
             *  We have to add the user access token to the request here, because this consumer is executed in the original
             *  thread and we therefore have access to the original request. We store it in a temporary header since otherwise
             *  it would be overwritten by the above exchange filter.
             */
            r.header("User-Authorization", request.getHeader("Authorization"));
        }).build();
    }

}
