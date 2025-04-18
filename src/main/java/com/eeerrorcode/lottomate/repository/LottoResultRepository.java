package com.eeerrorcode.lottomate.repository;

import java.util.Optional;

// import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eeerrorcode.lottomate.domain.entity.lotto.LottoResults;

public interface LottoResultRepository  extends JpaRepository<LottoResults, Long>{

    boolean existsByDrawRound(Long drawRound); // 중복 여부 확인
    Optional<LottoResults> findTopByOrderByDrawRoundDesc(); // 최신 회차 정보
    Optional<LottoResults> findTopByOrderByDrawDateDesc(); // 최신 회차의 날짜 정보
    
} 
