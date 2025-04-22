package com.eeerrorcode.lottomate.service.lotto;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import com.eeerrorcode.lottomate.domain.entity.lotto.LottoResultDetails;
import com.eeerrorcode.lottomate.domain.entity.lotto.LottoResults;
import com.eeerrorcode.lottomate.repository.lotto.LottoResultDetailsRepository;
import com.eeerrorcode.lottomate.repository.lotto.LottoResultRepository;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class LottoCrawlerServiceImpl implements LottoCrawlerService {

  private final LottoResultDetailsRepository lottoResultDetailsRepository;
  private final LottoResultRepository lottoResultRepository;

  @Override
  public void crawlAndSave(WebDriver driver, int round) {
    String url = "https://dhlottery.co.kr/gameResult.do?method=byWin&drwNo=" + round;
    driver.get(url);

    try {
      Thread.sleep(500);
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
      log.info("크롤링 중 오류 발생");
    }
    try {
      if (lottoResultRepository.existsByDrawRound((long) round)) {
        log.info("{}회차는 이미 존재합니다. 건너뜁니다.", round);
        return;
      }

      List<WebElement> lottoResultBalls = driver.findElements(By.cssSelector(".win_result .nums span.ball_645"));
      Integer n1 = Integer.parseInt(lottoResultBalls.get(0).getText());
      Integer n2 = Integer.parseInt(lottoResultBalls.get(1).getText());
      Integer n3 = Integer.parseInt(lottoResultBalls.get(2).getText());
      Integer n4 = Integer.parseInt(lottoResultBalls.get(3).getText());
      Integer n5 = Integer.parseInt(lottoResultBalls.get(4).getText());
      Integer n6 = Integer.parseInt(lottoResultBalls.get(5).getText());
      Integer bonus = Integer
          .parseInt(driver.findElement(By.cssSelector(".win_result .bonus span.ball_645")).getText());

      LocalDateTime drawDate = null;
      try {
        String rawDate = driver.findElement(By.cssSelector(".win_result .desc")).getText().replaceAll("[^0-9년월일 ]", "")
            .trim();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
        drawDate = LocalDate.parse(rawDate, formatter).atStartOfDay();
      } catch (Exception e) {
        log.info("[{}회차] 날짜 파싱 실패: {}", round, e.getMessage());
        return;
      }

      String winnerStr = driver.findElement(By.cssSelector(".tbl_data tr:nth-of-type(2) td:nth-of-type(2)")).getText()
          .replaceAll("[^0-9]", "");
      String amountStr = driver.findElement(By.cssSelector(".tbl_data tr:nth-of-type(2) td:nth-of-type(3)")).getText()
          .replaceAll("[^0-9]", "");
      Long winners = Long.parseLong(winnerStr);
      Long firstPrizeAmount = Long.parseLong(amountStr);

      Long totalSales = 0L;

      try {
        String salesStr = driver.findElement(By.cssSelector(".win_result .desc")).getText().replaceAll("[^0-9]", "");
        totalSales = Long.parseLong(salesStr);
      } catch (Exception e) {
        log.info(round + "파싱 오류 발생");
      }

      LottoResults returnResults = LottoResults
          .builder()
          .drawRound(Long.valueOf(round))
          .drawDate(drawDate)
          .n1(n1).n2(n2).n3(n3).n4(n4).n5(n5).n6(n6)
          .bonusNumber(bonus)
          .firstPrizeWinners(winners)
          .firstPrizeAmount(firstPrizeAmount)
          .totalSalesAmount(totalSales)
          .build();

      lottoResultRepository.save(returnResults);

      List<LottoResultDetails> details = new ArrayList<>();
      List<WebElement> rows = driver.findElements(By.cssSelector(".tbl_data tbody tr"));

      Integer availableRows = rows.size();
      for (int i = 1; i < availableRows && i <= 5; i++) {
        List<WebElement> cols = rows.get(i).findElements(By.tagName("td"));
        if (cols.size() < 4)
          continue;

        int rank = Integer.parseInt(cols.get(0).getText().replaceAll("[^0-9]", ""));
        long winnerCount = Long.parseLong(cols.get(2).getText().replaceAll("[^0-9]", ""));
        long prizeAmount = Long.parseLong(cols.get(3).getText().replaceAll("[^0-9]", ""));

        LottoResultDetails detail = LottoResultDetails
            .builder()
            .lottoResults(returnResults)
            .rank(rank)
            .winnerCount(winnerCount)
            .prizeAmount(prizeAmount)
            .build();

        details.add(detail);
      }

      lottoResultDetailsRepository.saveAll(details);
      log.info("{} 회차 저장 완료", round);
    } catch (Exception e) {
      log.info("{} 회차 저장 중 오류 발생", round, e.getMessage(), e);
    }

  }


  @Override
  public void crawlAll() {
    WebDriverManager.chromedriver().setup();
    WebDriver driver = new ChromeDriver();

    try {
      Integer latest = getLatestRound();
      if (latest == -1) {
        log.info("최신 회차 정보 없음");
        return;
      }
      for (int round = 1; round <= latest; round++) {
        if (lottoResultRepository.existsByDrawRound(Long.valueOf(round))) {
          log.info("{} 회차 중복, 넘어갑니다", round);
          continue;
        }
        crawlAndSave(driver, round);
      }
      log.info("{} 회차까지 크롤링 완료", latest);
    } catch (Exception e) {
      log.info("크롤링 중 오류 발생", e.getMessage(), e);
    } finally {
      driver.quit();
    }
  }


  @Override
  public void crawlLatest() {
    WebDriverManager.chromedriver().setup();
    WebDriver driver = new ChromeDriver();

    try {
      Integer latest = getLatestRound();
      if (latest == -1) {
        return;
      }
      if (lottoResultRepository.existsByDrawRound((long) latest)) {
        log.info(latest + "회차 존재");
        return;
      }
      crawlAndSave(driver, latest);
    } catch (Exception e) {
      log.info("크롤링 오류 발생");
    } finally {
      driver.quit();
    }
  }

  @Override
  public Integer getLatestRound() {
    WebDriver driver = new ChromeDriver();
    try {
      driver.get("https://dhlottery.co.kr/gameResult.do?method=byWin");

      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

      WebElement roundElement = wait.until(
          ExpectedConditions.presenceOfElementLocated(By.cssSelector(".win_result h4 strong")));

      int latest = Integer.parseInt(roundElement.getText().replaceAll("[^0-9]", ""));
      log.info("크롤링 대상 최신 회차 번호: {}", latest);
      return latest;

    } catch (Exception e) {
      log.warn("크롤링 대상 사이트 최신 회차 정보 로딩 실패: {}", e.getMessage());
      return -1;
    } finally {
      driver.quit();
    }
  }

@Override
public Long crawlLatestRound() {
  WebDriverManager.chromedriver().setup();
  WebDriver driver = new ChromeDriver();

  try {
    Integer latest = getLatestRound();
    if (latest == -1 || lottoResultRepository.existsByDrawRound((long) latest)) {
      log.info("[{}회차] 이미 존재하거나 유효하지 않음. 건너뜀.", latest);
      return null;
    }

    crawlAndSave(driver, latest);
    return (long) latest;

  } catch (Exception e) {
    log.error("[크롤링 실패] 예외 발생", e);
    return null;

  } finally {
    driver.quit();
  }
}

}
