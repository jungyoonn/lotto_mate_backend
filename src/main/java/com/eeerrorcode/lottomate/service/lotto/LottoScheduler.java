package com.eeerrorcode.lottomate.service.lotto;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.eeerrorcode.lottomate.domain.dto.lotto.LottoResultResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;


/**
 * 로또 스케줄러 컴포넌트.
 * <p>
 * 매주 토요일 밤 21시에 동행복권 사이트에서 최신 로또 회차 정보를 자동으로 크롤링하고
 * DB에 저장합니다. 크롤링 성공 여부는 이전/이후 회차 정보를 비교하여 확인합니다.
 * </p>
 * <p>
 * 또한, 수동 실행이 필요한 경우 {@link com.eeerrorcode.lottomate.controller.admin.LottoAdminController}
 * 의 API를 통해 수동 호출이 가능합니다.
 * </p>
 *
 * @author DahnDell
 * @see LottoCrawlerService
 * @see LottoResultService
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class LottoScheduler {

  private final LottoCrawlerService lottoCrawlerService;
  private final LottoResultService lottoResultService;
  private final LottoHistoryService lottoHistoryService;

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

  @Scheduled(cron = "0 10 21 * * SAT", zone = "Asia/Seoul")
  public void crawlAndEvaluateLatestRound() {
  try {
    Long newRound = lottoCrawlerService.crawlLatestRound();
    if (newRound != null) {
      log.info("[스케줄러] {}회차 크롤링 성공. 사용자 응모 결과 평가 시작", newRound);
      lottoHistoryService.updateWinningResults(newRound);
    } else {
      log.warn("[스케줄러] 회차 없음 또는 이미 존재. 결과 평가 생략됨");
    }
  } catch (Exception e) {
    log.error("[스케줄러 오류] 크롤링 및 결과 평가 실패", e);
  }
}
}
