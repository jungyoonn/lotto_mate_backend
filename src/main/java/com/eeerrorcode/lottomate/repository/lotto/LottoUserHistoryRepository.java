package com.eeerrorcode.lottomate.repository.lotto;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.eeerrorcode.lottomate.domain.entity.lotto.LottoUserHistory;

public interface LottoUserHistoryRepository extends JpaRepository<LottoUserHistory, Long>{
  List<LottoUserHistory> findByUserId(Long userId);
  Page<LottoUserHistory> findByUserId(Long userId, Pageable pageable);
  boolean existsByUserIdAndDrawRound(Long userId, Long drawRound);
}
