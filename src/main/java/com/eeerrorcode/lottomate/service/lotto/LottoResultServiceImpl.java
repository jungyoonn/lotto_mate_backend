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

  // @Override
  // public LottoNumberHitmapResponse getHitMapMatrix(long range) {
  // Long latestDrawRound = lottoResultRepository.findTopByOrderByDrawRoundDesc()
  // .map(LottoResults::getDrawRound)
  // .orElseThrow(() -> new IllegalStateException("로또 데이터가 존재하지 않습니다."));

  // Long startRound = Math.max(1, latestDrawRound - range + 1);

  // List<LottoResults> resultsInRange =
  // lottoResultRepository.findByDrawRoundBetween(startRound, latestDrawRound);

  // SortedMap<Long, Map<Integer, Integer>> matrix = new TreeMap<>();

  // for (LottoResults result : resultsInRange) {
  // Map<Integer, Integer> numberMap = new TreeMap<>();

  // // 초기화: 1~45번 번호 모두 0으로 설정
  // for (int i = 1; i <= 45; i++) {
  // numberMap.put(i, 0);
  // }

  // // 등장 번호들 (보너스 포함)
  // List<Integer> appeared = List.of(
  // result.getN1(), result.getN2(), result.getN3(),
  // result.getN4(), result.getN5(), result.getN6(),
  // result.getBonusNumber());

  // // 등장한 번호들의 등장 횟수 +1
  // for (Integer num : appeared) {
  // numberMap.put(num, numberMap.get(num) + 1);
  // }

  // matrix.put(result.getDrawRound(), numberMap);
  // }

  // return LottoNumberHitmapResponse.builder()
  // .hitmapMatrix(matrix)
  // .build();
  // }

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

}
