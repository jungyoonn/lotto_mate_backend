package com.eeerrorcode.lottomate.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eeerrorcode.lottomate.domain.dto.CommonResponse;
import com.eeerrorcode.lottomate.domain.dto.lotto.LottoResultResponse;
import com.eeerrorcode.lottomate.service.lotto.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lotto")
@Tag(name = "Lotto API", description = "로또 다양한 기능을 포함한 주요 API 그룹")
public class LottoController {

  private final LottoCrawlerService crawlerService;
  private final LottoResultService resultService;

  // 전체 크롤링 메서드(DB 업데이트용)
  @Operation(summary = "전체 회차 크롤링 실행", description = "1회차부터 현재까지의 모든 로또 당첨 정보를 동행복권 사이트에서 크롤링하여 DB에 저장합니다.")
  @ApiResponse(responseCode = "200", description = "전체 회차 크롤링이 성공적으로 수행되었습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommonResponse.class)))
  @PostMapping("/crawlall")
  public ResponseEntity<?> crawlAll() {
    try {
      crawlerService.crawlAll();
      return ResponseEntity.ok(
        new CommonResponse<>("전체 회차 크롤링 완료", "완료됨", null)
      );
    } catch (Exception e) {
      return ResponseEntity.status(500).body(
        new CommonResponse<>("크롤링 실패", null, "INTERNAL_ERROR. 크롤링 대상이나 서버를 확인하세요.")
      );
    }
  }

  // DB 상태 확인 메서드
  @Operation(summary = "로또 DB 무결성 검증", description = "로또 결과 테이블의 마지막 회차, 날짜, 총 저장 row 수를 확인합니다.")
  @ApiResponse(responseCode = "200", description = "정상적으로 데이터 상태가 반환되었습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommonResponse.class)))
  @GetMapping("/verify")
  public ResponseEntity<CommonResponse<LottoResultResponse>> verifyData() {
    try {
      LottoResultResponse verifiedResponse = resultService.getLottoStatus();
      return ResponseEntity.ok(
        new CommonResponse<>("데이터 무결성 검증 완료", verifiedResponse, null)
      );
    } catch (Exception e) {
      return ResponseEntity.status(500).body(
        new CommonResponse<>("데이터 검증 실패", null, "INTERNAL_SERVER_ERROR. 데이터가 훼손되었을 가능성이 높습니다.")
      );
    }
  }

  // DB 정보 조회(사용자 선택에 따른 번호 갯수 정보의 조회)
  @Operation(summary = "번호 출현 통계 조회", description = "지정된 회차 수를 기준으로 1~45 번호들의 등장 횟수를 반환합니다.")
  @ApiResponse(responseCode = "200", description = "번호 통계 결과가 성공적으로 반환되었습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommonResponse.class)))
  @GetMapping("/stats/distribution")
  public ResponseEntity<CommonResponse<Map<Long, Long>>> getDistribution(@RequestParam("range") long range) {
    try {
      Map<Long, Long> distribution = resultService.getNumberDistribution(range);
      return ResponseEntity.ok(
        new CommonResponse<>("번호 분포 통계 조회 완료", distribution, null)
      );
    } catch (Exception e) {
      return ResponseEntity.status(500).body(
        new CommonResponse<>("번호 통계 조회 실패", null, "DISTRIBUTION_QUERY_ERROR. 번호 조회를 실패하였습니다. DB 연결이나 서버 상태를 확인하세요.")
      );
    }
  }


}
