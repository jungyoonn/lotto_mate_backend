package com.eeerrorcode.lottomate.service.lotto;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.eeerrorcode.lottomate.domain.dto.lotto.LottoUserHistoryResponse;
import com.eeerrorcode.lottomate.repository.lotto.LottoUserHistoryRepository;
import com.eeerrorcode.lottomate.util.PageUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LottoHistroyServiceImpl implements LottoHistoryService{
  
  private final LottoUserHistoryRepository historyRepository;
  
  @Override
  public List<LottoUserHistoryResponse> logUserHistory(Long userId, int page, int size) {
    Pageable pageable = PageUtil.of(page, size, "drawRound", true);
    return historyRepository.findByUserId(userId, pageable).map(LottoUserHistoryResponse::toDto).toList();
  }
  
}
