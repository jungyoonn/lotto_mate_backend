package com.eeerrorcode.lottomate.service.lotto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.eeerrorcode.lottomate.domain.dto.lotto.LottoUserHistoryRequest;
import com.eeerrorcode.lottomate.domain.dto.lotto.LottoUserHistoryResponse;
import com.eeerrorcode.lottomate.domain.dto.lotto.LottoUserWinningUpdateRequest;
import com.eeerrorcode.lottomate.domain.entity.lotto.LottoResults;
import com.eeerrorcode.lottomate.domain.entity.lotto.LottoUserHistory;
import com.eeerrorcode.lottomate.repository.lotto.LottoResultRepository;
import com.eeerrorcode.lottomate.repository.lotto.LottoUserHistoryRepository;
import com.eeerrorcode.lottomate.util.PageUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LottoHistoryServiceImpl implements LottoHistoryService {

  private final LottoUserHistoryRepository historyRepository;
  private final LottoResultRepository lottoResultRepository;

  @Override
  public List<LottoUserHistoryResponse> logUserHistory(Long userId, int page, int size) {
    Pageable pageable = PageUtil.of(page, size, "drawRound", true);
    return historyRepository.findByUserId(userId, pageable).map(LottoUserHistoryResponse::toDto).toList();
  }

  @Override
  public void saveUserLottoHistory(Long userId, LottoUserHistoryRequest request) {
    LottoUserHistory entity = request.toEntity(userId);
    historyRepository.save(entity);
  }

  @Override
  public void updateWinningResults(Long drawRound) {
    LottoResults result = lottoResultRepository.findByDrawRound(drawRound)
        .orElseThrow(() -> new IllegalArgumentException(drawRound + "회차 결과가 없습니다."));

    Set<Integer> winningNumbers = Set.of(
        result.getN1(), result.getN2(), result.getN3(),
        result.getN4(), result.getN5(), result.getN6());
    int bonus = result.getBonusNumber();

    List<LottoUserHistory> histories = historyRepository.findByDrawRoundAndIsClaimedFalse(drawRound);
    List<LottoUserHistory> updatedHistories = new ArrayList<>();

    for (LottoUserHistory history : histories) {
      Set<Integer> userNumbers = Arrays.stream(history.getNumbers().split(","))
          .map(String::trim)
          .map(Integer::parseInt)
          .collect(Collectors.toSet());

      long matchCount = userNumbers.stream().filter(winningNumbers::contains).count();
      boolean bonusMatch = userNumbers.contains(bonus);

      int rank = calculateRank(matchCount, bonusMatch);
      long amount = 0L;

      LottoUserWinningUpdateRequest updateDto = LottoUserWinningUpdateRequest.builder()
          .winningRank(rank)
          .winningAmount(amount)
          .isClaimed(true)
          .build();

      LottoUserHistory updated = updateDto.toUpdatedEntity(history);
      updatedHistories.add(updated);
    }

    historyRepository.saveAll(updatedHistories);
  }

  public int calculateRank(long matchCount, boolean bonusMatch) {
    if (matchCount == 6)
      return 1;
    if (matchCount == 5 && bonusMatch)
      return 2;
    if (matchCount == 5)
      return 3;
    if (matchCount == 4)
      return 4;
    if (matchCount == 3)
      return 5;
    return 0;
  }

}
