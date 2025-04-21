package com.eeerrorcode.lottomate.service.lotto;

import java.util.Map;

import com.eeerrorcode.lottomate.domain.dto.lotto.LottoResultResponse;

/**
 * 로또 결과 조회 및 통계 분석을 위한 서비스 인터페이스입니다.
 * DB에 저장된 회차 정보로부터 최신 회차 상태 확인 및 번호 출현 빈도 통계를 제공합니다.
 */
public interface LottoResultService {
  /**
   * DB로부터 전체 로또 결과 row 수와 가장 최신 회차의 정보를 조회합니다.
   * 최신 회차의 회차 번호(drawRound), 추첨일(drawDate), 저장된 row 총 개수를 DTO로 반환합니다.
   *
   * @return 최신 로또 회차 정보를 담은 LottoResultResponse DTO
   */
  LottoResultResponse getLottoStatus();

  /**
   * 로또 결과 테이블에서 최신 회차부터 지정된 회차 수만큼 범위를 계산한 뒤,
   * 1~45번 번호 각각이 등장한 횟수를 계산하여 Map으로 반환합니다.
   * 모든 번호를 0부터 초기화한 뒤 실제 등장한 번호는 해당 횟수로 업데이트합니다.
   *
   * @param range 분석할 회차 범위 (예: 100이면 최신 100회 기준 분석)
   * @return 번호별 등장 횟수를 담은 TreeMap (1~45번 → 등장 횟수)
   */
  Map<Long, Long> getNumberDistribution(long range); // DB 기반 번호 ditribution 정보 종합하기
}
