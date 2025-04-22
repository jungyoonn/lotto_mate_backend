package com.eeerrorcode.lottomate.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.eeerrorcode.lottomate.domain.dto.subscription.*;
import com.eeerrorcode.lottomate.security.CustomUserDetails;
import com.eeerrorcode.lottomate.service.subscription.SubscriptionPlanService;
import com.eeerrorcode.lottomate.service.subscription.SubscriptionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
    @Valid @RequestBody SubscriptionVerifyPaymentRequestDto requestDto
  ) {
    Long userId = userDetails.getUser().getId();
    Long subscriptionId = subscriptionService.verifyPaymentAndActivateSubscription(userId, requestDto);
    
    log.info("결제 검증 및 구독 활성화 성공: userId = {}, subscriptionId = {}", userId, subscriptionId);
    return ResponseEntity.ok(subscriptionId);
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
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "404", description = "구독 정보 없음")
    }
  )
  @GetMapping("/details")
  public ResponseEntity<?> getSubscriptionDetails(
    @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
    @RequestParam String imp_uid
  ) {
    Long userId = userDetails.getUser().getId();
    SubscriptionDetailsResponseDto details = subscriptionService.getSubscriptionDetails(userId, imp_uid);
    
    return ResponseEntity.ok(details);
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
