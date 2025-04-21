package com.eeerrorcode.lottomate.domain.dto.lotto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "추천된 번호 하나에 대한 출현 정보 및 추천 사유")
@Getter
@Builder
public class LottoNumberInsight {

    @Schema(description = "추천된 번호", example = "24")
    private Long number;

    @Schema(description = "최근 회차 기준 출현 횟수", example = "5")
    private Long count;

    @Schema(description = "출현 빈도 순위 (1이 가장 높은 빈도)", example = "4")
    private Long rank;

    @Schema(description = "이 번호가 추천된 사유", example = "최근 4주 중 3번 출현한 연속 인기 번호")
    private String reason;
}
