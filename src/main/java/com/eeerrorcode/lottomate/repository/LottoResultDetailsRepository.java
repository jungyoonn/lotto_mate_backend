package com.eeerrorcode.lottomate.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eeerrorcode.lottomate.domain.entity.lotto.LottoResultDetails;

public interface LottoResultDetailsRepository extends JpaRepository<LottoResultDetails, Long>{
    
}
