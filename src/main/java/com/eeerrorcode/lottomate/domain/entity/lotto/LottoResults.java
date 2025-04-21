package com.eeerrorcode.lottomate.domain.entity.lotto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lotto_results")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LottoResults {
    /**
     * 기본 식별자
     * ID 칼럼(회차 정보와 별개, 추후 확장성 고려하였습니다)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID 칼럼(회차 정보와 별개, 추후 확장성 고려하였습니다)
    /**
     * 로또 추첨 회차 번호
     * 회차 정보(동행복권 크롤링 결과입니다)
     */
    @Column(name = "draw_round", unique = true, nullable = false)
    private Long drawRound; // 회차 정보(동행복권 크롤링 결과입니다)
    /**
     * 로또 추첨 일시
     * 날짜 정보(동행복권 크롤링 결과입니다)
     */
    @Column(name = "draw_date", nullable = false)
    private LocalDateTime drawDate; // 날짜 정보(동행복권 크롤링 결과입니다)

    @Column(name = "n1", nullable = false)
    private Integer n1; // 번호 정보(1번)

    @Column(name = "n2", nullable = false)
    private Integer n2; // 번호 정보(2번)

    @Column(name = "n3", nullable = false)
    private Integer n3; // 번호 정보(3번)

    @Column(name = "n4", nullable = false)
    private Integer n4; // 번호 정보(4번)

    @Column(name = "n5", nullable = false)
    private Integer n5; // 번호 정보(5번)

    @Column(name = "n6", nullable = false)
    private Integer n6; // 번호 정보(6번)
    /**
     * 보너스 번호
     * 번호 정보(보너스)
     */
    @Column(name = "bonus_number", nullable = false)
    private Integer bonusNumber; // 번호 정보(보너스)

    /**
     * 1등 당첨자 수
     * 1등 당첨자 수(동행복권 크롤링 결과입니다)
     */
    @Column(name = "first_prize_winners", nullable = false)
    private Long firstPrizeWinners; // 1등 당첨자 수(동행복권 크롤링 결과입니다)

    /**
     * 1등 당첨금액
     * 1등 당첨 금액(동행복권 크롤링 결과입니다)
     */
    @Column(name = "first_prize_amount", nullable = false)
    private Long firstPrizeAmount; // 1등 당첨 금액(동행복권 크롤링 결과입니다)

    /**
     * 해당 회차 총 판매금액
     * 복권 총 판매 수(동행복권 크롤링 결과입니다)
     */
    @Column(name = "total_sales_amount", nullable = false)
    private Long totalSalesAmount; // 복권 총 판매 수(동행복권 크롤링 결과입니다)

    /**
     * 당첨 상세 정보 목록
     * 각 등수별 당첨 상세 정보를 포함합니다
     */
    @Builder.Default
    @OneToMany(mappedBy = "lottoResults", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LottoResultDetails> prizeDetails = new ArrayList<>();
}
