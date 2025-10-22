package com.youtopin.vaadin.samples.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Aligns Spring Security with the demo's session-based guard so that the custom
 * Vaadin login view is presented instead of the default Spring login form.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/VAADIN/**",
                                "/frontend/**",
                                "/frontend-es5/**",
                                "/frontend-es6/**",
                                "/webjars/**",
                                "/icons/**",
                                "/images/**",
                                "/styles/**",
                                "/themes/**",
                                "/manifest.webmanifest",
                                "/sw.js",
                                "/offline.html"
                        ).permitAll()
                        .anyRequest().permitAll())
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .logout(logout -> logout.disable());
        return http.build();
    }
}
