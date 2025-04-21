package com.eeerrorcode.lottomate.service.payment;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import com.eeerrorcode.lottomate.domain.dto.payment.*;
import com.eeerrorcode.lottomate.domain.entity.payment.*;
import com.eeerrorcode.lottomate.domain.entity.user.User;

public interface PaymentService {
  void processRefund(Long subscriptionId);
  
   /**
   * 포트원 결제 검증 (가맹점 결제 검증)
   * 
   * @param impUid 포트원 결제 고유번호
   * @param merchantUid 주문번호
   * @param amount 결제 금액
   */
  void verifyPayment(String impUid, String merchantUid, BigDecimal amount);
    
  /**
   * 결제 수단 등록
   * 
   * @param userId 사용자 ID
   * @param requestDto 결제 수단 등록 요청 DTO
   * @return 등록된 결제 수단 ID
   */
  Long registerPaymentMethod(Long userId, PaymentMethodCreateRequestDto requestDto);
  
  /**
   * 결제 수단 목록 조회
   * 
   * @param userId 사용자 ID
   * @return 결제 수단 목록
   */
  List<PaymentMethodDto> getPaymentMethods(Long userId);
  
  /**
   * 결제 수단 삭제 (비활성화)
   * 
   * @param userId 사용자 ID
   * @param paymentMethodId 결제 수단 ID
   * @return 삭제된 결제 수단 ID
   */
  Long deletePaymentMethod(Long userId, Long paymentMethodId);
  
  /**
   * 결제 영수증 URL 조회
   * 
   * @param userId 사용자 ID
   * @param impUid 포트원 결제 고유번호
   * @return 영수증 URL DTO
   */
  PaymentReceiptResponseDto getPaymentReceipt(Long userId, String impUid);
  
  /**
   * 결제 정보 조회
   * 
   * @param userId 사용자 ID
   * @param paymentId 결제 ID
   * @return 결제 정보 DTO
   */
  PaymentResponseDto getPaymentInfo(Long userId, Long paymentId);
  
  /**
   * 결제 로그 기록
   * 
   * @param userId 사용자 ID
   * @param requestDto 로그 생성 요청 DTO
   * @param ipAddress IP 주소
   * @return 생성된 로그 ID
   */
  Long logPaymentAction(Long userId, PaymentLogCreateRequestDto requestDto, String ipAddress);
  
  /**
   * 결제 로그 조회
   * 
   * @param userId 사용자 ID
   * @return 결제 로그 목록
   */
  List<PaymentLogResponseDto> getPaymentLogs(Long userId);
  
  /**
   * 관리자용 결제 로그 상세 조회
   * 
   * @param logId 로그 ID
   * @return 결제 로그 상세 정보
   */
  PaymentLogDto getPaymentLogDetail(Long logId);
  
  /**
   * 결제 환불 처리
   * 
   * @param userId 사용자 ID
   * @param requestDto 환불 요청 DTO
   * @return 환불된 금액
   */
  BigDecimal refundPayment(Long userId, PaymentRefundRequestDto requestDto);
  
  
  /**
   * Payment 엔티티를 PaymentResponseDTO로 변환
   * 
   * @param payment 결제 엔티티
   * @return 결제 응답 DTO
   */
  default PaymentResponseDto toDto(Payment payment) {
    if (payment == null) {
      return null;
    }
      
    PaymentResponseDto dto = PaymentResponseDto.builder()
      .id(payment.getId())
      .amount(payment.getAmount())
      .paymentMethod(payment.getPaymentMethod())
      .paymentStatus(payment.getPaymentStatus().getDisplayName())
      .merchantUid(payment.getMerchantUid())
      .impUid(payment.getImpUid())
      .paymentDate(payment.getPaymentDate())
      .receiptUrl(payment.getReceiptUrl())
      .cardName(payment.getCardName())
      .cardNumber(payment.getCardNumber())
      .refundAmount(payment.getRefundAmount())
      .refundDate(payment.getRefundDate())
      .pgProvider(payment.getPgProvider())
      .build();
          
    // 구독 정보가 있으면 설정
    if (payment.getSubscription() != null) {
      dto.setSubscriptionId(payment.getSubscription().getId());
    }
    
    return dto;
  }
  
  /**
   * PaymentMethod 엔티티를 PaymentMethodDTO로 변환
   * 
   * @param paymentMethod 결제 수단 엔티티
   * @return 결제 수단 DTO
   */
  default PaymentMethodDto toDto(PaymentMethod paymentMethod) {
    if (paymentMethod == null) {
      return null;
    }
    
    return PaymentMethodDto.builder()
      .id(paymentMethod.getId())
      .userId(paymentMethod.getUser().getId())
      .methodType(paymentMethod.getMethodType())
      .isDefault(paymentMethod.isDefault())
      .cardName(paymentMethod.getCardName())
      .cardNumber(paymentMethod.getCardNumber())
      .cardExpiry(paymentMethod.getCardExpiry())
      .build();
  }
  
  /**
   * PaymentLog 엔티티를 PaymentLogResponseDTO로 변환
   * 
   * @param log 결제 로그 엔티티
   * @return 결제 로그 응답 DTO
   */
  default PaymentLogResponseDto toLogResponseDto(PaymentLog log) {
    if (log == null) {
      return null;
    }
    
    PaymentLogResponseDto dto = PaymentLogResponseDto.builder()
      .id(log.getId())
      .action(log.getAction().getDisplayName())
      .ipAddress(log.getIpAddress())
      .build();
          
    // 결제 ID 설정
    if (log.getPayment() != null) {
      dto.setPaymentId(log.getPayment().getMerchantUid());
    }
      
    // 날짜 포맷팅
    dto.setFormattedDateFromDateTime(log.getCreatedAt());
    
    return dto;
  }
  
