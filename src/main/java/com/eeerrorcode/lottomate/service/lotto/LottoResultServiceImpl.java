package com.eeerrorcode.lottomate.service.lotto;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import com.eeerrorcode.lottomate.domain.dto.lotto.LottoNumberHitmapResponse;
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

  @Override
  public LottoNumberHitmapResponse getHitMapMatrix(long range) {
    Long latestDrawRound = lottoResultRepository.findTopByOrderByDrawRoundDesc()
        .map(LottoResults::getDrawRound)
        .orElseThrow(() -> new IllegalStateException("로또 데이터가 존재하지 않습니다."));

    Long startRound = Math.max(1, latestDrawRound - range + 1); 

    List<LottoResults> resultsInRange = lottoResultRepository.findByDrawRoundBetween(startRound, latestDrawRound);

    SortedMap<Long, Map<Integer, Boolean>> matrix = new TreeMap<>();

    for (LottoResults result : resultsInRange) {
      Map<Integer, Boolean> numberMap = new TreeMap<>();

      for (int i = 1; i <= 45; i++) {
        boolean exists = List.of(
            result.getN1(), result.getN2(), result.getN3(),
            result.getN4(), result.getN5(), result.getN6(),
            result.getBonusNumber()).contains(i);
        numberMap.put(i, exists);
      }

      matrix.put(result.getDrawRound(), numberMap);
    }

    return LottoNumberHitmapResponse.builder()
        .hitmapMatrix(matrix)
        .build();
  }

}
