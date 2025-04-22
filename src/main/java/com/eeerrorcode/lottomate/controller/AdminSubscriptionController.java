package com.eeerrorcode.lottomate.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.eeerrorcode.lottomate.domain.dto.subscription.*;
import com.eeerrorcode.lottomate.service.subscription.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
@RequestMapping("/api/admin/subscription")
// @PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Subscription API", description = "관리자용 구독 관리 API")
public class AdminSubscriptionController {
  private final SubscriptionService subscriptionService;
  private final SubscriptionPlanService subscriptionPlanService;

  @Operation(
    summary = "관리자 - 모든 구독 플랜 조회",
    description = "활성화 및 비활성화 상태 포함 모든 구독 플랜을 조회합니다.",
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = SubscriptionPlanDto.class)))
      ),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "403", description = "권한 없음")
    }
  )
  @GetMapping("/plans/all")
  public ResponseEntity<List<SubscriptionPlanDto>> getAllPlans() {
    List<SubscriptionPlanDto> plans = subscriptionPlanService.getAllPlans();
    return ResponseEntity.ok(plans);
  }

  @Operation(
    summary = "관리자 - 구독 플랜 등록",
    description = "새로운 구독 플랜을 등록합니다.",
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "등록 성공",
        content = @Content(schema = @Schema(implementation = SubscriptionPlanDto.class))
      ),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "403", description = "권한 없음")
    }
  )
  @PostMapping("/plans")
  public ResponseEntity<SubscriptionPlanDto> createPlan(@Valid @RequestBody SubscriptionPlanDto planDto) {
    Long planId = subscriptionPlanService.createPlan(planDto);
    SubscriptionPlanDto createdPlan = subscriptionPlanService.getPlanById(planId);
    
    log.info("구독 플랜 등록 완료: id = {}, name = {}", planId, createdPlan.getName());
    return ResponseEntity.ok(createdPlan);
  }

  @Operation(
    summary = "관리자 - 구독 플랜 수정",
    description = "기존 구독 플랜을 수정합니다.",
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "수정 성공",
        content = @Content(schema = @Schema(implementation = SubscriptionPlanDto.class))
      ),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "플랜 없음")
    }
  )
  @PutMapping("/plans/{planId}")
  public ResponseEntity<SubscriptionPlanDto> updatePlan(
      @PathVariable Long planId, 
      @Valid @RequestBody SubscriptionPlanDto planDto) {
      
    SubscriptionPlanDto updatedPlan = subscriptionPlanService.updatePlan(planId, planDto);
      
    log.info("구독 플랜 수정 완료: id = {}, name = {}", planId, updatedPlan.getName());
    return ResponseEntity.ok(updatedPlan);
  }

  @Operation(
    summary = "관리자 - 구독 플랜 삭제",
    description = "구독 플랜을 삭제(비활성화)합니다. 현재 사용 중인 플랜은 삭제할 수 없습니다.",
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "삭제 성공",
        content = @Content(schema = @Schema(implementation = Boolean.class))
      ),
      @ApiResponse(responseCode = "400", description = "삭제 실패"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "403", description = "권한 없음"),
      @ApiResponse(responseCode = "404", description = "플랜 없음")
    }
  )
  @DeleteMapping("/plans/{planId}")
  public ResponseEntity<Boolean> deletePlan(@PathVariable Long planId) {
    boolean result = subscriptionPlanService.deletePlan(planId);
    
    if (result) {
      log.info("구독 플랜 삭제(비활성화) 완료: id = {}", planId);
      return ResponseEntity.ok(true);
    } else {
      log.warn("구독 플랜 삭제 실패: id = {} (사용 중인 플랜)", planId);
      return ResponseEntity.badRequest().body(false);
    }
  }
    
  @Operation(
    summary = "관리자 - 모든 구독 정보 조회",
    description = "모든 사용자의 구독 정보를 조회합니다.",
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = SubscriptionResponseDto.class)))
      ),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "403", description = "권한 없음")
    }
  )
  @GetMapping("/list")
  public ResponseEntity<List<SubscriptionResponseDto>> getAllSubscriptions() {
    List<SubscriptionResponseDto> subscriptions = subscriptionService.getAllSubscriptionsForAdmin();
    return ResponseEntity.ok(subscriptions);
  }
    
  @Operation(
    summary = "관리자 - 모든 구독 취소 요청 조회",
    description = "처리되지 않은 모든 구독 취소 요청을 조회합니다.",
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = SubscriptionCancellationResponseDto.class)))
      ),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "403", description = "권한 없음")
    }
  )
  @GetMapping("/cancellations")
  public ResponseEntity<List<SubscriptionCancellationResponseDto>> getAllCancellationRequests() {
    List<SubscriptionCancellationResponseDto> cancellations = subscriptionService.getAllCancellationRequestsForAdmin();
    return ResponseEntity.ok(cancellations);
  }
    
  @Operation(
    summary = "관리자 - 구독 취소 요청 처리",
    description = "구독 취소 요청을 승인 또는 거부합니다.",
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "처리 성공",
        content = @Content(schema = @Schema(implementation = Long.class))
      ),
      @ApiResponse(responseCode = "400", description = "처리 실패"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "403", description = "권한 없음"),
      @ApiResponse(responseCode = "404", description = "취소 요청 없음")
    }
  )
  @PutMapping("/cancellations/process")
  public ResponseEntity<Long> processCancellationRequest(
      @Valid @RequestBody SubscriptionCancellationAdminRequestDto requestDto) {
      
    Long cancellationId = subscriptionService.processCancellationAdmin(requestDto);
    
    log.info("구독 취소 요청 처리 완료: cancellationId = {}, adminProcessed = {}", 
      cancellationId, requestDto.isAdminProcessed());
    return ResponseEntity.ok(cancellationId);
  }
}
