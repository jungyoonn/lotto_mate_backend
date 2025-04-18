package com.eeerrorcode.lottomate.repository.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.eeerrorcode.lottomate.domain.entity.payment.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
  
}