  /**
   * PaymentLog 엔티티를 PaymentLogDTO로 변환 (관리자용)
   * 
   * @param log 결제 로그 엔티티
   * @return 결제 로그 DTO
   */
  default PaymentLogDto toLogDto(PaymentLog log) {
    if (log == null) {
      return null;
    }
    
    PaymentLogDto dto = PaymentLogDto.builder()
      .id(log.getId())
      .userId(log.getUser().getId())
      .action(log.getAction())
      .requestData(log.getRequestData())
      .responseData(log.getResponseData())
      .ipAddress(log.getIpAddress())
      .createdAt(log.getCreatedAt())
      .build();
        
    // 결제 ID 설정
    if (log.getPayment() != null) {
      dto.setPaymentId(log.getPayment().getId());
    }
      
    // 액션 표시명 설정
    dto.setActionDisplayFromAction();
    
    return dto;
  }
  
  /**
   * PaymentLog 엔티티 리스트를 PaymentLogResponseDTO 리스트로 변환
   * 
   * @param logs 결제 로그 엔티티 리스트
   * @return 결제 로그 응답 DTO 리스트
   */
  default List<PaymentLogResponseDto> toLogResponseDtoList(List<PaymentLog> logs) {
    if (logs == null) {
      return null;
    }
    
    return logs.stream()
      .map(this::toLogResponseDto)
      .collect(Collectors.toList());
  }

   /**
   * PaymentMethodDto를 PaymentMethod 엔티티로 변환
   * 
   * @param dto 결제 수단 DTO
   * @return 결제 수단 엔티티
   */
  default PaymentMethod toEntity(PaymentMethodDto dto) {
    if (dto == null) {
      return null;
    }
    
    return PaymentMethod.builder()
      .id(dto.getId())
      .user(User.builder().id(dto.getUserId()).build())
      .methodType(dto.getMethodType())
      .isDefault(dto.isDefault())
      .cardName(dto.getCardName())
      .cardNumber(dto.getCardNumber())
      .cardExpiry(dto.getCardExpiry())
      .billingKey(dto.getBillingKey())
      .isActive(dto.isActive())
      .build();
  }

  /**
   * PaymentDto를 Payment 엔티티로 변환
   * 
   * @param dto Payment dto
   * @return Payment 엔티티
   */
  default Payment toEntity(PaymentDto dto) {
    if (dto == null) {
      return null;
    }

    return Payment.builder()
      .id(dto.getId())
      .user(User.builder().id(dto.getUserId()).build())
      .subscription(Subscription.builder().id(dto.getSubscriptionId()).build())
      .amount(dto.getAmount())
      .paymentMethod(dto.getPaymentMethod())
      .paymentStatus(dto.getPaymentStatus())
      .merchantUid(dto.getMerchantUid())
      .impUid(dto.getImpUid())
      .paymentDate(dto.getPaymentDate())
      .refundAmount(dto.getRefundAmount())
      .refundDate(dto.getRefundDate())
      .pgProvider(dto.getPgProvider())
      .cardName(dto.getCardName())
      .cardNumber(dto.getCardNumber())
      .bankName(dto.getBankName())
      .accountNumber(dto.getAccountNumber())
      .receiptUrl(dto.getReceiptUrl())
      .build();
  }

  /**
   * Payment 엔티티를 PaymentDto로 변환
   * 
   * @param dto Payment dto
   * @return Payment 엔티티
   */
  default PaymentDto toPaymentDto(Payment payment) {
    if (payment == null) {
      return null;
    }

    return PaymentDto.builder()
      .id(payment.getId())
      .userId(payment.getUser().getId())
      .subscriptionId(payment.getSubscription().getId())
      .amount(payment.getAmount())
      .paymentMethod(payment.getPaymentMethod())
      .paymentStatus(payment.getPaymentStatus())
      .merchantUid(payment.getMerchantUid())
      .impUid(payment.getImpUid())
      .paymentDate(payment.getPaymentDate())
      .refundAmount(payment.getRefundAmount())
      .refundDate(payment.getRefundDate())
      .pgProvider(payment.getPgProvider())
      .cardName(payment.getCardName())
      .cardNumber(payment.getCardNumber())
      .bankName(payment.getBankName())
      .accountNumber(payment.getAccountNumber())
      .receiptUrl(payment.getReceiptUrl())
      .build();
  }

  /**
   * PaymentLogDto를 PaymentLog 엔티티로 변환
   * 
   * @param dto PaymentLog dto
   * @return PaymentLog 엔티티
   */
  default PaymentLog toEntity(PaymentLogDto dto) {
    if (dto == null) {
      return null;
    }

    return PaymentLog.builder()
      .id(dto.getId())
      .user(User.builder().id(dto.getUserId()).build())
      .payment(Payment.builder().id(dto.getPaymentId()).build())
      .action(dto.getAction())
      .requestData(dto.getRequestData())
      .responseData(dto.getResponseData())
      .ipAddress(dto.getIpAddress())
      .build();
  }
}
