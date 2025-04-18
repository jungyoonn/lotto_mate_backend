package com.eeerrorcode.lottomate.service.lotto;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import com.eeerrorcode.lottomate.domain.dto.lotto.LottoResultResponse;
import com.eeerrorcode.lottomate.domain.entity.lotto.LottoResults;
import com.eeerrorcode.lottomate.repository.lotto.LottoResultRepository;
import com.eeerrorcode.lottomate.repository.projection.lotto.NumberFrequency;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LottoResultServiceImpl implements LottoResultService {

  private final LottoResultRepository lottoResultRepository;

  @Override
  public LottoResultResponse getLottoStatus() {
    long totalCount = lottoResultRepository.count();

    Optional<LottoResults> latest = lottoResultRepository.findTopByOrderByDrawRoundDesc();

    return LottoResultResponse.builder()
      .totalCount(totalCount)
      .drawRound(latest.map(LottoResults::getDrawRound)
        .orElseThrow(() -> new RuntimeException("최신 회차 정보가 없습니다.")))
      .drawDate(latest.map(LottoResults::getDrawDate).orElse(null))
      .build();
  }

  @Override
  public Map<Long, Long> getNumberDistribution(long range) {

    Long latestRound = lottoResultRepository.findTopByOrderByDrawRoundDesc()
      .orElseThrow(() -> new RuntimeException("최신 회차 정보가 없습니다."))
      .getDrawRound();

    Long rangeStart = latestRound - range + 1;

    List<NumberFrequency> numberFrequencyList = lottoResultRepository.findNumberFrequenciesInRange(rangeStart);

    Map<Long, Long> distribution = new TreeMap<>();
    for (long i = 1; i <= 45; i++) {
      distribution.put(i, 0L);
    }

    for (NumberFrequency row : numberFrequencyList) {
      distribution.put(row.getNum(), row.getFrequency());
    }

    return distribution;
  }

}
