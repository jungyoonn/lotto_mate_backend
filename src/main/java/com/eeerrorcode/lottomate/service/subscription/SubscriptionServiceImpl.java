package com.eeerrorcode.lottomate.service.subscription;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eeerrorcode.lottomate.domain.dto.payment.*;
import com.eeerrorcode.lottomate.domain.dto.subscription.*;
import com.eeerrorcode.lottomate.domain.entity.payment.*;
import com.eeerrorcode.lottomate.exeption.ResourceNotFoundException;
import com.eeerrorcode.lottomate.repository.UserRepository;
import com.eeerrorcode.lottomate.repository.payment.*;

import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;

@Service
@Transactional
@Log4j2
public class SubscriptionServiceImpl implements SubscriptionService {
  @Autowired
  private SubscriptionRepository subscriptionRepository;
  @Autowired
  private PaymentRepository paymentRepository;
  @Autowired
  private SubscriptionPlanRepository subscriptionPlanRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private PaymentMethodRepository paymentMethodRepository;

  @Override
  public SubscriptionResponseDto getSubscriptionInfo(Long userId) {
    // 활성 상태의 구독 정보 조희
    List<SubscriptionResponseDto> subscriptions = toResponseDtoList(subscriptionRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, SubscriptionStatus.ACTIVE));
        
    if (subscriptions.isEmpty()) {
      // 활성 구독이 없을 경우, 취소된 구독 조회
      subscriptions = toResponseDtoList(subscriptionRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, SubscriptionStatus.CANCELLED));
      if (subscriptions.isEmpty()) {
        return null; // 구독 정보 없음
      }
    }

    // 가장 최근 구독 정보, 결제 정보 가져오기
    SubscriptionResponseDto subscription = subscriptions.get(0);
    PaymentResponseDto paymentResponseDto = toDto(paymentRepository.findTopBySubscriptionIdAndPaymentStatusOrderByPaymentDateDesc(
      subscription.getId(), PaymentStatus.COMPLETE).orElse(null));

    PaymentMethodDto paymentMethodDto = subscription.getPaymentMethod();

    subscription.setPaymentMethod(paymentMethodDto);
    subscription.setRecentPayment(paymentResponseDto);

    return subscription;
  }

  @Override
  public Long createSubscription(Long userId, SubscriptionCreateRequestDto requestDto) {
    userRepository.findById(userId)
      .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + userId));

    // 플랜 정보 조회 (basic, standard, premium)
    Long planId = subscriptionPlanRepository.findByNameIgnoreCase(requestDto.getPlan())
      .orElseThrow(() -> new ResourceNotFoundException("구독 플랜을 찾을 수 없습니다: " + requestDto.getPlan())).getId();
          
    // 결제 수단 조회
    PaymentMethodDto paymentMethodDto = null;
    if (requestDto.getPaymentMethodId() != null) {
      paymentMethodDto = toDto(paymentMethodRepository.findByIdAndUserId(requestDto.getPaymentMethodId(), userId)
        .orElseThrow(() -> new ResourceNotFoundException("결제 수단을 찾을 수 없습니다: " + requestDto.getPaymentMethodId())));
    }

    // 구독 기간 설정 (월간/연간)
    int durationMonths = "monthly".equals(requestDto.getPeriod()) ? 1 : 12;
    
    // 시작일, 종료일, 다음 결제일 설정
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime endDate = now.plusMonths(durationMonths);

    SubscriptionDto subscriptionDto = SubscriptionDto.builder()
      .userId(userId)
      .planId(planId)
      .status(SubscriptionStatus.PENDING) // 결제 전이므로 PENDING 상태
      .startDate(now)
      .endDate(endDate)
      .autoRenewal(requestDto.isAutoRenewal())
      .nextPaymentDate(endDate)
      .paymentMethod(paymentMethodDto)
      .build();

    Long subscriptionId = subscriptionRepository.save(toEntity(subscriptionDto)).getId();
    log.info("구독 생성 완료 => {}", subscriptionId);

    return subscriptionId;
  }

  @Override
  public Long verifyPaymentAndActivateSubscription(Long userId, SubscriptionVerifyPaymentRequestDto requestDto) {
    throw new UnsupportedOperationException("Unimplemented method 'verifyPaymentAndActivateSubscription'");
  }

  @Override
  public Long cancelSubscription(Long userId, SubscriptionCancelRequestDto requestDto) {
    throw new UnsupportedOperationException("Unimplemented method 'cancelSubscription'");
  }

  @Override
  public SubscriptionDetailsResponseDto getSubscriptionDetails(Long userId, String impUid) {
    throw new UnsupportedOperationException("Unimplemented method 'getSubscriptionDetails'");
  }

  @Override
  public List<SubscriptionResponseDto> getAllSubscriptions(Long userId) {
    throw new UnsupportedOperationException("Unimplemented method 'getAllSubscriptions'");
  }

  @Override
  public Long updateAutoRenewal(Long userId, Long subscriptionId, boolean autoRenewal) {
    throw new UnsupportedOperationException("Unimplemented method 'updateAutoRenewal'");
  }

  @Override
  public Long changePlan(Long userId, Long subscriptionId, String newPlanName) {
    throw new UnsupportedOperationException("Unimplemented method 'changePlan'");
  }

  @Override
  public Long changePaymentMethod(Long userId, Long subscriptionId, Long paymentMethodId) {
    throw new UnsupportedOperationException("Unimplemented method 'changePaymentMethod'");
  }
  
}
