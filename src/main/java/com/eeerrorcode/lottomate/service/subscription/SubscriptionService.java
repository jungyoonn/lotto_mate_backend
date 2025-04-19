package com.eeerrorcode.lottomate.service.subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.eeerrorcode.lottomate.domain.dto.payment.*;
import com.eeerrorcode.lottomate.domain.dto.subscription.*;
import com.eeerrorcode.lottomate.domain.entity.payment.*;
import com.eeerrorcode.lottomate.domain.entity.user.User;

public interface SubscriptionService {
  /**
   * 사용자의 구독 정보 조회
   * 
   * @param userId 사용자 ID
   * @return 구독 정보 응답 Dto
   */
  SubscriptionResponseDto getSubscriptionInfo(Long userId);
  
  /**
   * 구독 생성
   * 
   * @param userId 사용자 ID
   * @param requestDTO 구독 생성 요청 Dto
   * @return 생성된 구독 ID
   */
  Long createSubscription(Long userId, SubscriptionCreateRequestDto requestDto);
  
  /**
   * 결제 확인 및 구독 활성화
   * 
   * @param userId 사용자 ID
   * @param requestDTO 결제 검증 요청 Dto
   * @return 구독 ID
   */
  Long verifyPaymentAndActivateSubscription(Long userId, SubscriptionVerifyPaymentRequestDto requestDto);
  
  /**
   * 구독 취소
   * 
   * @param userId 사용자 ID
   * @param requestDTO 취소 요청 Dto
   * @return 취소된 구독 ID
   */
  Long cancelSubscription(Long userId, SubscriptionCancelRequestDto requestDto);
  
  /**
   * 구독 상세 정보 조회
   * 
   * @param userId 사용자 ID
   * @param impUid 포트원 결제 고유번호
   * @return 구독 상세 정보 응답 Dto
   */
  SubscriptionDetailsResponseDto getSubscriptionDetails(Long userId, String impUid);
  
  /**
   * 사용자의 모든 구독 정보 조회
   * 
   * @param userId 사용자 ID
   * @return 구독 정보 리스트
   */
  List<SubscriptionResponseDto> getAllSubscriptions(Long userId);
  
  /**
   * 자동 결제 갱신 설정 변경
   * 
   * @param userId 사용자 ID
   * @param subscriptionId 구독 ID
   * @param autoRenewal 자동 갱신 여부
   * @return 변경된 구독 ID
   */
  Long updateAutoRenewal(Long userId, Long subscriptionId, boolean autoRenewal);
  
  /**
   * 구독 플랜 변경
   * 
   * @param userId 사용자 ID
   * @param subscriptionId 현재 구독 ID
   * @param newPlanName 새 플랜 이름 (basic, standard, premium)
   * @return 변경된 구독 ID
   */
  Long changePlan(Long userId, Long subscriptionId, String newPlanName);
  
  /**
   * 결제 수단 변경
   * 
   * @param userId 사용자 ID
   * @param subscriptionId 구독 ID
   * @param paymentMethodId 새 결제 수단 ID
   * @return 변경된 구독 ID
   */
  Long changePaymentMethod(Long userId, Long subscriptionId, Long paymentMethodId);


  /**
   * SubscriptionDto를 Subscription 엔티티로 변환
   * 
   * @param dto 구독 dto
   * @return 구독 엔티티
   */
  default Subscription toEntity(SubscriptionDto dto) {
    if (dto == null) {
      return null;
    }

    return Subscription.builder()
      .id(dto.getId())
      .user(User.builder().id(dto.getUserId()).build())
      .plan(SubscriptionPlan.builder().id(dto.getPlanId()).build())
      .status(dto.getStatus())
      .startDate(dto.getStartDate())
      .endDate(dto.getEndDate())
      .autoRenewal(dto.isAutoRenewal())
      .nextPaymentDate(dto.getNextPaymentDate())
      .billingKey(dto.getBillingKey())
      .paymentMethod(this.toEntity(dto.getPaymentMethod()))
      .build();
  }

