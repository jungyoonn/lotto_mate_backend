package com.eeerrorcode.lottomate.service.lotto;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eeerrorcode.lottomate.domain.enums.LottoRange;
import com.eeerrorcode.lottomate.repository.lotto.LottoResultRepository;
import com.eeerrorcode.lottomate.repository.projection.lotto.NumberFrequency;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class LottoRecommendServiceImpl implements LottoRecommendService {

  @Autowired
  private final LottoResultRepository lottoResultRepository;

  @Override
  public List<Long> recommendNumbers(LottoRange range) {
    Long latestRound = lottoResultRepository.findTopByOrderByDrawRoundDesc()
        .orElseThrow(() -> new RuntimeException("최신 회차 정보가 없습니다."))
        .getDrawRound();

    Long rangeStart = latestRound - range.getVALUE() + 1;

    List<NumberFrequency> frequencyList = lottoResultRepository.findNumberFrequenciesInRange(rangeStart);

    return frequencyList.stream()
        .sorted((a, b) -> Long.compare(b.getFrequency(), a.getFrequency()))
        .limit(6)
        .map(NumberFrequency::getNum)
        .sorted()
        .toList();
  }

}
