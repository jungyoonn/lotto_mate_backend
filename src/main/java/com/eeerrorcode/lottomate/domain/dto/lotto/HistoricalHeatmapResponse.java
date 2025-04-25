package com.eeerrorcode.lottomate.domain.dto.lotto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoricalHeatmapResponse {
  private Map<Integer, Map<Long, Integer>> hitmap;
}
