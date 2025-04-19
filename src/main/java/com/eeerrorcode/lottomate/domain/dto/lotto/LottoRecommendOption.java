package com.eeerrorcode.lottomate.domain.dto.lotto;

import com.eeerrorcode.lottomate.domain.enums.LottoRange;
import com.eeerrorcode.lottomate.domain.enums.LottoRecommendSystem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Set;

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

  @Schema(description = "추천에서 제외할 번호 목록 (회피 번호)", example = "[3, 12, 27]")
  @Builder.Default
  private Set<Long> excludedNumbers = Set.of();

  @Schema(description = "무조건 포함할 번호 목록 (고정 번호)", example = "[7, 14]")
  @Builder.Default
  private Set<Long> fixedNumbers = Set.of();

  @Schema(description = "추천 알고리즘 모드", example = "MIXED")
  private LottoRecommendSystem mode;

}
