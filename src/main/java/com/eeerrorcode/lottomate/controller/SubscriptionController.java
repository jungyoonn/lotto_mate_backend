package com.eeerrorcode.lottomate.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequiredArgsConstructor
@RequestMapping("/api/subscription")
@Tag(name = "Subscription API", description = "구독 관련 기능을 테스트할 수 있는 Restful API입니다")
public class SubscriptionController {
  
  @PostMapping("/verify-payment")
  public ResponseEntity<?> verifyPaymentAndCreateSubscription(@RequestBody String entity) {
      
    return ResponseEntity.ok().body("success");
  }
  
  @GetMapping("/info")
  public ResponseEntity<?> getSubscriptionInfo(@RequestParam String param) {
    return ResponseEntity.ok().body("success");
  }
  
  @PostMapping("/cancel")
  public ResponseEntity<?> cancelSubscription(@RequestBody String entity) {
    
    return ResponseEntity.ok().body("success");
  }
  
}
