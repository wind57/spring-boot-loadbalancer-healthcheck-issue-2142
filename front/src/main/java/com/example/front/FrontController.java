package com.example.front;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FrontController {

  private final BackendRequestSubmitter backendRequestSubmitter;

  @Autowired
  public FrontController(BackendRequestSubmitter backendRequestSubmitter) {
    this.backendRequestSubmitter = backendRequestSubmitter;
  }

  @GetMapping(value = "/healthcheck")
  public String healthcheck() {
    return "OK";
  }

  @GetMapping(value = "/")
  public String index() {
    return backendRequestSubmitter.submitRequest();
  }
}
