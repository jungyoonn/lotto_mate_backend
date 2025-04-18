package com.eeerrorcode.lottomate.service.lotto;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.log4j.Log4j2;

@SpringBootTest
@Log4j2
public class LottoServiceTests {
  
  @Autowired
  private LottoResultService lottoResultService;

  @Test
  public void testgetNumberDistribution(){
    Map<Long, Long> testResult = lottoResultService.getNumberDistribution(1000);
    log.info("번호 분포 결과: {}", testResult);
  }

}
