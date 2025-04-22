package com.eeerrorcode.lottomate.service.lotto;

import org.openqa.selenium.WebDriver;

/**
 * 로또 데이터 크롤링을 위한 서비스 인터페이스입니다.
 * 동행복권 사이트에서 회차별 데이터를 가져와 저장하는 기능을 정의합니다.
 */
public interface LottoCrawlerService {

  /**
   * 동행복권 사이트의 최신 회차 번호를 조회하여 해당 회차만 크롤링 및 DB 저장을 수행합니다.
   * 이미 저장된 경우 크롤링을 수행하지 않습니다.
   */
  public void crawlLatest();

  /**
   * 동행복권 사이트에서 최신 회차 번호를 추출하여 반환합니다.
   * 
   * @return 크롤링 가능한 최신 회차 번호 (실패 시 -1 반환)
   */
  public Integer getLatestRound();

  /**
   * 지정된 회차의 로또 당첨 정보를 동행복권 사이트에서 크롤링하여 DB에 저장합니다.
   * 
   * @param driver Selenium WebDriver 인스턴스
   * @param round  크롤링할 회차 번호
   */
  public void crawlAndSave(WebDriver driver, int Round);

  /**
   * 로또 1회차부터 최신 회차까지 전체 데이터를 동행복권 사이트에서 순차적으로 크롤링하고 DB에 저장합니다.
   * 중복된 회차는 건너뛰며, 모든 회차를 자동으로 처리합니다.
   */
  public void crawlAll();

  Long crawlLatestRound();
}
