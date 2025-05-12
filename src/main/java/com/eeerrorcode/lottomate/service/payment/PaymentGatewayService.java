package com.eeerrorcode.lottomate.service.payment;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.eeerrorcode.lottomate.domain.dto.payment.PaymentGatewayResponseDto;
import com.eeerrorcode.lottomate.exeption.PaymentException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * 외부 결제 게이트웨이(포트원)와의 통신을 담당하는 서비스
 */
@Service
@Transactional
@Log4j2
@RequiredArgsConstructor
public class PaymentGatewayService {
  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;
  
  @Value("${iamport.api.key}")
  private String iamportApiKey;
  
  @Value("${iamport.api.secret}")
  private String iamportApiSecret;
  
  @Value("${iamport.api.url}")
  private String iamportApiUrl;
  
  /**
   * 포트원 액세스 토큰 발급
   * 
   * @return 발급된 액세스 토큰
   * @throws PaymentException 토큰 발급 실패 시 발생
   */
  public String getAccessToken() {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      
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
        throw new PaymentException("포트원 토큰 발급 실패: " + root.get("message").asText());
      }
      
      // 액세스 토큰 추출
      String accessToken = root.get("response").get("access_token").asText();
      log.info("포트원 액세스 토큰 발급 성공");
      return accessToken;
    } catch (Exception e) {
      log.error("포트원 토큰 발급 실패: " + e.getMessage(), e);
      throw new PaymentException("포트원 토큰 발급 중 오류 발생: " + e.getMessage());
    }
  }
  
  /**
   * 결제 정보 조회
   * 
   * @param impUid 포트원 결제 고유번호
   * @return 결제 정보 조회 결과
   * @throws PaymentException 결제 정보 조회 실패 시 발생
   */
  public PaymentGatewayResponseDto getPaymentInfo(String impUid) {
    try {
      // 액세스 토큰 발급
      String accessToken = getAccessToken();
      
      HttpHeaders headers = new HttpHeaders();
      headers.set("Authorization", accessToken);
      
      HttpEntity<String> entity = new HttpEntity<>(headers);
      String url = iamportApiUrl + "/payments/" + impUid;
      
      String response = restTemplate.getForObject(url, String.class, entity);
      JsonNode root = objectMapper.readTree(response);
      
      // 응답 코드가 0이 아닐 경우 오류
      if (root.get("code").asInt(1) != 0) {
        throw new PaymentException("포트원 결제 정보 조회 실패: " + root.get("message").asText());
      }
      
      // 결제 정보 추출 및 DTO 매핑
      JsonNode paymentData = root.get("response");
      PaymentGatewayResponseDto responseDto = new PaymentGatewayResponseDto();
      
      responseDto.setImpUid(paymentData.get("imp_uid").asText());
      responseDto.setMerchantUid(paymentData.get("merchant_uid").asText());
      responseDto.setAmount(new BigDecimal(paymentData.get("amount").asText()));
      responseDto.setStatus(paymentData.get("status").asText());
      
      // 카드 정보가 있는 경우
      if (paymentData.has("card_info") && !paymentData.get("card_info").isNull()) {
        JsonNode cardInfo = paymentData.get("card_info");
        responseDto.setCardName(cardInfo.get("card_name").asText());
        responseDto.setCardNumber(cardInfo.get("card_number").asText());
      }
      
      // 영수증 URL이 있는 경우
      if (paymentData.has("receipt_url") && !paymentData.get("receipt_url").isNull()) {
        responseDto.setReceiptUrl(paymentData.get("receipt_url").asText());
      }
      
      return responseDto;
    } catch (Exception e) {
      log.error("포트원 결제 정보 조회 실패: " + e.getMessage(), e);
      throw new PaymentException("포트원 결제 정보 조회 중 오류 발생: " + e.getMessage());
    }
  }
  
  /**
   * 결제 환불 처리
   * 
   * @param impUid 포트원 결제 고유번호
   * @param amount 환불 금액
   * @param reason 환불 사유
   * @return 환불 처리 결과
   * @throws PaymentException 환불 처리 실패 시 발생
   */
  public PaymentGatewayResponseDto refundPayment(String impUid, BigDecimal amount, String reason) {
    try {
      // 액세스 토큰 발급
      String accessToken = getAccessToken();
      
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
      headers.set("Authorization", accessToken);
      
      MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
      map.add("imp_uid", impUid);
      map.add("amount", amount.toString());
      map.add("reason", reason);
      
      HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
      String url = iamportApiUrl + "/payments/cancel";
      
      String response = restTemplate.postForObject(url, entity, String.class);
      JsonNode root = objectMapper.readTree(response);
      
      // 응답 코드가 0이 아닐 경우 오류
      if (root.get("code").asInt(1) != 0) {
        throw new PaymentException("포트원 결제 환불 실패: " + root.get("message").asText());
      }
      
      // 환불 결과 추출 및 DTO 매핑
      JsonNode refundData = root.get("response");
      PaymentGatewayResponseDto responseDto = new PaymentGatewayResponseDto();
      
      responseDto.setImpUid(refundData.get("imp_uid").asText());
      responseDto.setMerchantUid(refundData.get("merchant_uid").asText());
      responseDto.setAmount(new BigDecimal(refundData.get("cancel_amount").asText()));
      responseDto.setStatus(refundData.get("status").asText());
      
      log.info("포트원 결제 환불 성공: impUid={}, amount={}", impUid, amount);
      return responseDto;
    } catch (Exception e) {
      log.error("포트원 결제 환불 실패: " + e.getMessage(), e);
      throw new PaymentException("포트원 결제 환불 중 오류 발생: " + e.getMessage());
    }
  }
  
  /**
   * 정기결제 빌링키 발급
   * 
   * @param customerUid 구매자 식별키
   * @param cardNumber 카드번호
   * @param expiry 유효기간 (YYYY-MM)
   * @param birth 생년월일 (YYMMDD)
   * @param pwd2Digit 카드 비밀번호 앞 2자리
   * @return 빌링키 발급 결과
   * @throws PaymentException 빌링키 발급 실패 시 발생
   */
  public String issueBillingKey(String customerUid, String cardNumber, String expiry, String birth, String pwd2Digit) {
    try {
      // 액세스 토큰 발급
      String accessToken = getAccessToken();
      
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.set("Authorization", accessToken);
      
      // 빌링키 발급 요청 본문
      String requestBody = String.format(
        "{\"customer_uid\":\"%s\",\"card_number\":\"%s\",\"expiry\":\"%s\",\"birth\":\"%s\",\"pwd_2digit\":\"%s\"}", 
        customerUid, cardNumber, expiry, birth, pwd2Digit
      );
      
      HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
      String url = iamportApiUrl + "/subscribe/customers/" + customerUid;
      
      String response = restTemplate.postForObject(url, entity, String.class);
      JsonNode root = objectMapper.readTree(response);
      
      // 응답 코드가 0이 아닐 경우 오류
      if (root.get("code").asInt(1) != 0) {
        throw new PaymentException("포트원 빌링키 발급 실패: " + root.get("message").asText());
      }
      
      log.info("포트원 빌링키 발급 성공: customerUid={}", customerUid);
      return customerUid;
    } catch (Exception e) {
      log.error("포트원 빌링키 발급 실패: " + e.getMessage(), e);
      throw new PaymentException("포트원 빌링키 발급 중 오류 발생: " + e.getMessage());
    }
  }
  
  /**
   * 정기결제 요청 처리
   * 
   * @param customerUid 구매자 식별키 (빌링키)
   * @param merchantUid 주문번호
   * @param amount 결제금액
   * @param name 주문명
   * @return 정기결제 요청 결과
   * @throws PaymentException 정기결제 요청 실패 시 발생
   */
  public PaymentGatewayResponseDto requestSubscription(String customerUid, String merchantUid, BigDecimal amount, String name) {
    try {
      // 액세스 토큰 발급
      String accessToken = getAccessToken();
      
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.set("Authorization", accessToken);
      
      // 정기결제 요청 본문
      String requestBody = String.format(
        "{\"customer_uid\":\"%s\",\"merchant_uid\":\"%s\",\"amount\":%s,\"name\":\"%s\"}", 
        customerUid, merchantUid, amount.toString(), name
      );
      
      HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
      String url = iamportApiUrl + "/subscribe/payments/again";
      
      String response = restTemplate.postForObject(url, entity, String.class);
      JsonNode root = objectMapper.readTree(response);
      
      // 응답 코드가 0이 아닐 경우 오류
      if (root.get("code").asInt(1) != 0) {
        throw new PaymentException("포트원 정기결제 요청 실패: " + root.get("message").asText());
      }
      
      // 결제 결과 추출 및 DTO 매핑
      JsonNode paymentData = root.get("response");
      PaymentGatewayResponseDto responseDto = new PaymentGatewayResponseDto();
      
      responseDto.setImpUid(paymentData.get("imp_uid").asText());
      responseDto.setMerchantUid(paymentData.get("merchant_uid").asText());
      responseDto.setAmount(new BigDecimal(paymentData.get("amount").asText()));
      responseDto.setStatus(paymentData.get("status").asText());
      
      log.info("포트원 정기결제 요청 성공: customerUid={}, merchantUid={}, amount={}", 
        customerUid, merchantUid, amount);
      return responseDto;
    } catch (Exception e) {
      log.error("포트원 정기결제 요청 실패: " + e.getMessage(), e);
      throw new PaymentException("포트원 정기결제 요청 중 오류 발생: " + e.getMessage());
    }
  }
}
