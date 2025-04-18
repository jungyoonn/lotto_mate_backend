package com.eeerrorcode.lottomate.repository.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.eeerrorcode.lottomate.domain.entity.payment.Subscription;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long>{
  
}
