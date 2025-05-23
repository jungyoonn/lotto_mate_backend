package com.eeerrorcode.lottomate.service.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.eeerrorcode.lottomate.domain.dto.payment.*;
import com.eeerrorcode.lottomate.domain.entity.payment.*;
import com.eeerrorcode.lottomate.exeption.*;
import com.eeerrorcode.lottomate.repository.UserRepository;
import com.eeerrorcode.lottomate.repository.payment.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Transactional
@Log4j2
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService{
  private final PaymentMethodRepository paymentMethodRepository;
  private final PaymentRepository paymentRepository;
  private final PaymentLogRepository paymentLogRepository;
  private final SubscriptionRepository subscriptionRepository;
  private final UserRepository userRepository;
  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;
  private final PaymentGatewayService paymentGatewayService;
  
  // // 생성자에 @Qualifier 추가
  // public PaymentServiceImpl(
  //   PaymentMethodRepository paymentMethodRepository,
  //   PaymentRepository paymentRepository,
  //   PaymentLogRepository paymentLogRepository,
  //   SubscriptionRepository subscriptionRepository,
  //   UserRepository userRepository,
  //   RestTemplate restTemplate,
  //   @Qualifier("jacksonTemplateObjectMapper") ObjectMapper objectMapper) {
    
  //   this.paymentMethodRepository = paymentMethodRepository;
  //   this.paymentRepository = paymentRepository;
  //   this.paymentLogRepository = paymentLogRepository;
  //   this.subscriptionRepository = subscriptionRepository;
  //   this.userRepository = userRepository;
  //   this.restTemplate = restTemplate;
  //   this.objectMapper = objectMapper;
  // }

  @Value("${iamport.api.key}")
  private String iamportApiKey;
  
  @Value("${iamport.api.secret}")
  private String iamportApiSecret;
  
  @Value("${iamport.api.url}")
  private String iamportApiUrl;

  @Override
  public String getPortOneAccessToken() {
    try {
      log.info("포트원 액세스 토큰 발급 요청");
      
      // API 키 확인
      if (iamportApiKey == null || iamportApiKey.isEmpty() || iamportApiSecret == null || iamportApiSecret.isEmpty()) {
        log.error("포트원 API 키 또는 시크릿이 설정되지 않았습니다");
        throw new PaymentException("포트원 API 키가 설정되지 않았습니다");
      }
      
      // 토큰 요청 헤더 및 본문 설정
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      
      String requestBody = String.format(
        "{\"imp_key\":\"%s\",\"imp_secret\":\"%s\"}", 
        iamportApiKey, 
        iamportApiSecret
      );
      
      // 요청 엔티티 생성
      HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
      String url = iamportApiUrl + "/users/getToken";
      
      log.info("포트원 토큰 요청 URL: {}", url);
      
      // API 호출
      ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, entity, String.class);
      String response = responseEntity.getBody();
      
      if (response == null || response.isEmpty()) {
        log.error("포트원 토큰 발급 응답이 비어있습니다");
        throw new PaymentException("포트원 토큰 발급 응답이 비어있습니다");
      }
      
      log.debug("포트원 토큰 응답: {}", response);
      
      // 응답 파싱
      JsonNode root = objectMapper.readTree(response);
      
      // 응답 코드가 0이 아닐 경우 오류
      if (root.get("code").asInt(1) != 0) {
        log.error("포트원 토큰 발급 실패: {}", root.get("message").asText());
        throw new PaymentException("포트원 토큰 발급 실패: " + root.get("message").asText());
      }
      
      // 액세스 토큰 추출 및 반환
      JsonNode responseNode = root.get("response");
      if (responseNode == null || responseNode.isNull() || !responseNode.has("access_token")) {
        log.error("포트원 응답에 토큰 정보가 없습니다");
        throw new PaymentException("포트원 응답에 토큰 정보가 없습니다");
      }
      
      String accessToken = responseNode.get("access_token").asText();
      log.info("포트원 액세스 토큰 발급 성공");
      
      return accessToken;
    } catch (HttpClientErrorException e) {
      log.error("포트원 토큰 발급 HTTP 오류: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
      throw new PaymentException("포트원 토큰 발급 중 HTTP 오류 발생: " + e.getMessage());
    } catch (Exception e) {
      log.error("포트원 토큰 발급 실패: " + e.getMessage(), e);
      throw new PaymentException("포트원 토큰 발급 중 오류 발생: " + e.getMessage());
    }
  }
  
  @Override
  public void processRefund(Long subscriptionId) {
    // 해당 구독의 가장 최근 결제 조회
    Payment payment = paymentRepository.findTopBySubscriptionIdAndPaymentStatusOrderByPaymentDateDesc(
      subscriptionId, PaymentStatus.COMPLETE)
      .orElseThrow(() -> new ResourceNotFoundException("환불할 결제 정보를 찾을 수 없습니다: " + subscriptionId));
    
    try {
      // 이미 환불된 경우 예외 발생
      if (payment.isRefunded()) {
        throw new PaymentException("이미 환불된 결제입니다.");
      }
      
      // 포트원 환불 요청
      String token = getIamportAccessToken();
      
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
      headers.set("Authorization", token);
      
      MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
      map.add("imp_uid", payment.getImpUid());
      map.add("amount", payment.getAmount().toString());
      map.add("reason", "구독 취소로 인한 환불");
      
      HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
      String url = iamportApiUrl + "/payments/cancel";
      
      String response = restTemplate.postForObject(url, entity, String.class);
      JsonNode root = objectMapper.readTree(response);
      
      // 응답 코드가 0이 아닐 경우 오류
      if (root.get("code").asInt(1) != 0) {
        throw new PaymentException("포트원 결제 환불 실패: " + root.get("message").asText());
      }
      
      // 환불 처리
      payment.refund(payment.getAmount(), LocalDateTime.now());
      paymentRepository.save(payment);
      
      log.info("결제 환불 처리 완료: subscriptionId = {}, paymentId = {}, amount = {}", 
        subscriptionId, payment.getId(), payment.getAmount());
    } catch (Exception e) {
      log.error("결제 환불 처리 실패: " + e.getMessage(), e);
      throw new PaymentException("결제 환불 처리 중 오류 발생: " + e.getMessage());
    }
  }

  @Override
  public void verifyPayment(String impUid, String merchantUid, BigDecimal amount) {
    try {
      log.info("결제 검증 시작: impUid={}, merchantUid={}, amount={}", impUid, merchantUid, amount);
      
      // 매번 새로운 토큰을 발급받아 사용 (캐싱된 토큰을 사용하지 않음)
      String token = getPortOneAccessToken();
      log.info("포트원 액세스 토큰 발급 성공");
      
      if (token == null || token.isEmpty()) {
        throw new PaymentVerificationException("포트원 액세스 토큰 발급 실패");
      }
      
      // 포트원 결제 정보 조회
      HttpHeaders headers = new HttpHeaders();
      headers.set("Authorization", token);
      
      HttpEntity<String> entity = new HttpEntity<>(headers);
      String url = iamportApiUrl + "/payments/" + impUid;
      
      log.info("포트원 결제 정보 요청: URL={}", url);
      ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
      String response = responseEntity.getBody();
      
      if (response == null || response.isEmpty()) {
        throw new PaymentVerificationException("포트원 결제 정보 응답이 비어있습니다");
      }
      
      log.info("포트원 결제 정보 응답: {}", response);
      JsonNode root = objectMapper.readTree(response);
      
      // 응답 코드가 0이 아닐 경우 오류
      if (root.get("code").asInt(1) != 0) {
        throw new PaymentVerificationException("포트원 결제 정보 조회 실패: " + root.get("message").asText());
      }
      
      // 결제 정보 확인
      JsonNode payment = root.get("response");
      if (payment == null || payment.isNull()) {
        throw new PaymentVerificationException("포트원 응답에 결제 정보가 없습니다");
      }
      
      String paymentImpUid = payment.get("imp_uid").asText();
      String paymentMerchantUid = payment.get("merchant_uid").asText();
      BigDecimal paymentAmount = new BigDecimal(payment.get("amount").asText());
      String status = payment.get("status").asText();
      
      log.info("결제 정보 확인: impUid={}, merchantUid={}, amount={}, status={}", 
        paymentImpUid, paymentMerchantUid, paymentAmount, status);
      
      // 결제 정보 일치 여부 확인
      if (!impUid.equals(paymentImpUid)) {
        throw new PaymentVerificationException("포트원 결제 고유번호 불일치: 요청=" + impUid + ", 실제=" + paymentImpUid);
      }
      
      if (!merchantUid.equals(paymentMerchantUid)) {
        throw new PaymentVerificationException("주문번호 불일치: 요청=" + merchantUid + ", 실제=" + paymentMerchantUid);
      }
      
      if (amount.compareTo(paymentAmount) != 0) {
        throw new PaymentVerificationException("결제 금액 불일치: 요청=" + amount + ", 실제=" + paymentAmount);
      }
      
      if (!"paid".equals(status)) {
        throw new PaymentVerificationException("결제 상태 오류: " + status);
      }
      
      log.info("결제 검증 성공: {}, {}, {}", impUid, merchantUid, amount);
    } catch (HttpClientErrorException e) {
      log.error("HTTP 클라이언트 오류: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
      throw new PaymentVerificationException("결제 검증 중 HTTP 오류 발생: " + e.getMessage(), e);
    } catch (Exception e) {
      log.error("결제 검증 실패: " + e.getMessage(), e);
      throw new PaymentVerificationException("결제 검증 중 오류 발생: " + e.getMessage(), e);
    }
  }

  @Override
  public Long registerPaymentMethod(Long userId, PaymentMethodCreateRequestDto requestDto) {
    userRepository.findById(userId)
      .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + userId));
    
    try {
      // 기본 결제 수단으로 설정하는 경우, 기존 기본 결제 수단 해제
      if (requestDto.isDefault()) {
        paymentMethodRepository.findByUserIdAndIsDefaultTrue(userId).ifPresent(existingDefault -> {
          existingDefault.unsetDefault();
          paymentMethodRepository.save(existingDefault);
        });
      }

      PaymentMethodDto paymentMethodDto = PaymentMethodDto.builder()
        .userId(userId)
        .methodType(requestDto.getMethodType())
        .isDefault(requestDto.isDefault())
        .cardName(requestDto.getCardName())
        .cardNumber(requestDto.getCardNumber())
        .cardExpiry(requestDto.getCardExpiry())
        .billingKey(requestDto.getBillingKey())
        .isActive(true)
        .build();

      Long paymentMethodId = paymentMethodRepository.save(toEntity(paymentMethodDto)).getId();
      log.info("결제 수단 등록 완료: userId = {}, paymentMethodId = {}", userId, paymentMethodId);
      
      return paymentMethodId;
    } catch (Exception e) {
      log.error("결제 수단 등록 실패: " + e.getMessage(), e);
      throw new PaymentException("결제 수단 등록 중 오류 발생: " + e.getMessage());
    }
  }

  @Override
  public List<PaymentMethodDto> getPaymentMethods(Long userId) {
    return paymentMethodRepository.findByUserIdAndIsActiveTrue(userId).stream().map(p -> toDto(p)).toList();
  }

  @Override
  public Long deletePaymentMethod(Long userId, Long paymentMethodId) {
    PaymentMethodDto paymentMethodDto = toDto(paymentMethodRepository.findByIdAndUserId(paymentMethodId, userId)
      .orElseThrow(() -> new ResourceNotFoundException("결제 수단을 찾을 수 없습니다: " + paymentMethodId)));

    // 활성 구독에서 사용 중인지 확인
    boolean isInUse = subscriptionRepository.existsByPaymentMethodIdAndStatusIn(
      paymentMethodId, List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.PENDING));
    
    if (isInUse) {
      throw new PaymentException("활성 구독에서 사용 중인 결제 수단은 삭제할 수 없습니다.");
    }

    try {
      // 기본 결제 수단인 경우, 다른 결제 수단을 기본으로 설정
      if (paymentMethodDto.isDefault()) {
        paymentMethodRepository.findFirstByUserIdAndIsActiveTrueAndIdNot(userId, paymentMethodId)
          .ifPresent(newDefault -> {
            newDefault.setAsDefault();
            paymentMethodRepository.save(newDefault);
          });
      }
    
      paymentMethodDto.setActive(false); // 비활성화 설정
      paymentMethodRepository.save(toEntity(paymentMethodDto));
      
      log.info("결제 수단 삭제 완료: userId = {}, paymentMethodId = {}", userId, paymentMethodId);
      return paymentMethodId;
    } catch (Exception e) {
      log.error("결제 수단 삭제 실패: " + e.getMessage(), e);
      throw new PaymentException("결제 수단 삭제 중 오류 발생: " + e.getMessage());
    }
  }

  @Override
  public PaymentReceiptResponseDto getPaymentReceipt(Long userId, String impUid) {
    PaymentDto paymentDto = toPaymentDto(paymentRepository.findByImpUidAndUserId(impUid, userId)
      .orElseThrow(() -> new ResourceNotFoundException("결제 정보를 찾을 수 없습니다: " + impUid)));

    try {
      // 영수증 URL이 이미 있으면 바로 반환
      if (paymentDto.getReceiptUrl() != null && !paymentDto.getReceiptUrl().isEmpty()) {
        return PaymentReceiptResponseDto.builder()
          .impUid(paymentDto.getImpUid())
          .merchantUid(paymentDto.getMerchantUid())
          .receiptUrl(paymentDto.getReceiptUrl())
          .cardName(paymentDto.getCardName())
          .cardNumber(paymentDto.getCardNumber())
          .pgProvider(paymentDto.getPgProvider())
          .build();
      }
      
      // PaymentGatewayService를 사용하여 결제 정보 조회
      PaymentGatewayResponseDto paymentInfo = paymentGatewayService.getPaymentInfo(impUid);
      
      // 영수증 URL 업데이트
      if (paymentInfo.getReceiptUrl() != null) {
        paymentDto.setReceiptUrl(paymentInfo.getReceiptUrl());
        paymentRepository.save(toEntity(paymentDto));
      }
      
      return PaymentReceiptResponseDto.builder()
        .impUid(paymentDto.getImpUid())
        .merchantUid(paymentDto.getMerchantUid())
        .receiptUrl(paymentInfo.getReceiptUrl())
        .cardName(paymentInfo.getCardName() != null ? paymentInfo.getCardName() : paymentDto.getCardName())
        .cardNumber(paymentInfo.getCardNumber() != null ? paymentInfo.getCardNumber() : paymentDto.getCardNumber())
        .pgProvider(paymentDto.getPgProvider())
        .build();
    } catch (Exception e) {
      log.error("영수증 URL 조회 실패: " + e.getMessage(), e);
      throw new PaymentException("영수증 URL 조회 중 오류 발생: " + e.getMessage());
    }
  }

  @Override
  public PaymentResponseDto getPaymentInfo(Long userId, Long paymentId) {
    return toDto(paymentRepository.findByIdAndUserId(paymentId, userId)
      .orElseThrow(() -> new ResourceNotFoundException("결제 정보를 찾을 수 없습니다: " + paymentId)));
  }

  @Override
  public Long logPaymentAction(Long userId, PaymentLogCreateRequestDto requestDto, String ipAddress) {
    userRepository.findById(userId)
      .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + userId));

    try {
      PaymentDto paymentDto = null;

      if (requestDto.getPaymentId() != null) {
        paymentDto = toPaymentDto(paymentRepository.findById(requestDto.getPaymentId())
          .orElse(null));
      }

      PaymentLogDto paymentLogDto = PaymentLogDto.builder()
        .userId(userId)
        .paymentId(paymentDto == null ? null : paymentDto.getId())
        .action(requestDto.getAction())
        .requestData(requestDto.getRequestData())
        .responseData(requestDto.getResponseData())
        .ipAddress(ipAddress)
        .build();

      Long logId = paymentLogRepository.save(toEntity(paymentLogDto)).getId();
      log.info("결제 로그 기록 완료: userId = {}, action = {}, logId = {}", 
        userId, requestDto.getAction(), logId);
      
      return logId;
    } catch (Exception e) {
      log.error("결제 로그 기록 실패: " + e.getMessage(), e);
      throw new PaymentException("결제 로그 기록 중 오류 발생: " + e.getMessage());
    }
  }

  @Override
  public List<PaymentLogResponseDto> getPaymentLogs(Long userId) {
    // 사용자 존재 여부 확인
    if (!userRepository.existsById(userId)) {
      throw new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + userId);
    }
    
    // 사용자별 로그 조회 (최신순)
    return paymentLogRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(l -> toLogResponseDto(l)).toList();
  }

  @Override
  public PaymentLogDto getPaymentLogDetail(Long logId) {
    return toLogDto(paymentLogRepository.findById(logId)
      .orElseThrow(() -> new ResourceNotFoundException("결제 로그를 찾을 수 없습니다: " + logId)));
  }

  @Override
  public BigDecimal refundPayment(Long userId, PaymentRefundRequestDto requestDto) {
    // 결제 정보 조회
    PaymentDto paymentDto = toPaymentDto(paymentRepository.findByImpUidAndUserId(requestDto.getImpUid(), userId)
      .orElseThrow(() -> new ResourceNotFoundException("결제 정보를 찾을 수 없습니다: " + requestDto.getImpUid())));

    try {
      // 이미 환불된 경우 예외 발생
      if (paymentDto.isRefunded()) {
        throw new PaymentException("이미 환불된 결제입니다.");
      }
      
      // 환불 금액 검증
      if (requestDto.getRefundAmount().compareTo(paymentDto.getAmount()) > 0) {
        throw new PaymentException("환불 금액이 결제 금액보다 클 수 없습니다.");
      }
      
      // PaymentGatewayService를 사용하여 환불 요청
      PaymentGatewayResponseDto refundResult = paymentGatewayService.refundPayment(
        paymentDto.getImpUid(),
        requestDto.getRefundAmount(),
        requestDto.getReason()
      );
      
      if (!refundResult.isCancelled()) {
        throw new PaymentException("포트원 결제 환불 실패: " + (refundResult.getErrorMsg() != null ? refundResult.getErrorMsg() : "알 수 없는 오류"));
      }
      
      // 환불 처리 상태 업데이트
      if (requestDto.getRefundAmount().compareTo(paymentDto.getAmount()) == 0) {
        // 전액 환불
        paymentDto.setPaymentStatus(PaymentStatus.REFUNDED);
      } else {
        // 부분 환불
        paymentDto.setPaymentStatus(PaymentStatus.PARTIAL_REFUNDED);
      }
      
      // refundAmount와 refundDate 설정
      paymentDto.setRefundAmount(requestDto.getRefundAmount());
      paymentDto.setRefundDate(LocalDateTime.now());
      
      // 수정된 DTO를 엔티티로 변환하여 저장
      paymentRepository.save(toEntity(paymentDto));
      
      // 구독 취소 요청이 있으면 처리
      if (requestDto.isCancelSubscription() && requestDto.getSubscriptionId() != null) {
        Subscription subscription = subscriptionRepository.findById(requestDto.getSubscriptionId())
          .orElseThrow(() -> new ResourceNotFoundException("구독 정보를 찾을 수 없습니다: " + requestDto.getSubscriptionId()));
        
        subscription.cancel();
        subscription.setEndDate(LocalDateTime.now());
        subscriptionRepository.save(subscription);
        
        log.info("구독 취소 처리 완료: subscriptionId={}", requestDto.getSubscriptionId());
      }
      
      log.info("결제 환불 처리 완료: userId={}, impUid={}, amount={}", 
        userId, requestDto.getImpUid(), requestDto.getRefundAmount());
      
      return requestDto.getRefundAmount();
    } catch (Exception e) {
      log.error("결제 환불 처리 실패: " + e.getMessage(), e);
      throw new PaymentException("결제 환불 처리 중 오류 발생: " + e.getMessage());
    }
  }
  
  /**
   * 포트원 API 액세스 토큰 발급
   * 
   * @return 액세스 토큰
   */
  private String getIamportAccessToken() {
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
      return accessToken;
    } catch (Exception e) {
      log.error("포트원 토큰 발급 실패: " + e.getMessage(), e);
      throw new PaymentException("포트원 토큰 발급 중 오류 발생: " + e.getMessage());
    }
  }
}
