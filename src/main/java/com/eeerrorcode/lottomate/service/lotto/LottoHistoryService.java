package com.eeerrorcode.lottomate.service.lotto;

import java.util.List;

import com.eeerrorcode.lottomate.domain.dto.lotto.LottoUserHistoryResponse;

public interface LottoHistoryService {
  List<LottoUserHistoryResponse> logUserHistory(Long userId, int page, int size);
}
