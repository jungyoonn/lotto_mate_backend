package com.eeerrorcode.lottomate.service.lotto;

import java.util.Map;

import com.eeerrorcode.lottomate.domain.dto.lotto.LottoLatestResponse;
import com.eeerrorcode.lottomate.domain.dto.lotto.LottoNumberHitmapResponse;
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

  // /**
  // * 회차별 번호 등장 여부를 히트맵 형태의 행렬로 반환합니다.
  // * 각 회차(drawRound)를 Key로 하고, 1~45번까지의 번호가 해당 회차에 등장했는지 여부(Boolean)를 값으로 가지는
  // Map을
  // * 구성합니다.
  // * 예: {1167: {1:false, 2:true, ..., 45:false}, 1166: {...}, ...}
  // *
  // * @param range 분석할 회차 수 (최신 회차부터 몇 회차를 조회할 것인지)
  // * @return 히트맵 형태의 데이터 구조 (SortedMap<Long, Map<Integer, Boolean>>)
  // */
  // LottoNumberHitmapResponse getHitMapMatrix(long range);

  /**
   * 지정된 회차 범위(startRound ~ endRound) 내에서 각 회차별 로또 번호(1~45번)의 등장 횟수를 히트맵 형태로
   * 반환합니다.
   * <p>
   * 반환되는 데이터는 각 회차를 key로 하고, 해당 회차에서 1~45번 번호의 등장 횟수를 value로 가지는 Map 구조입니다.
   * 이 정보는 Chart.js 등에서 시각적 히트맵 그래프를 구성할 때 사용됩니다.
   * </p>
   * 
   * <pre>
   * 예시:
   * {
   *   1168: {1=0, 2=1, ..., 33=2},
   *   1167: {1=1, 2=0, ..., 45=1},
   *   ...
   * }
   * </pre>
   *
   * @param startRound 조회할 시작 회차 (포함)
   * @param endRound   조회할 종료 회차 (포함)
   * @return 히트맵 매트릭스 구조를 포함한 응답 DTO
   * @throws IllegalArgumentException startRound가 endRound보다 클 경우 발생
   */
  LottoNumberHitmapResponse getHitMapMatrixByRange(long startRound, long endRound);

  Map<Integer, Integer> getNumberDistributionByRange(long startRound, long endRound);

  Map<Integer, Map<Long, Integer>> getHistoricalHitmap(long startRound, long endRound);

  LottoLatestResponse getLatestDraw();

  // SortedMap<Long, Map<Integer, Integer>> getHitmapMatrix();

}
