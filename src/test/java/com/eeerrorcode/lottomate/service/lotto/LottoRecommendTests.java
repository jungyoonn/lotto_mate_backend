package com.eeerrorcode.lottomate.service.lotto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.eeerrorcode.lottomate.domain.enums.LottoRange;

import lombok.extern.log4j.Log4j2;

@SpringBootTest
@Log4j2
public class LottoRecommendTests {
  @Autowired
  private LottoRecommendService lottoRecommendService;

  @Test
  public void testRecommend100() {
    List<Long> result = lottoRecommendService.recommendNumbers(LottoRange.RECENT_100);
    log.info("추천 번호: {}", result);

    assertThat(result).hasSize(6);
    assertThat(result).allMatch(num -> num >= 1 && num <= 45); 
  }
}
