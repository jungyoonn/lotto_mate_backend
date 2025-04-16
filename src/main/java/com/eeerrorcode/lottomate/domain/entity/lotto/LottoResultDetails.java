package com.eeerrorcode.lottomate.domain.entity.lotto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "lotto_result_details")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LottoResultDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lotto_result_id", nullable = false)
    private LottoResults lottoResults;

    @Column(name = "rank", nullable = false)
    private Integer rank;

    @Column(name = "winner_count", nullable = false)
    private Long winnerCount;

    @Column(name = "prize_amount", nullable = false)
    private Long prizeAmount;

}
