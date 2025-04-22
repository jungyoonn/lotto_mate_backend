package com.eeerrorcode.lottomate.repository.lotto;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eeerrorcode.lottomate.domain.entity.lotto.LottoResults;
import com.eeerrorcode.lottomate.repository.projection.lotto.NumberFrequency;

public interface LottoResultRepository extends JpaRepository<LottoResults, Long> {

  boolean existsByDrawRound(Long drawRound); // 중복 여부 확인

  Optional<LottoResults> findTopByOrderByDrawRoundDesc(); // 최신 회차 정보

  Optional<LottoResults> findTopByOrderByDrawDateDesc(); // 최신 회차의 날짜 정보

  @Query(value = """
      SELECT num, COUNT(*) AS frequency
      FROM (
          SELECT n1 AS num FROM lotto_results WHERE draw_round >= :rangeStart
          UNION ALL
          SELECT n2 FROM lotto_results WHERE draw_round >= :rangeStart
          UNION ALL
          SELECT n3 FROM lotto_results WHERE draw_round >= :rangeStart
          UNION ALL
          SELECT n4 FROM lotto_results WHERE draw_round >= :rangeStart
          UNION ALL
          SELECT n5 FROM lotto_results WHERE draw_round >= :rangeStart
          UNION ALL
          SELECT n6 FROM lotto_results WHERE draw_round >= :rangeStart
      ) AS numbers
      GROUP BY num
      ORDER BY num
      """, nativeQuery = true)
  List<NumberFrequency> findNumberFrequenciesInRange(@Param("rangeStart") Long rangeStart); // rangeStart 만큼의 회차 조회(번호
                                                                                            // 통계용)

  @Query(value = """
      SELECT num, COUNT(*) AS frequency
      FROM (
          SELECT n1 AS num FROM lotto_results WHERE draw_round >= :rangeStart
          UNION ALL
          SELECT n2 FROM lotto_results WHERE draw_round >= :rangeStart
          UNION ALL
          SELECT n3 FROM lotto_results WHERE draw_round >= :rangeStart
          UNION ALL
          SELECT n4 FROM lotto_results WHERE draw_round >= :rangeStart
          UNION ALL
          SELECT n5 FROM lotto_results WHERE draw_round >= :rangeStart
          UNION ALL
          SELECT n6 FROM lotto_results WHERE draw_round >= :rangeStart
          UNION ALL
          SELECT bonus_number FROM lotto_results WHERE draw_round >= :rangeStart
      ) AS numbers
      GROUP BY num
      ORDER BY num
      """, nativeQuery = true)
  List<NumberFrequency> findNumberFrequenciesWithBonusInRange(@Param("rangeStart") Long rangeStart);


  List<LottoResults> findByDrawRoundBetween(Long start, Long end);
}
