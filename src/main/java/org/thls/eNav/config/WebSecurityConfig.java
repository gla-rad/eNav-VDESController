package org.thls.eNav.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import java.util.UUID;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf()
                .ignoringAntMatchers("/config/**")
                .and()
                .authorizeRequests()
                .antMatchers("/assets/**").permitAll()
                .antMatchers("/login").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                //.loginPage("/login")
                .and()
                .logout()
                //.logoutUrl("/logout")
                .and()
                .httpBasic()
                .and()
                .rememberMe()
                .key(UUID.randomUUID().toString())
                .tokenValiditySeconds(1209600);
    }

}
