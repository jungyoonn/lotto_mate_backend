package com.eeerrorcode.lottomate.domain.dto.lotto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LottoRecommendResponse {

  @Schema(description = "추천된 번호 목록", example = "[5, 12, 23, 31, 44, 45]")
  private List<Long> numbers;

  @Schema(description = "요청에 사용된 옵션 정보")
  private LottoRecommendOption options;

  @Schema(description = "추천된 번호 분석", example = "최근 몇 주간 등장 횟수가 많습니다")
  private List<LottoNumberInsight> insights;
}
