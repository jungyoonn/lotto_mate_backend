package com.eeerrorcode.lottomate.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.eeerrorcode.lottomate.domain.dto.payment.PaymentLogCreateRequestDto;
import com.eeerrorcode.lottomate.domain.dto.subscription.*;
import com.eeerrorcode.lottomate.domain.entity.user.User;
import com.eeerrorcode.lottomate.exeption.ResourceNotFoundException;
import com.eeerrorcode.lottomate.repository.UserRepository;
import com.eeerrorcode.lottomate.security.CustomUserDetails;
import com.eeerrorcode.lottomate.security.JwtTokenProvider;
import com.eeerrorcode.lottomate.service.payment.PaymentService;
import com.eeerrorcode.lottomate.service.subscription.SubscriptionPlanService;
import com.eeerrorcode.lottomate.service.subscription.SubscriptionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;


@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/subscription")
@Tag(name = "Subscription API", description = "구독 관련 기능을 테스트할 수 있는 API입니다")
public class SubscriptionController {
  private final SubscriptionService subscriptionService;
  private final SubscriptionPlanService subscriptionPlanService;
  private final PaymentService paymentService;
  private final JwtTokenProvider jwtUtil;
  private final UserRepository userRepository;

  @Operation(
    summary = "활성화된 구독 플랜 목록 조회",
    description = "현재 활성화된 모든 구독 플랜을 조회합니다.",
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "구독 플랜 목록 조회 성공",
        content = @Content(schema = @Schema(implementation = List.class))
      )
    }
  )
  @GetMapping("/plans/active")
  public ResponseEntity<?> getActivePlans() {
    List<SubscriptionPlanDto> activePlans = subscriptionPlanService.getAllActivePlans();
    return ResponseEntity.ok(activePlans);
  }

  @Operation(
    summary = "결제 검증 및 구독 활성화",
    description = "포트원 결제 후 결제 검증을 수행하고 구독을 활성화합니다.",
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "결제 검증 및 구독 활성화 성공",
        content = @Content(schema = @Schema(implementation = Long.class))
      ),
      @ApiResponse(responseCode = "400", description = "결제 검증 실패"),
      @ApiResponse(responseCode = "401", description = "인증 실패")
    }
  )
  @PostMapping("/verify-payment")
  public ResponseEntity<?> verifyPaymentAndCreateSubscription(
    @Valid @RequestBody SubscriptionVerifyPaymentRequestDto requestDto,
    @RequestHeader(value = "X-PortOne-Token", required = false) String portOneToken,
    @RequestHeader(value = "Authorization", required = false) String authHeader,
    HttpServletRequest request
  ) {
    Long userId = null;
    
    // JWT 토큰에서 사용자 ID 추출 시도
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7);
      try {
        // JWT 토큰 파싱하여 userId 추출
        userId = jwtUtil.getUserIdFromToken(token);
        log.info("토큰에서 추출한 userId: {}", userId);
      } catch (Exception e) {
        log.warn("토큰에서 userId 추출 실패: {}", e.getMessage());
      }
    }
    
    // userId가 없는 경우 requestDto에서 이메일 정보를 사용하여 사용자 조회
    if (userId == null && requestDto.getUserEmail() != null) {
      try {
        User user = userRepository.findByEmail(requestDto.getUserEmail())
          .orElseThrow(() -> new ResourceNotFoundException("해당 이메일의 사용자를 찾을 수 없습니다: " + requestDto.getUserEmail()));
        userId = user.getId();
        log.info("이메일로 조회한 userId: {}", userId);
      } catch (Exception e) {
        log.error("이메일로 사용자 조회 실패: {}", e.getMessage());
        return ResponseEntity.badRequest().body("사용자 정보를 찾을 수 없습니다. 로그인 후 다시 시도해주세요.");
      }
    }
    
    if (userId == null) {
      log.error("사용자 ID를 확인할 수 없습니다.");
      return ResponseEntity.badRequest().body("사용자 인증에 실패했습니다. 로그인 후 다시 시도해주세요.");
    }
    
    try {
      // 포트원 토큰이 없는 경우 발급 시도
      if (portOneToken == null || portOneToken.isEmpty()) {
        portOneToken = paymentService.getPortOneAccessToken();
      }
      
      // 결제 검증 요청 로그 기록
      try {
        paymentService.logPaymentAction(
          userId, 
          PaymentLogCreateRequestDto.builder()
            .action(com.eeerrorcode.lottomate.domain.entity.payment.PaymentLogAction.PAYMENT_ATTEMPT)
            .requestData(requestDto.toString())
            .build(),
          request.getRemoteAddr()
        );
      } catch (Exception e) {
        log.warn("결제 로그 기록 실패: {}", e.getMessage());
        // 로그 기록 실패는 주요 기능을 차단하지 않도록 예외 처리
      }
      
      // 결제 검증 및 구독 활성화
      Long subscriptionId = subscriptionService.verifyPaymentAndActivateSubscription(userId, requestDto);
      
      // 결제 성공 로그 기록
      try {
        paymentService.logPaymentAction(
          userId, 
          PaymentLogCreateRequestDto.builder()
            .action(com.eeerrorcode.lottomate.domain.entity.payment.PaymentLogAction.PAYMENT_SUCCESS)
            .paymentId(subscriptionId)
            .build(),
          request.getRemoteAddr()
        );
      } catch (Exception e) {
        log.warn("결제 성공 로그 기록 실패: {}", e.getMessage());
        // 로그 기록 실패는 주요 기능을 차단하지 않도록 예외 처리
      }
      
      log.info("결제 검증 및 구독 활성화 성공: userId = {}, subscriptionId = {}", userId, subscriptionId);
      return ResponseEntity.ok(subscriptionId);
    } catch (Exception e) {
      // 결제 실패 로그 기록
      try {
        if (userId != null) {
          paymentService.logPaymentAction(
            userId, 
            PaymentLogCreateRequestDto.builder()
              .action(com.eeerrorcode.lottomate.domain.entity.payment.PaymentLogAction.PAYMENT_FAILED)
              .responseData("Error: " + e.getMessage())
              .build(),
            request.getRemoteAddr()
          );
        }
      } catch (Exception logError) {
        log.warn("결제 실패 로그 기록 실패: {}", logError.getMessage());
      }
      
      log.error("결제 검증 실패: " + e.getMessage(), e);
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }
  
  @Operation(
    summary = "구독 정보 조회",
    description = "현재 로그인한 사용자의 구독 정보를 조회합니다.",
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "구독 정보 조회 성공",
        content = @Content(schema = @Schema(implementation = SubscriptionResponseDto.class))
      ),
      @ApiResponse(responseCode = "204", description = "구독 정보 없음"),
      @ApiResponse(responseCode = "401", description = "인증 실패")
    }
  )
  @GetMapping("/info")
  public ResponseEntity<?> getSubscriptionInfo(
    @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    Long userId = userDetails.getUser().getId();
    SubscriptionResponseDto subscription = subscriptionService.getSubscriptionInfo(userId);
    
    if (subscription == null) {
      return ResponseEntity.noContent().build(); // 204 No Content
    }
    
    return ResponseEntity.ok(subscription);
  }
  
  @Operation(
    summary = "구독 생성",
    description = "새로운 구독을 생성합니다. 결제 전 구독 정보를 먼저 생성합니다.",
    responses = {
      @ApiResponse(
        responseCode = "201",
        description = "구독 생성 성공",
        content = @Content(schema = @Schema(implementation = Long.class))
      ),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "401", description = "인증 실패")
    }
  )
  @PostMapping
  public ResponseEntity<?> createSubscription(
    @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
    @Valid @RequestBody SubscriptionCreateRequestDto requestDto
  ) {
    Long userId = userDetails.getUser().getId();
    Long subscriptionId = subscriptionService.createSubscription(userId, requestDto);
    
    return ResponseEntity.status(HttpStatus.CREATED).body(subscriptionId);
  }

  @Operation(
    summary = "구독 취소",
    description = "현재 활성화된 구독을 취소합니다.",
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "구독 취소 성공",
        content = @Content(schema = @Schema(implementation = Long.class))
      ),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "404", description = "구독 정보 없음")
    }
  )
  @PostMapping("/cancel")
  public ResponseEntity<?> cancelSubscription(
    @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
    @Valid @RequestBody SubscriptionCancelRequestDto requestDto
  ) {
    Long userId = userDetails.getUser().getId();
    Long subscriptionId = subscriptionService.cancelSubscription(userId, requestDto);
    
    log.info("구독 취소 성공: userId = {}, subscriptionId = {}", userId, subscriptionId);
    return ResponseEntity.ok(subscriptionId);
  }
  
  @Operation(
    summary = "구독 상세 정보 조회",
    description = "결제 완료 후 구독 상세 정보를 조회합니다.",
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "구독 상세 정보 조회 성공",
        content = @Content(schema = @Schema(implementation = SubscriptionDetailsResponseDto.class))
      ),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "404", description = "구독 정보 없음")
    }
  )
  @GetMapping("/details")
  public ResponseEntity<?> getSubscriptionDetails(
    @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
    @RequestParam String imp_uid,
    @RequestParam(required = false) String email,
    HttpServletRequest request
  ) {
    try {
      Long userId = null;
      
      // 인증된 사용자가 있는 경우 ID 추출
      if (userDetails != null) {
        userId = userDetails.getUser().getId();
        log.info("인증된 사용자의 구독 정보 요청: userId={}, impUid={}", userId, imp_uid);
      } else {
        log.info("인증되지 않은 요청으로 구독 정보 조회: impUid={}, email={}", imp_uid, email);
        
        // 이메일이 제공된 경우 사용자 조회
        if (email != null && !email.isEmpty()) {
          User user = userRepository.findByEmail(email)
            .orElse(null);
          
          if (user != null) {
            userId = user.getId();
            log.info("이메일로 사용자 조회 성공: userId={}", userId);
          } else {
            log.warn("이메일로 사용자 조회 실패: email={}", email);
          }
        }
        
        // JWT 토큰에서 사용자 ID 추출 시도 (Authorization 헤더 확인)
        if (userId == null) {
          String authHeader = request.getHeader("Authorization");
          if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
              userId = jwtUtil.getUserIdFromToken(token);
              log.info("토큰에서 사용자 ID 추출 성공: userId={}", userId);
            } catch (Exception e) {
              log.warn("토큰에서 사용자 ID 추출 실패: {}", e.getMessage());
            }
          }
        }
      }
      
      // userId가 있든 없든 구독 정보 조회 시도
      SubscriptionDetailsResponseDto details = subscriptionService.getSubscriptionDetails(userId, imp_uid);
      log.info("구독 상세 정보 조회 성공: impUid={}, subscriptionId={}", imp_uid, details.getId());
      
      return ResponseEntity.ok(details);
    } catch (ResourceNotFoundException e) {
      log.error("구독 정보를 찾을 수 없음: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    } catch (Exception e) {
      log.error("구독 상세 정보 조회 중 오류 발생: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body("구독 정보 조회 중 오류가 발생했습니다: " + e.getMessage());
    }
  }

  @Operation(
    summary = "자동 갱신 설정 변경",
    description = "구독의 자동 갱신 설정을 변경합니다.",
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "자동 갱신 설정 변경 성공",
        content = @Content(schema = @Schema(implementation = Long.class))
      ),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "404", description = "구독 정보 없음")
    }
  )
  @PutMapping("/{subscriptionId}/auto-renewal")
  public ResponseEntity<?> updateAutoRenewal(
    @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
    @PathVariable Long subscriptionId,
    @RequestParam boolean autoRenewal
  ) {
    Long userId = userDetails.getUser().getId();
    Long updatedSubscriptionId = subscriptionService.updateAutoRenewal(userId, subscriptionId, autoRenewal);
    
    log.info("자동 갱신 설정 변경 성공: userId = {}, subscriptionId = {}, autoRenewal = {}", 
      userId, subscriptionId, autoRenewal);
    return ResponseEntity.ok(updatedSubscriptionId);
  }

  @Operation(
    summary = "플랜 변경",
    description = "구독의 플랜을 변경합니다. 다음 결제일에 적용됩니다.",
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "플랜 변경 요청 성공",
        content = @Content(schema = @Schema(implementation = Long.class))
      ),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "404", description = "구독 정보 없음")
    }
  )
  @PutMapping("/{subscriptionId}/plan")
  public ResponseEntity<?> changePlan(
    @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
    @PathVariable Long subscriptionId,
    @RequestParam String planName
  ) {
    Long userId = userDetails.getUser().getId();
    Long updatedSubscriptionId = subscriptionService.changePlan(userId, subscriptionId, planName);
    
    log.info("플랜 변경 요청 성공: userId = {}, subscriptionId = {}, newPlan = {}", 
      userId, subscriptionId, planName);
    return ResponseEntity.ok(updatedSubscriptionId);
  }

  @Operation(
    summary = "결제 수단 변경",
    description = "구독의 결제 수단을 변경합니다.",
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "결제 수단 변경 성공",
        content = @Content(schema = @Schema(implementation = Long.class))
      ),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "404", description = "구독 정보 없음")
    }
  )
  @PutMapping("/{subscriptionId}/payment-method")
  public ResponseEntity<?> changePaymentMethod(
    @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
    @PathVariable Long subscriptionId,
    @RequestParam Long paymentMethodId
  ) {
    Long userId = userDetails.getUser().getId();
    Long updatedSubscriptionId = subscriptionService.changePaymentMethod(userId, subscriptionId, paymentMethodId);
    
    log.info("결제 수단 변경 성공: userId = {}, subscriptionId = {}, paymentMethodId = {}", 
      userId, subscriptionId, paymentMethodId);
    return ResponseEntity.ok(updatedSubscriptionId);
  }
}
