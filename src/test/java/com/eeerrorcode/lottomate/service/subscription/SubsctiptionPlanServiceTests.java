package com.eeerrorcode.lottomate.service.subscription;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.log4j.Log4j2;

@SpringBootTest
@Log4j2
public class SubsctiptionPlanServiceTests {
  @Autowired
  private SubscriptionPlanService service;

  @Test
  public void testFindAllIsActive() {
    log.info(service.getAllActivePlans());
  }
}
