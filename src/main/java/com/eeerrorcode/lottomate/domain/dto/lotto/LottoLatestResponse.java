package com.eeerrorcode.lottomate.domain.dto.lotto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LottoLatestResponse {
  private Long drawRound;
  private List<Integer> numbers;
  private Integer bonusNumber;
  private String drawDate;
}
