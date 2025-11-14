package com.assistant.smartsearch.config;

import com.assistant.smartsearch.application.DDoSProtectionFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final DDoSProtectionFilter ddosProtectionFilter;

    public SecurityConfig(DDoSProtectionFilter ddosProtectionFilter) {
        this.ddosProtectionFilter = ddosProtectionFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/chatbot").permitAll()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(ddosProtectionFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}