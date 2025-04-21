package com.eeerrorcode.lottomate.domain.dto.lotto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class LottoResultResponse {
  private Long drawRound; // 마지막 회차 정보입니다. 
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime drawDate; // 마지막 회차의 날짜 정보입니다. 
  private Long totalCount; // 마지막 회차까지의 Row 개수 정보입니다. 
}
