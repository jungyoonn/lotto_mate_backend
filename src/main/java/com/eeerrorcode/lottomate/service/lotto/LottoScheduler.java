package com.eeerrorcode.lottomate.service.lotto;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.eeerrorcode.lottomate.domain.dto.lotto.LottoResultResponse;

// import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@RequiredArgsConstructor
@Log4j2
public class LottoScheduler {

  private final LottoCrawlerService lottoCrawlerService;
  private final LottoResultService lottoResultService;

  /**
   * 매주 토요일 21시 실행: 최신 회차 자동 업데이트
   */
  @Scheduled(cron = "0 0 21 * * SAT", zone = "Asia/Seoul")
  public void crawlLatestRoundIfNeeded() {
    try {
      LottoResultResponse before = lottoResultService.getLottoStatus();
      Long beforeRound = before.getDrawRound();
      Long expectedRound = beforeRound + 1;

      log.info("[스케줄러 시작] 현재 회차: {}", beforeRound);

      lottoCrawlerService.crawlLatest();

      LottoResultResponse after = lottoResultService.getLottoStatus();
      Long afterRound = after.getDrawRound();

      if (afterRound.equals(expectedRound)) {
        log.info("[크롤링 성공] {}회차가 정상적으로 추가되었습니다", afterRound);
      } else {
        log.warn("⚠ [크롤링 미적용] 회차가 여전히 {}입니다. 수동 확인 필요!", afterRound);
      }

    } catch (Exception e) {
      log.error("[스케줄러 실패] 예외 발생", e);
    }
  }

  // @PostConstruct 
  // public void testCrawlOnce() {
  //   log.info("[테스트] 강제 실행 시작");
  //   crawlLatestRoundIfNeeded();
  // }
}
