package com.eeerrorcode.lottomate.service.lotto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class LottoHistoryServiceTests {
    private final LottoHistoryServiceImpl service = new LottoHistoryServiceImpl(null, null);

  @Test
  void testRank1() {
    assertEquals(1, service.calculateRank(6, false));
  }

  @Test
  void testRank2() {
    assertEquals(2, service.calculateRank(5, true));
  }

  @Test
  void testRank3() {
    assertEquals(3, service.calculateRank(5, false));
  }

  @Test
  void testRank4() {
    assertEquals(4, service.calculateRank(4, false));
  }

  @Test
  void testRank5() {
    assertEquals(5, service.calculateRank(3, true));
    assertEquals(5, service.calculateRank(3, false));
  }

  @Test
  void testRank0() {
    assertEquals(0, service.calculateRank(2, true));
    assertEquals(0, service.calculateRank(0, false));
  }
}
