package com.eeerrorcode.lottomate.repository.payment;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.log4j.Log4j2;

@SpringBootTest
@Log4j2
public class SubscriptionRepositoryTests {
  @Autowired
  private SubscriptionRepository repository;

  @Test
  public void testRepositoryExists() {
    assertNotNull(repository);
  }
}
