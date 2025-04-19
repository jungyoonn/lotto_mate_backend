package com.eeerrorcode.lottomate.domain.dto.lotto;

import com.eeerrorcode.lottomate.domain.enums.LottoRange;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "로또 번호 추천 요청 옵션 DTO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LottoRecommendOption {
  @Schema(description = "추천에 사용할 회차 범위", example = "RECENT_100")
  private LottoRange range;

  @Schema(description = "짝/홀 필터링 비율 허용 여부", example = "true")
  @Builder.Default
  private boolean allowEvenOddMix = true;

  @Schema(description = "보너스 번호 포함 여부", example = "false")
  @Builder.Default
  private boolean includeBonusNumber = false;
}
