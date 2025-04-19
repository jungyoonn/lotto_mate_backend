package com.eeerrorcode.lottomate.service.subscription;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eeerrorcode.lottomate.domain.dto.subscription.*;
import com.eeerrorcode.lottomate.repository.payment.SubscriptionRepository;

import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;

@Service
@Transactional
@Log4j2
public class SubscriptionServiceImpl implements SubscriptionService {
  @Autowired
  private SubscriptionRepository subscriptionRepository;

  @Override
  public SubscriptionResponseDto getSubscriptionInfo(Long userId) {
    throw new UnsupportedOperationException("Unimplemented method 'getSubscriptionInfo'");
  }

  @Override
  public Long createSubscription(Long userId, SubscriptionCreateRequestDto requestDto) {
    throw new UnsupportedOperationException("Unimplemented method 'createSubscription'");
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
