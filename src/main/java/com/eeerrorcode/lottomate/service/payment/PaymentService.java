package com.eeerrorcode.lottomate.service.payment;

import java.math.BigDecimal;

public interface PaymentService {

  void verifyPayment(String impUid, String merchantUid, BigDecimal amount);

  void processRefund(Long subscriptionId);
  
}
