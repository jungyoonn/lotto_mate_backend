package com.eeerrorcode.lottomate.service.lotto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.eeerrorcode.lottomate.domain.dto.lotto.LottoRecommendOption;
import com.eeerrorcode.lottomate.domain.dto.lotto.LottoRecommendResponse;
import com.eeerrorcode.lottomate.domain.enums.LottoRange;
import com.eeerrorcode.lottomate.domain.enums.LottoRecommendSystem;

import lombok.extern.log4j.Log4j2;

@SpringBootTest
@Log4j2
public class LottoRecommendTests {

  @Autowired
  private LottoRecommendService lottoRecommendService;

  @Test
  void testRecommend100() {
    LottoRecommendOption option = LottoRecommendOption.builder()
        .range(LottoRange.RECENT_100)
        .mode(LottoRecommendSystem.HIGH_FREQUENCY)
        .allowEvenOddMix(true)
        .includeBonusNumber(true)
        .build();

    LottoRecommendResponse result = lottoRecommendService.recommendNumbers(option);
    List<Long> numbers = result.getNumbers();

    log.info("추천 번호: {}", numbers);

    assertThat(numbers).hasSize(6);
    assertThat(numbers).allMatch(n -> n >= 1 && n <= 45);
  }

}
