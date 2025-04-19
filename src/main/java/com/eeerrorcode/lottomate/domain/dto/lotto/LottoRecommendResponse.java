package com.eeerrorcode.lottomate.domain.dto.lotto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "로또 번호 추천 결과 DTO")
@Getter
@Builder
@AllArgsConstructor
public class LottoRecommendResponse {

  @Schema(description = "추천된 번호 목록 (6개)", example = "[3, 12, 18, 24, 36, 42]")
  private List<Integer> numbers;

  @Schema(description = "적용된 추천 옵션")
  private LottoRecommendOption options;
}
