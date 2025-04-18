package com.eeerrorcode.lottomate.service.lotto;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.eeerrorcode.lottomate.domain.dto.lotto.LottoResultResponse;
import com.eeerrorcode.lottomate.domain.entity.lotto.LottoResults;
import com.eeerrorcode.lottomate.repository.LottoResultRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LottoResultServiceImpl implements LottoResultService{

    private final LottoResultRepository lottoResultRepository;

    @Override
    public LottoResultResponse getLottoStatus() {
        long totalCount = lottoResultRepository.count();

        Optional<LottoResults> latest = lottoResultRepository.findTopByOrderByDrawRoundDesc();

        return LottoResultResponse.builder()
                .totalCount(totalCount)
                .drawRound(latest.map(LottoResults::getDrawRound).orElseThrow(null))
                .drawDate(latest.map(LottoResults::getDrawDate).orElse(null))
            .build();
    }
}
