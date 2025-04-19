package com.eeerrorcode.lottomate.service.lotto;

import java.util.List;

import com.eeerrorcode.lottomate.domain.enums.LottoRange;

public interface LottoRecommendService {
  List<Long> recommendNumbers(LottoRange range);
} 