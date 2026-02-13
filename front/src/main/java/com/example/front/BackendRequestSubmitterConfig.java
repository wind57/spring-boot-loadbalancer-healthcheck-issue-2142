package com.example.front;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableDiscoveryClient
public class BackendRequestSubmitterConfig {

  @Bean
  public BackendRequestSubmitter requestSubmitter(ReactorLoadBalancerExchangeFilterFunction lbFunction) {
    WebClient webClient = WebClient
        .builder()
        .baseUrl("http://backend-service")
        .filter(lbFunction)
        .build();
    return new BackendRequestSubmitter(webClient);
  }
}
