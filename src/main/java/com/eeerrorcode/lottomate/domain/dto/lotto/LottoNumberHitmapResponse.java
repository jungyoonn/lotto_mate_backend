package com.eeerrorcode.lottomate.domain.dto.lotto;

import java.util.Map;
import java.util.SortedMap;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Schema(description = "히트맵 통계 응답. 회차별 번호 등장 횟수를 나타냅니다.")
public class LottoNumberHitmapResponse {

  @Schema(description = "회차별 번호 등장 횟수. (예: 1167회차의 1번 번호가 2회 등장하면 → {1167: {1:2, 2:0, ..., 45:1}})")
  private SortedMap<Long, Map<Integer, Integer>> hitmapMatrix;

}
