package com.eeerrorcode.lottomate.repository;

// import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eeerrorcode.lottomate.domain.entity.lotto.LottoResults;

public interface LottoResultsRepository  extends JpaRepository<LottoResults, Long>{

    // Optional<LottoResults> DrawRound(Long drawRound);

    boolean existsByDrawRound(Long drawRound);
    
} 
