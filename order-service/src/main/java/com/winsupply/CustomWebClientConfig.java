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

//    @Bean
//    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {

//      http.authorizeHttpRequests((requests) -> requests.anyRequest().authenticated());
//      http.formLogin();
//      http.httpBasic();

//        http.authorizeHttpRequests((requests) -> requests.anyRequest().permitAll());
//        http.formLogin();
//        http.httpBasic();

//        http.authorizeHttpRequests((requests) -> requests.anyRequest().denyAll());
//        http.formLogin();
//        http.httpBasic();

//        http.authorizeHttpRequests((requests) -> requests.requestMatchers("/orders/{orderId}").authenticated().requestMatchers("/orders").permitAll().requestMatchers("/orders/search").denyAll());
//        http.formLogin();
//        http.httpBasic();
//        return http.build();
//
//    }

}
