package org.grad.eNav.vdesCtrl.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RequestInterceptor;
import feign.codec.Decoder;
import feign.jackson.JacksonDecoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.PageJacksonModule;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SortJacksonModule;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

/**
 * The FeignClientConfig Class.
 * <p>
 * This configuration provides the OAuth2 authorization for the Feign clients.
 * It will inject new authorization tokens into the feign request header through
 * a request interceptor.
 * <p>
 * Note that this configuration is not annotated, but it should be injected
 * directly to the feign requests that do not already have authorization.
 * <p>
 * The best source for this type of an implementation cab be found here:
 * <a>
 *     https://stackoverflow.com/questions/55308918/spring-security-5-calling-oauth2-secured-api-in-application-runner-results-in-il
 * </a>
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public class FeignClientConfig {

    /**
     * The OAuth2 Authorized Client Manager bean provider. In the new Spring
     * Security 5 framework, we can use the OAuth2AuthorizedClientService
     * class to authorize our clients, as long as the configuration is found
     * in the application.properties file.
     *
     * @param clientRegistrationRepository the client registration repository
     * @param clientService the OAuth2 authorized client service
     * @return the OAuth2 authorized client manager to authorize the feign requests
     */
    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(ClientRegistrationRepository clientRegistrationRepository,
                                                                 OAuth2AuthorizedClientService clientService) {
        // First create an OAuth2 Authorized Client Provider
        OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build();

        // Create a client manage to handle the Feign authorization
        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrationRepository,
                clientService
        );

        // Set the client provider in the client
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        // And return
        return authorizedClientManager;
    }

    /**
     * The Feign request interceptor that will inject the new authorization
     * tokens. To generate those token, it will use the OAuth2AuthorizedClient
     * manager defined above.
     *
     * To generate our token, we need a principal but our setup with service
     * accounts in keycloak doesn't care about that. So in here we use an
     * anonymous authentication token.
     *
     * @param manager the OAuth2 authorized client manager to authorize the feign requests
     * @return the Feign request interceptor
     */
    @Bean
    public RequestInterceptor repositoryClientOAuth2Interceptor(OAuth2AuthorizedClientManager manager) {
        return requestTemplate -> {
            OAuth2AuthorizedClient client = manager.authorize(OAuth2AuthorizeRequest
                    .withClientRegistrationId("keycloak")
                    .principal(new AnonymousAuthenticationToken("name", "vdes-ctrl", AuthorityUtils.createAuthorityList("ROLE_ACTUATOR")))
                    .build());
            String accessToken = client.getAccessToken().getTokenValue();
            requestTemplate.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        };
    }

}
