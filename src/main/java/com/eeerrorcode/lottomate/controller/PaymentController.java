package com.eeerrorcode.lottomate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.eeerrorcode.lottomate.domain.dto.payment.*;
import com.eeerrorcode.lottomate.security.CustomUserDetails;
import com.eeerrorcode.lottomate.service.payment.PaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
@Tag(name = "Payment API", description = "결제 관련 기능을 테스트할 수 있는 API입니다")
@Log4j2
public class PaymentController {
  private final PaymentService paymentService;

  @Operation(
    summary = "결제 수단 등록",
    description = "새로운 결제 수단을 등록합니다.",
    responses = {
      @ApiResponse(
        responseCode = "201",
        description = "결제 수단 등록 성공",
        content = @Content(schema = @Schema(implementation = Long.class))
      ),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "401", description = "인증 실패")
    }
  )
  @PostMapping("/methods")
  public ResponseEntity<?> registerPaymentMethod(
    @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
    @Valid @RequestBody PaymentMethodCreateRequestDto requestDto
  ) {
    Long userId = userDetails.getUser().getId();
    Long paymentMethodId = paymentService.registerPaymentMethod(userId, requestDto);
    
    log.info("결제 수단 등록 성공: userId={}, paymentMethodId={}", userId, paymentMethodId);
    return ResponseEntity.status(HttpStatus.CREATED).body(paymentMethodId);
  }

  @Operation(
    summary = "결제 수단 목록 조회",
    description = "사용자의 모든 결제 수단을 조회합니다.",
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "결제 수단 목록 조회 성공",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = PaymentMethodDto.class)))
      ),
      @ApiResponse(responseCode = "401", description = "인증 실패")
    }
  )
  @GetMapping("/methods")
  public ResponseEntity<?> getPaymentMethods(
    @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    Long userId = userDetails.getUser().getId();
    List<PaymentMethodDto> paymentMethods = paymentService.getPaymentMethods(userId);
    
    return ResponseEntity.ok(paymentMethods);
  }

  @Operation(
    summary = "결제 수단 삭제",
    description = "결제 수단을 삭제(비활성화)합니다.",
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "결제 수단 삭제 성공",
        content = @Content(schema = @Schema(implementation = Long.class))
      ),
      @ApiResponse(responseCode = "400", description = "결제 수단 삭제 실패"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "404", description = "결제 수단 없음")
    }
  )
  @DeleteMapping("/methods/{paymentMethodId}")
  public ResponseEntity<?> deletePaymentMethod(
    @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
    @PathVariable Long paymentMethodId
  ) {
    Long userId = userDetails.getUser().getId();
    Long deletedPaymentMethodId = paymentService.deletePaymentMethod(userId, paymentMethodId);
    
    log.info("결제 수단 삭제 성공: userId = {}, paymentMethodId = {}", userId, deletedPaymentMethodId);
    return ResponseEntity.ok(deletedPaymentMethodId);
  }

  @Operation(
    summary = "결제 영수증 조회",
    description = "결제 영수증 URL을 조회합니다.",
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "결제 영수증 조회 성공",
        content = @Content(schema = @Schema(implementation = PaymentReceiptResponseDto.class))
      ),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "404", description = "결제 정보 없음")
    }
  )
  @GetMapping("/receipt")
  public ResponseEntity<?> getPaymentReceipt(
    @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
    @RequestParam String imp_uid
  ) {
    Long userId = userDetails.getUser().getId();
    PaymentReceiptResponseDto receipt = paymentService.getPaymentReceipt(userId, imp_uid);
    
    return ResponseEntity.ok(receipt);
  }

  @Operation(
    summary = "결제 정보 조회",
    description = "특정 결제 정보를 조회합니다.",
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "결제 정보 조회 성공",
        content = @Content(schema = @Schema(implementation = PaymentResponseDto.class))
      ),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "404", description = "결제 정보 없음")
    }
  )
  @GetMapping("/{paymentId}")
  public ResponseEntity<?> getPaymentInfo(
    @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
    @PathVariable Long paymentId
  ) {
    Long userId = userDetails.getUser().getId();
    PaymentResponseDto payment = paymentService.getPaymentInfo(userId, paymentId);
    
    return ResponseEntity.ok(payment);
  }

  @Operation(
    summary = "결제 로그 기록",
    description = "결제 관련 로그를 기록합니다.",
    responses = {
      @ApiResponse(
        responseCode = "201",
        description = "결제 로그 기록 성공",
        content = @Content(schema = @Schema(implementation = Long.class))
      ),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "401", description = "인증 실패")
    }
  )
  @PostMapping("/logs")
  public ResponseEntity<?> logPaymentAction(
    @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
    @Valid @RequestBody PaymentLogCreateRequestDto requestDto,
    HttpServletRequest request
  ) {
    Long userId = userDetails.getUser().getId();
    String ipAddress = request.getRemoteAddr();
    
    Long logId = paymentService.logPaymentAction(userId, requestDto, ipAddress);
    
    return ResponseEntity.status(HttpStatus.CREATED).body(logId);
  }

  @Operation(
    summary = "결제 로그 조회",
    description = "사용자의 결제 로그 목록을 조회합니다.",
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "결제 로그 조회 성공",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = PaymentLogResponseDto.class)))
      ),
      @ApiResponse(responseCode = "401", description = "인증 실패")
    }
  )
  @GetMapping("/logs")
  public ResponseEntity<?> getPaymentLogs(
    @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    Long userId = userDetails.getUser().getId();
    List<PaymentLogResponseDto> logs = paymentService.getPaymentLogs(userId);
    
    return ResponseEntity.ok(logs);
  }

  @Operation(
    summary = "결제 환불 요청",
    description = "결제 환불을 요청합니다.",
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "결제 환불 요청 성공",
        content = @Content(schema = @Schema(implementation = BigDecimal.class))
      ),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "404", description = "결제 정보 없음")
    }
  )
  @PostMapping("/refund")
  public ResponseEntity<?> refundPayment(
    @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
    @Valid @RequestBody PaymentRefundRequestDto requestDto
  ) {
    Long userId = userDetails.getUser().getId();
    BigDecimal refundedAmount = paymentService.refundPayment(userId, requestDto);
    
    log.info("결제 환불 요청 성공: userId = {}, impUid = {}, amount = {}", 
      userId, requestDto.getImpUid(), refundedAmount);
    return ResponseEntity.ok(refundedAmount);
  }
}
