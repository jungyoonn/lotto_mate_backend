package com.eeerrorcode.lottomate.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.log4j.Log4j2;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@Log4j2
@RequestMapping("/api/test")
public class TestController {
  @GetMapping("")
  public ResponseEntity<?> getTest() {
    return ResponseEntity.ok("test successful!!");
  }
  
}
