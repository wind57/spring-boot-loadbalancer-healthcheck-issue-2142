package com.example.front;

import org.springframework.web.reactive.function.client.WebClient;

public class BackendRequestSubmitter {

  private final WebClient webClient;

  public BackendRequestSubmitter(WebClient webClient) {
    this.webClient = webClient;
  }

  public String submitRequest() {
    return webClient
        .get()
        .uri("/submit")
        .retrieve()
        .bodyToMono(String.class)
        .block();
  }
}
