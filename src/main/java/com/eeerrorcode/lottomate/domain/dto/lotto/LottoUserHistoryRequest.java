package com.eeerrorcode.lottomate.domain.dto.lotto;

import com.eeerrorcode.lottomate.domain.entity.lotto.LottoUserHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class LottoUserHistoryRequest {

  private Long drawRound;
  private String numbers;
  private Boolean isAuto;
  private Boolean isSubscribed;

  public LottoUserHistory toEntity(Long userId) {
    return LottoUserHistory.builder()
        .userId(userId)
        .drawRound(this.drawRound)
        .numbers(this.numbers)
        .isAuto(this.isAuto)
        .isSubscribed(this.isSubscribed)
        .isClaimed(false)
        .build();
  }
}
