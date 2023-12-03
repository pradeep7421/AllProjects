package com.winsupply;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class CustomWebClientConfig {

    @Bean
    WebClient customWebClient() {
        return WebClient.builder().build();
    }

}
