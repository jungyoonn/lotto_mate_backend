package com.eeerrorcode.lottomate.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LottoRange {
  RECENT_77(77L), RECENT_100(100L), RECENT_200(200L), RECENT_777(777L);
  private final Long VALUE;
}
