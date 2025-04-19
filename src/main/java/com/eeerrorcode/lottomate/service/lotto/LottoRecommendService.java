package com.eeerrorcode.lottomate.service.lotto;

import com.eeerrorcode.lottomate.domain.dto.lotto.LottoRecommendOption;
import com.eeerrorcode.lottomate.domain.dto.lotto.LottoRecommendResponse;

/**
 * 로또 번호 추천 관련 서비스를 정의하는 인터페이스입니다.
 * 번호 통계 기반의 추천 알고리즘을 통해 사용자에게 번호를 제공합니다.
 */
public interface LottoRecommendService {

  /**
   * 번호 추천 API의 핵심 메서드.
   * 주어진 옵션에 따라 회차 범위 내 통계 기반으로 6개의 번호를 반환합니다.
   *
   * @param option 추천 알고리즘에 필요한 회차 범위, 짝홀 필터링 여부, 보너스 번호 포함 여부를 포함하는 DTO
   * @return 추천된 번호와 옵션 정보가 담긴 응답 DTO
   */
  LottoRecommendResponse recommendNumbers(LottoRecommendOption option);
}