package com.eeerrorcode.lottomate.domain.dto.lotto;

import com.eeerrorcode.lottomate.domain.entity.lotto.LottoUserHistory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 사용자 응모 결과 업데이트를 위한 요청 DTO입니다.
 * - 기존 응모 내역(LottoUserHistory)에 덮어쓸 결과 정보만 포함됩니다.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LottoUserWinningUpdateRequest {

  private Integer winningRank;     // 당첨 등수 (0~5, 0은 미당첨)
  private Long winningAmount;      // 당첨 금액 (없을 경우 0L)
  private Boolean isClaimed;       // 결과 반영 여부

  /**
   * 기존 응모 내역을 기반으로 응모 결과를 반영한 새 엔티티로 변환합니다.
   * @param origin 기존 응모 엔티티
   * @return 갱신된 응모 엔티티
   */
  public LottoUserHistory toUpdatedEntity(LottoUserHistory origin) {
    return LottoUserHistory.builder()
        .id(origin.getId())
        .userId(origin.getUserId())
        .drawRound(origin.getDrawRound())
        .numbers(origin.getNumbers())
        .isAuto(origin.isAuto())
        .isSubscribed(origin.isSubscribed())
        .winningRank(this.winningRank)
        .winningAmount(this.winningAmount)
        .isClaimed(this.isClaimed)
        .build();
  }
}
