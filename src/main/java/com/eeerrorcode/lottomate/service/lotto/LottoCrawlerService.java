package com.eeerrorcode.lottomate.service.lotto;

import org.openqa.selenium.WebDriver;

public interface LottoCrawlerService {

  // 크론 활용한 최신 추첨 번호 가져오기(스케줄러)
  public void crawlLatest();

  // 최신 추첨 번호 가져오기 위한 최신 회차 정보 가져오기(동행복권 크롤링)
  public Integer getLatestRound();

  // 크롤링한 정보 기반 저장 및 쿼리 업데이트(동행복권 크롤링)
  public void crawlAndSave(WebDriver driver, int Round);

  public void crawlAll();
} 
