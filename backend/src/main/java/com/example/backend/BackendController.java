package com.example.backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BackendController {

  @GetMapping(value = "/healthcheck")
  public String test() {
    return "OK";
  }

  @GetMapping(value = "/submit")
  public String submit() {
    return "DONE";
  }
}
