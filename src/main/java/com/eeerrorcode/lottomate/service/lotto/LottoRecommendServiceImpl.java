package com.eeerrorcode.lottomate.service.lotto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eeerrorcode.lottomate.domain.dto.lotto.LottoRecommendOption;
import com.eeerrorcode.lottomate.domain.dto.lotto.LottoRecommendResponse;
import com.eeerrorcode.lottomate.repository.lotto.LottoResultRepository;
import com.eeerrorcode.lottomate.repository.projection.lotto.NumberFrequency;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@AllArgsConstructor
@Log4j2
public class LottoRecommendServiceImpl implements LottoRecommendService {

  @Autowired
  private final LottoResultRepository lottoResultRepository;

  @Override
  public LottoRecommendResponse recommendNumbers(LottoRecommendOption option) {
    try {
      Long latestRound = lottoResultRepository.findTopByOrderByDrawRoundDesc()
          .orElseThrow(() -> new RuntimeException("최신 회차 정보가 없습니다."))
          .getDrawRound();

      Long rangeStart = latestRound - option.getRange().getVALUE() + 1;

      List<NumberFrequency> frequencyList = option.isIncludeBonusNumber()
          ? lottoResultRepository.findNumberFrequenciesWithBonusInRange(rangeStart)
          : lottoResultRepository.findNumberFrequenciesInRange(rangeStart);

      switch (option.getMode()) {
        case HIGH_FREQUENCY -> frequencyList.sort((a, b) -> Long.compare(b.getFrequency(), a.getFrequency()));
        case LOW_FREQUENCY -> frequencyList.sort((a, b) -> Long.compare(a.getFrequency(), b.getFrequency()));
        case MIXED -> Collections.shuffle(frequencyList);
        default -> throw new IllegalArgumentException("잘못된 추천 모드입니다.");
      }

      List<Long> sortedByFrequency = frequencyList.stream()
          .map(NumberFrequency::getNum)
          .toList();

      Set<Long> excluded = option.getExcludedNumbers() != null
          ? option.getExcludedNumbers()
          : Collections.emptySet();

      List<Long> candidates = sortedByFrequency.stream()
          .filter(n -> !excluded.contains(n))
          .toList();

      List<Long> finalNumbers = new ArrayList<>();
      if (option.getFixedNumbers() != null && !option.getFixedNumbers().isEmpty()) {
        finalNumbers.addAll(option.getFixedNumbers());
      }

      int remaining = 6 - finalNumbers.size();
      candidates = candidates.stream()
          .filter(n -> !finalNumbers.contains(n))
          .toList();

      List<Long> mixed = applyEvenOddFilter(candidates, option.isAllowEvenOddMix());

      finalNumbers.addAll(
          mixed.stream()
              .limit(Math.max(0, remaining))
              .toList());

      List<Long> result = finalNumbers.stream()
          .limit(6)
          .sorted()
          .toList();

      return LottoRecommendResponse.builder()
          .numbers(result)
          .options(option)
          .build();

    } catch (Exception e) {
      log.error("추천 번호 생성 중 예외 발생!", e); 
      throw e;
    }
  }

  private List<Long> applyEvenOddFilter(List<Long> numbers, boolean allowMix) {

    if (allowMix) {
      return numbers;
    }

    List<Long> evens = new ArrayList<>(numbers.stream().filter(n -> n % 2 == 0).toList());
    List<Long> odds = new ArrayList<>(numbers.stream().filter(n -> n % 2 != 0).toList());

    log.info("짝수 후보 개수: {}", evens.size());
    log.info("홀수 후보 개수: {}", odds.size());
    log.info("총 후보 수: {}", numbers.size());

    Collections.shuffle(evens);
    Collections.shuffle(odds);

    List<Long> result = new ArrayList<>();

    if (evens.size() >= 3 && odds.size() >= 3) {
      result.addAll(evens.subList(0, 3));
      result.addAll(odds.subList(0, 3));
    } else {
      int evenCount = Math.min(3, evens.size());
      int oddCount = Math.min(3, odds.size());

      result.addAll(evens.subList(0, evenCount));
      result.addAll(odds.subList(0, oddCount));

      List<Long> leftovers = new ArrayList<>(numbers);
      leftovers.removeAll(result);
      Collections.shuffle(leftovers);
      while (result.size() < 6 && !leftovers.isEmpty()) {
        result.add(leftovers.remove(0));
      }
    }
    return result.stream().limit(6).toList();
  }

}
