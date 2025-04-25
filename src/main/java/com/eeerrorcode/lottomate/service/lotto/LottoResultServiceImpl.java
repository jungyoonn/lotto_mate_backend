package com.eeerrorcode.lottomate.service.lotto;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import com.eeerrorcode.lottomate.domain.dto.lotto.LottoLatestResponse;
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
  public LottoNumberHitmapResponse getHitMapMatrixByRange(long start, long end) {
    if (start > end)
      throw new IllegalArgumentException("startRound는 endRound보다 작아야 합니다.");

    List<LottoResults> resultsInRange = lottoResultRepository.findByDrawRoundBetween(start, end);

    SortedMap<Long, Map<Integer, Integer>> matrix = new TreeMap<>();

    for (LottoResults result : resultsInRange) {
      Map<Integer, Integer> numberMap = new TreeMap<>();
      for (int i = 1; i <= 45; i++)
        numberMap.put(i, 0);

      List<Integer> appeared = List.of(
          result.getN1(), result.getN2(), result.getN3(),
          result.getN4(), result.getN5(), result.getN6(),
          result.getBonusNumber());

      for (Integer num : appeared) {
        numberMap.put(num, numberMap.get(num) + 1);
      }

      matrix.put(result.getDrawRound(), numberMap);
    }

    return LottoNumberHitmapResponse.builder().hitmapMatrix(matrix).build();
  }

  @Override
  public Map<Integer, Integer> getNumberDistributionByRange(long start, long end) {
    if (start > end)
      throw new IllegalArgumentException("시작 회차는 종료 회차보다 작아야 합니다.");

    List<LottoResults> results = lottoResultRepository.findByDrawRoundBetween(start, end);

    Map<Integer, Integer> freqMap = new TreeMap<>();
    for (int i = 1; i <= 45; i++)
      freqMap.put(i, 0);

    for (LottoResults result : results) {
      List<Integer> numbers = List.of(
          result.getN1(), result.getN2(), result.getN3(),
          result.getN4(), result.getN5(), result.getN6(),
          result.getBonusNumber());

      for (Integer num : numbers) {
        freqMap.put(num, freqMap.get(num) + 1);
      }
    }

    return freqMap;
  }

  @Override
  public Map<Integer, Map<Long, Integer>> getHistoricalHitmap(long startRound, long endRound) {
    if (startRound > endRound) {
      throw new IllegalArgumentException("startRound는 endRound보다 작거나 같아야 합니다.");
    }

    List<LottoResults> results = lottoResultRepository.findByDrawRoundBetween(startRound, endRound);

    // 1~45번까지 모든 번호 초기화
    Map<Integer, Map<Long, Integer>> resultMap = new TreeMap<>();

    for (int num = 1; num <= 45; num++) {
      resultMap.put(num, new TreeMap<>()); // 번호 중심
    }

    for (LottoResults result : results) {
      Long round = result.getDrawRound();

      List<Integer> appeared = List.of(
          result.getN1(), result.getN2(), result.getN3(),
          result.getN4(), result.getN5(), result.getN6(),
          result.getBonusNumber());

      for (int num = 1; num <= 45; num++) {
        int value = appeared.contains(num) ? 1 : 0;
        resultMap.get(num).put(round, value);
      }
    }

    return resultMap;
  }

  @Override
  public LottoLatestResponse getLatestDraw() {
    LottoResults latest = lottoResultRepository.findTopByOrderByDrawRoundDesc()
        .orElseThrow(() -> new IllegalStateException("로또 회차 데이터가 없습니다."));

    return LottoLatestResponse.builder()
        .drawRound(latest.getDrawRound())
        .numbers(Arrays.asList(latest.getN1(), latest.getN2(), latest.getN3(), latest.getN4(), latest.getN5(), latest.getN6()))
        .bonusNumber(latest.getBonusNumber())
        .drawDate(latest.getDrawDate().toString())
        .build();
  }

}
