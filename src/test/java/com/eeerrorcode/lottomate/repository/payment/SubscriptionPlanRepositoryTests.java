package com.eeerrorcode.lottomate.repository.payment;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.eeerrorcode.lottomate.domain.entity.payment.SubscriptionPlan;

import lombok.extern.log4j.Log4j2;

@SpringBootTest
@Log4j2
public class SubscriptionPlanRepositoryTests {
  @Autowired
  private SubscriptionPlanRepository repository;

  @Test
  public void testRepositoryExists() {
    assertNotNull(repository);
  }

  @Test
  public void testFindByActive() {
    List<SubscriptionPlan> plans = repository.findByIsActiveTrue();

    log.info(plans);
  }
}
