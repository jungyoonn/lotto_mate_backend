package com.eeerrorcode.lottomate.service.lotto;

import com.eeerrorcode.lottomate.domain.dto.lotto.LottoResultResponse;

public interface LottoResultService {

    LottoResultResponse getLottoStatus(); // DB 최신 로또 정보 불러오기
    
}
