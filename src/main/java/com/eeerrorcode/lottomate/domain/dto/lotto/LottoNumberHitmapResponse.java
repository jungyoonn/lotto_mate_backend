package com.eeerrorcode.lottomate.domain.dto.lotto;

import java.util.Map;
import java.util.SortedMap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class LottoNumberHitmapResponse {
  private SortedMap<Long, Map<Integer, Boolean>> hitmapMatrix;  
}
