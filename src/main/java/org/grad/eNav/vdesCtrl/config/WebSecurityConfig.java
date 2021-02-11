package org.grad.eNav.vdesCtrl.config;

import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.web.filter.ForwardedHeaderFilter;

import javax.servlet.DispatcherType;

@Configuration
@EnableWebSecurity
@ComponentScan(basePackageClasses = KeycloakSecurityComponents.class)
class WebSecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

    /**
     * Rewiring the security adapter to use the KeycloakAuthenticationProvider
     * in order to perform the authentication.
     *
     * @param auth The authentication manager builder
     * @throws Exception
     */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        KeycloakAuthenticationProvider keycloakAuthenticationProvider = keycloakAuthenticationProvider();
        keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());
        auth.authenticationProvider(keycloakAuthenticationProvider);
    }

    /**
     * Forwarded header filter filter registration bean.
     * <p>
     * This corrects the urls produced by the microservice when accessed from a proxy server.
     * E.g. Api gateway:
     * my-service.com/style.css -> apigateway.com/my-service/style.css
     * <p>
     * The proxy server should be sending the forwarded header address as a header
     * which this filter will pick up and resolve for us.
     *
     * @return the filter registration bean
     */
    @Bean
    FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
        final FilterRegistrationBean<ForwardedHeaderFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new ForwardedHeaderFilter());
        filterRegistrationBean.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ASYNC, DispatcherType.ERROR);
        filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return filterRegistrationBean;
    }

    /**
     * On multi-tenant scenarios, Keycloak will defer the resolution of a
     * KeycloakDeployment to the target application at the request-phase.
     *
     * A Request object is passed to the resolver and callers expect a complete
     * KeycloakDeployment. Based on this KeycloakDeployment, Keycloak will
     * resume authenticating and authorizing the request.
     */
    @Bean
    public KeycloakSpringBootConfigResolver KeycloakConfigResolver() {
        return new KeycloakSpringBootConfigResolver();
    }

    /**
     * Defines the session authentication strategy.
     */
    @Bean
    @Override
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
    }

    /**
     * Override this method to configure {@link WebSecurity} so that we ignore
     * certain requests like swagger, css etc.
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        //This will not attempt to authenticate these end points.
        //Saves on validation requests.
        web.ignoring().antMatchers(
                "/webjars/**",  //bootstrap
                "/css/**",                  //css files
                "/js/**",                   //js files
                "/actuator/health",         //spring health actuator
                "/v2/**",
                "/swagger-resources/**",
                "/swagger-resources",
                "/favicon.ico"
        );
    }

    /**
     * The HTTP security configuration.
     * <p>
     * For now this will allow all requests to the micro-service web, health
     * and login endpoints without any authorisation requirements.
     *
     * @param httpSecurity The HTTP security
     * @throws Exception Exception thrown while configuring the security
     */
    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        super.configure(httpSecurity);
        httpSecurity
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/tests/*")
                .hasRole("user")
                .anyRequest()
                .permitAll();
    }

}
