package com.eeerrorcode.lottomate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.eeerrorcode.lottomate.domain.dto.subscription.*;
import com.eeerrorcode.lottomate.security.CustomUserDetails;
import com.eeerrorcode.lottomate.service.subscription.SubscriptionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/subscription/cancellation")
@Tag(name = "Subscription Cancellation API", description = "구독 취소 이력 관련 기능을 테스트할 수 있는 API입니다")
@Log4j2
public class SubscriptionCancellationController {
  private final SubscriptionService subscriptionService;

  @Operation(
    summary = "구독 취소 이력 생성",
    description = "구독 취소 이력을 생성합니다.",
    responses = {
      @ApiResponse(
        responseCode = "201",
        description = "취소 이력 생성 성공",
        content = @Content(schema = @Schema(implementation = Long.class))
      ),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "404", description = "구독 정보 없음")
    }
  )
  @PostMapping
  public ResponseEntity<?> createCancellation(
    @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
    @Valid @RequestBody SubscriptionCancellationRequestDto requestDto
  ) {
    Long userId = userDetails.getUser().getId();
    Long cancellationId = subscriptionService.createCancellation(userId, requestDto);
    
    log.info("구독 취소 이력 생성 성공: userId = {}, subscriptionId = {}, cancellationId = {}", 
      userId, requestDto.getSubscriptionId(), cancellationId);
    return ResponseEntity.status(HttpStatus.CREATED).body(cancellationId);
  }
  
  @Operation(
    summary = "구독 취소 이력 목록 조회",
    description = "사용자의 구독 취소 이력 목록을 조회합니다.",
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "취소 이력 목록 조회 성공",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = SubscriptionCancellationResponseDto.class)))
      ),
      @ApiResponse(responseCode = "401", description = "인증 실패")
    }
  )
  @GetMapping
  public ResponseEntity<?> getCancellationHistory(
    @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    Long userId = userDetails.getUser().getId();
    List<SubscriptionCancellationResponseDto> cancellations = subscriptionService.getCancellationHistory(userId);
    
    return ResponseEntity.ok(cancellations);
  }
  
  @Operation(
    summary = "구독 취소 이력 상세 조회",
    description = "구독 취소 이력의 상세 정보를 조회합니다.",
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "취소 이력 상세 조회 성공",
        content = @Content(schema = @Schema(implementation = SubscriptionCancellationDto.class))
      ),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "404", description = "취소 이력 없음")
    }
  )
  @GetMapping("/{cancellationId}")
  public ResponseEntity<?> getCancellationDetail(
    @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
    @PathVariable Long cancellationId
  ) {
    Long userId = userDetails.getUser().getId();
    SubscriptionCancellationDto cancellation = subscriptionService.getCancellationDetail(userId, cancellationId);
    
    return ResponseEntity.ok(cancellation);
  }
}