  /**
   * PaymentMethodDto를 PaymentMethod 엔티티로 변환
   * 
   * @param dto PaymentMethod dto
   * @return PaymentMethod 엔티티
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

  default SubscriptionDto toDto(Subscription subscription) {
    if (subscription == null) {
      return null;
    }
  
    // 결제 수단 정보 변환
    PaymentMethodDto paymentMethodDto = null;
    if (subscription.getPaymentMethod() != null) {
      paymentMethodDto = toDto(subscription.getPaymentMethod());
    }
  
    return SubscriptionDto.builder()
      .id(subscription.getId())
      .userId(subscription.getUser().getId())
      .planId(subscription.getPlan().getId())
      .planName(subscription.getPlan().getName())
      .status(subscription.getStatus())
      .startDate(subscription.getStartDate())
      .endDate(subscription.getEndDate())
      .autoRenewal(subscription.isAutoRenewal())
      .nextPaymentDate(subscription.getNextPaymentDate())
      .billingKey(subscription.getBillingKey())
      .paymentMethod(paymentMethodDto)
      .build();
  }

  /**
   * PaymentMethod 엔티티를 PaymentMethodDto로 변환
   * 
   * @param paymentMethod 결제 수단 엔티티
   * @return 결제 수단 Dto
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
   * Payment 엔티티를 PaymentResponseDto로 변환
   * 
   * @param payment 결제 엔티티
   * @return 결제 응답 Dto
   */
  default PaymentResponseDto toPaymentResponseDto(Payment payment) {
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
      .build();
        
    // 구독 정보가 있으면 설정
    if (payment.getSubscription() != null) {
      dto.setSubscriptionId(payment.getSubscription().getId());
    }
    
    return dto;
  }

  /**
   * Subscription 엔티티 리스트를 SubscriptionResponseDto 리스트로 변환
   * 
   * @param subscriptions 구독 엔티티 리스트
   * @return 구독 응답 Dto 리스트
   */
  default List<SubscriptionResponseDto> toResponseDtoList(List<Subscription> subscriptions) {
    if (subscriptions == null) {
      return new ArrayList<>();
    }
    
    return subscriptions.stream()
      .map(this::toResponseDto)
      .collect(Collectors.toList());
  }

  /**
   * Subscription 엔티티를 SubscriptionResponseDto로 변환
   * 
   * @param subscription 구독 엔티티
   * @return 구독 응답 Dto
   */
  default SubscriptionResponseDto toResponseDto(Subscription subscription) {
    if (subscription == null) {
      return null;
    }
    
    SubscriptionPlan plan = subscription.getPlan();
    
    // 구독 기간 (monthly/yearly) 결정
    String period = plan.getDurationMonths() <= 1 ? "monthly" : "yearly";
    
    // 결제 수단 정보 변환
    PaymentMethodDto paymentMethodDto = toDto(subscription.getPaymentMethod());
    
    return SubscriptionResponseDto.builder()
      .id(subscription.getId())
      .plan(plan.getName().toLowerCase()) // basic, standard, premium 등
      .period(period)
      .status(subscription.getStatus())
      .startDate(subscription.getStartDate())
      .endDate(subscription.getEndDate())
      .nextPaymentDate(subscription.getNextPaymentDate())
      .autoRenewal(subscription.isAutoRenewal())
      .price(plan.getPrice())
      .paymentMethod(paymentMethodDto)
      .build();
  }

  /**
   * Payment 엔티티를 PaymentResponseDto로 변환
   * 
   * @param payment 결제 엔티티
   * @return 결제 응답 Dto
   */
  default PaymentResponseDto toDto(Payment payment) {
    if (payment == null) {
      return null;
    }

    return PaymentResponseDto.builder()
      .id(payment.getId())
      .amount(payment.getAmount())
      .paymentMethod(payment.getPaymentMethod())
      .paymentStatus(payment.getPaymentStatus().getDisplayName())
      .merchantUid(payment.getMerchantUid())
      .impUid(payment.getImpUid())
      .paymentDate(payment.getPaymentDate())
      .receiptUrl(payment.getReceiptUrl())
      .build();
  }
}
