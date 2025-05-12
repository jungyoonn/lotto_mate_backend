package com.eeerrorcode.lottomate.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
@Tag(name = "Payment Token API", description = "포트원 토큰 발급 관련 API입니다")
@Log4j2
public class PaymentTokenController {
  
  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;
  
  @Value("${iamport.api.key}")
  private String iamportApiKey;
  
  @Value("${iamport.api.secret}")
  private String iamportApiSecret;
  
  @Value("${iamport.api.url}")
  private String iamportApiUrl;
  
  @Operation(
    summary = "포트원 액세스 토큰 발급",
    description = "포트원 API 호출에 필요한 액세스 토큰을 발급합니다.",
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "토큰 발급 성공"
      ),
      @ApiResponse(responseCode = "400", description = "토큰 발급 실패")
    }
  )
  @PostMapping("/portone-token")
  public ResponseEntity<?> getPortOneToken() {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      
      // 포트원 인증 요청 본문
      String requestBody = String.format(
        "{\"imp_key\":\"%s\",\"imp_secret\":\"%s\"}", 
        iamportApiKey, 
        iamportApiSecret
      );
      
      HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
      String url = iamportApiUrl + "/users/getToken";
      
      String response = restTemplate.postForObject(url, entity, String.class);
      JsonNode root = objectMapper.readTree(response);
      
      // 응답 코드가 0이 아닐 경우 오류
      if (root.get("code").asInt(1) != 0) {
        log.error("포트원 토큰 발급 실패: " + root.get("message").asText());
        return ResponseEntity.badRequest().body(root);
      }
      
      // 액세스 토큰 추출
      JsonNode tokenData = root.get("response");
      
      log.info("포트원 토큰 발급 성공");
      return ResponseEntity.ok(tokenData);
    } catch (Exception e) {
      log.error("포트원 토큰 발급 중 오류 발생: " + e.getMessage(), e);
      return ResponseEntity.badRequest().body("{\"error\": \"토큰 발급 처리 중 오류가 발생했습니다.\"}");
    }
  }
}