package com.eeerrorcode.lottomate.service.lotto;

import java.util.Map;

import com.eeerrorcode.lottomate.domain.dto.lotto.LottoResultResponse;

public interface LottoResultService {

  LottoResultResponse getLottoStatus(); // DB 최신 로또 정보 불러오기
  Map<Long, Long> getNumberDistribution(long range); // DB 기반 번호 ditribution 정보 종합하기
}
