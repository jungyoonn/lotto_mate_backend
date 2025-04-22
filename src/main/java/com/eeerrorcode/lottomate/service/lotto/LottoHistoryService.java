package com.eeerrorcode.lottomate.service.lotto;

import java.util.List;

import com.eeerrorcode.lottomate.domain.dto.lotto.LottoUserHistoryRequest;
import com.eeerrorcode.lottomate.domain.dto.lotto.LottoUserHistoryResponse;

/**
 * 사용자 로또 응모 기록 관리 및 조회 기능을 제공하는 서비스 인터페이스입니다.
 * 사용자의 개별 응모 정보를 저장하거나, 페이징 기반으로 기록을 조회할 수 있습니다.
 */
public interface LottoHistoryService {

  /**
   * 특정 사용자의 로또 응모 기록을 회차 기준 내림차순으로 조회합니다.
   *
   * @param userId 조회 대상 사용자 ID
   * @param page   조회할 페이지 번호 (0부터 시작)
   * @param size   한 페이지당 항목 수
   * @return 응모 기록 리스트 (LottoUserHistoryResponse DTO 형태)
   */
  List<LottoUserHistoryResponse> logUserHistory(Long userId, int page, int size);

  /**
   * 사용자의 로또 응모 정보를 저장합니다.
   *
   * @param userId  저장할 사용자 ID
   * @param request 사용자의 응모 기록 정보 요청 DTO
   */
  void saveUserLottoHistory(Long userId, LottoUserHistoryRequest request);

  /**
   * 미처리 응모 기록을 회차별 당첨 결과 기준으로 갱신합니다.
   */
  void updateWinningResults(Long drawRound);
}
