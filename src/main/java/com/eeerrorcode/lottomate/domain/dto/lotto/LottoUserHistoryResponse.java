package com.eeerrorcode.lottomate.domain.dto.lotto;

import java.time.LocalDateTime;

import com.eeerrorcode.lottomate.domain.entity.lotto.LottoUserHistory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LottoUserHistoryResponse {

  private Long drawRound;
  private String numbers;
  private Boolean isAuto;
  private Boolean isSubscribed;
  private Integer winningRank;
  private Long winningAmount;
  private Boolean isClaimed;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static LottoUserHistoryResponse toDto(LottoUserHistory entity) {
    return LottoUserHistoryResponse.builder()
        .drawRound(entity.getDrawRound())
        .numbers(entity.getNumbers())
        .isAuto(entity.isAuto())
        .isSubscribed(entity.isSubscribed())
        .winningRank(entity.getWinningRank())
        .winningAmount(entity.getWinningAmount())
        .isClaimed(entity.isClaimed())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
  }

  public LottoUserHistory toEntity(Long userId) {
    return LottoUserHistory.builder()
        .userId(userId)
        .drawRound(this.drawRound)
        .numbers(this.numbers)
        .isAuto(this.isAuto)
        .isSubscribed(this.isSubscribed)
        .winningRank(this.winningRank)
        .winningAmount(this.winningAmount)
        .isClaimed(this.isClaimed)
        .build();
  }
}
