package com.eeerrorcode.lottomate.service.lotto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eeerrorcode.lottomate.domain.dto.lotto.LottoRecommendOption;
import com.eeerrorcode.lottomate.domain.dto.lotto.LottoRecommendResponse;
import com.eeerrorcode.lottomate.repository.lotto.LottoResultRepository;
import com.eeerrorcode.lottomate.repository.projection.lotto.NumberFrequency;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class LottoRecommendServiceImpl implements LottoRecommendService {

  @Autowired
  private final LottoResultRepository lottoResultRepository;

@Override
public LottoRecommendResponse recommendNumbers(LottoRecommendOption option) {
  Long latestRound = lottoResultRepository.findTopByOrderByDrawRoundDesc()
    .orElseThrow(() -> new RuntimeException("최신 회차 정보가 없습니다."))
    .getDrawRound();

  Long rangeStart = latestRound - option.getRange().getVALUE() + 1;

  // Projection 쿼리 분기
  List<NumberFrequency> frequencyList = option.isIncludeBonusNumber()
    ? lottoResultRepository.findNumberFrequenciesWithBonusInRange(rangeStart)
    : lottoResultRepository.findNumberFrequenciesInRange(rangeStart);

  Collections.shuffle(frequencyList); // 동일 빈도에 랜덤성 부여

  // 출현 빈도 기준 정렬 후 번호만 추출
  List<Long> sortedByFrequency = frequencyList.stream()
    .sorted((a, b) -> Long.compare(b.getFrequency(), a.getFrequency()))
    .map(NumberFrequency::getNum)
    .collect(java.util.stream.Collectors.toList());

  // 짝홀 조건 적용
  List<Integer> filtered = applyEvenOddFilter(sortedByFrequency, option.isAllowEvenOddMix());

  // 최종 6개 번호 선택 + 정렬
  List<Integer> finalNumbers = filtered.stream()
    .limit(6)
    .sorted()
    .toList();

  return LottoRecommendResponse.builder()
    .numbers(finalNumbers)
    .options(option)
    .build();
}

private List<Integer> applyEvenOddFilter(List<Long> numbers, boolean allowMix) {
  List<Integer> converted = numbers.stream().map(Long::intValue).toList();

  if (allowMix) return converted;

  List<Integer> evens = converted.stream().filter(n -> n % 2 == 0).toList();
  List<Integer> odds = converted.stream().filter(n -> n % 2 != 0).toList();

  Collections.shuffle(evens);
  Collections.shuffle(odds);

  int size = Math.min(3, Math.min(evens.size(), odds.size()));

  List<Integer> result = new ArrayList<>();
  result.addAll(evens.subList(0, size));
  result.addAll(odds.subList(0, size));
  return result;
}

}
