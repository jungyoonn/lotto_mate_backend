package com.eeerrorcode.lottomate.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eeerrorcode.lottomate.domain.dto.CommonResponse;
import com.eeerrorcode.lottomate.domain.dto.lotto.HistoricalHeatmapResponse;
import com.eeerrorcode.lottomate.domain.dto.lotto.LottoLatestResponse;
import com.eeerrorcode.lottomate.domain.dto.lotto.LottoNumberHitmapResponse;
import com.eeerrorcode.lottomate.domain.dto.lotto.LottoRecommendOption;
import com.eeerrorcode.lottomate.domain.dto.lotto.LottoRecommendResponse;
import com.eeerrorcode.lottomate.domain.dto.lotto.LottoResultResponse;
import com.eeerrorcode.lottomate.domain.dto.lotto.LottoUserHistoryRequest;
import com.eeerrorcode.lottomate.domain.dto.lotto.LottoUserHistoryResponse;
import com.eeerrorcode.lottomate.service.lotto.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lotto")
@Tag(name = "Lotto API", description = "로또 다양한 기능을 포함한 주요 API 그룹")
public class LottoController {

  private final LottoCrawlerService crawlerService;
  private final LottoResultService resultService;
  private final LottoRecommendService lottoRecommendService;
  private final LottoHistoryService lottoHistoryService;

  // 전체 크롤링 메서드(DB 업데이트용)
  @Operation(summary = "전체 회차 크롤링 실행", description = "1회차부터 현재까지의 모든 로또 당첨 정보를 동행복권 사이트에서 크롤링하여 DB에 저장합니다.")
  @ApiResponse(responseCode = "200", description = "전체 회차 크롤링이 성공적으로 수행되었습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommonResponse.class)))
  @PostMapping("/crawlall")
  public ResponseEntity<?> crawlAll() {
    try {
      crawlerService.crawlAll();
      return ResponseEntity.ok(
          new CommonResponse<>("전체 회차 크롤링 완료", "완료됨", null));
    } catch (Exception e) {
      return ResponseEntity.status(500).body(
          new CommonResponse<>("크롤링 실패", null, "INTERNAL_ERROR. 크롤링 대상이나 서버를 확인하세요."));
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
          new CommonResponse<>("데이터 무결성 검증 완료", verifiedResponse, null));
    } catch (Exception e) {
      return ResponseEntity.status(500).body(
          new CommonResponse<>("데이터 검증 실패", null, "INTERNAL_SERVER_ERROR. 데이터가 훼손되었을 가능성이 높습니다."));
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
          new CommonResponse<>("번호 분포 통계 조회 완료", distribution, null));
    } catch (Exception e) {
      return ResponseEntity.status(500).body(
          new CommonResponse<>("번호 통계 조회 실패", null, "DISTRIBUTION_QUERY_ERROR. 번호 조회를 실패하였습니다. DB 연결이나 서버 상태를 확인하세요."));
    }
  }

  @Operation(summary = "로또 번호 추천", description = """
      회차 범위 및 다양한 필터 옵션을 바탕으로 당첨 가능성이 높은 번호 6개를 추천합니다.

      [모드 선택]:
      - HIGH_FREQUENCY: 많이 출현한 번호 중심
      - MIXED: 두 가지 모드를 모두 혼합한 무작위 셔플 기반 추천
      - LOW_FREQUENCY: 적게 출현한 번호 중심

      [필터 옵션]:
      - 고정 번호(fixedNumbers)는 무조건 포함
      - 회피 번호(excludedNumbers)는 추천 대상에서 제외
      - 짝/홀 비율 고정(3:3)을 비활성화하면 자유롭게 추천됩니다
      - 보너스 번호는 통계에만 포함되며, 추천 번호에는 포함되지 않습니다
      """)
  @ApiResponse(responseCode = "200", description = "추천 번호가 성공적으로 반환됩니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LottoRecommendResponse.class)))
  @PostMapping("/recommend")
  public ResponseEntity<CommonResponse<LottoRecommendResponse>> recommendNumbers(
      @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "로또 번호 추천을 위한 옵션 값 (회차 범위, 모드, 고정/회피 번호, 짝홀/보너스 포함 여부 등)", required = true, content = @Content(schema = @Schema(implementation = LottoRecommendOption.class))) LottoRecommendOption option) {
    try {
      LottoRecommendResponse response = lottoRecommendService.recommendNumbers(option);
      return ResponseEntity.ok(CommonResponse.success(response, "추천 번호 조회 성공"));
    } catch (IllegalArgumentException e) {
      return ResponseEntity
          .badRequest()
          .body(CommonResponse.error("INVALID_OPTION", "입력하신 옵션이 잘못되었습니다."));
    } catch (Exception e) {
      return ResponseEntity
          .status(500)
          .body(CommonResponse.error("RECOMMEND_ERROR", "추천 알고리즘 실행 중 오류가 발생했습니다."));
    }
  }

  @Validated
  @GetMapping("/user/history")
  @Operation(summary = "사용자 로또 기록 조회", description = "특정 사용자(userId)의 로또 구매 이력 및 당첨 정보를 페이징 형식으로 반환합니다.")
  @ApiResponse(responseCode = "200", description = "사용자 로또 이력 반환 성공", content = @Content(schema = @Schema(implementation = LottoUserHistoryResponse.class)))
  public ResponseEntity<CommonResponse<List<LottoUserHistoryResponse>>> getUserLottoHistory(
      @RequestParam @NotNull @Min(1) Long userId,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "10") @Min(1) int size) {

    List<LottoUserHistoryResponse> result = lottoHistoryService.logUserHistory(userId, page, size);
    return ResponseEntity.ok(CommonResponse.success(result, "사용자 로또 기록 조회 성공"));
  }

  // @Operation(summary = "회차별 번호 등장 여부 매트릭스 조회", description = """
  // 지정된 회차 수 범위 내에서 각 회차(drawRound)별로 1~45번 로또 번호의 등장 여부를 Boolean 값으로 반환합니다.

  // - true: 해당 번호가 해당 회차에 등장함
  // - false: 등장하지 않음

  // 이 정보는 Chart.js 기반의 히트맵 시각화 등에 사용됩니다.
  // """)
  // @ApiResponse(responseCode = "200", description = "히트맵 데이터 반환 성공", content =
  // @Content(mediaType = "application/json", schema = @Schema(implementation =
  // LottoNumberHitmapResponse.class)))
  // @GetMapping("/stats/hitmap")
  // public ResponseEntity<CommonResponse<LottoNumberHitmapResponse>>
  // getHitmapMatrix(
  // @RequestParam @Min(1) long range) {
  // TODO: 추후 차트 전환 시 재사용 고려
  // LottoNumberHitmapResponse matrix = resultService.getHitMapMatrix(range);
  // return ResponseEntity.ok(CommonResponse.success(matrix, "히트맵 데이터 조회 성공"));
  // }

  @Operation(summary = "사용자 로또 응모 기록 저장", description = "사용자가 응모한 로또 번호 기록을 저장합니다.")
  @Parameter(name = "userId", description = "응모 기록을 저장할 사용자 ID", required = true)
  @ApiResponse(responseCode = "200", description = "응모 기록 저장 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommonResponse.class)))
  @PostMapping("/user/history")
  public ResponseEntity<CommonResponse<Void>>
  saveUserLottoHistory(@RequestParam @NotNull @Min(1) Long userId,
  @RequestBody @Valid LottoUserHistoryRequest request) {
  try {
  lottoHistoryService.saveUserLottoHistory(userId, request);
  return ResponseEntity.ok(CommonResponse.success(null, "로또 응모 기록 저장 완료"));
  } catch (Exception e) {
  return
  ResponseEntity.status(500).body(CommonResponse.error("HISTORY_SAVE_FAILED",
  "응모 기록 저장 중 오류가 발생했습니다."));
  }
  }

  @PutMapping("/user/history/update")
  @Operation(summary = "사용자 로또 응모 결과 갱신", description = "DB에 저장된 로또 당첨 번호를 기준으로 사용자의 응모 결과를 갱신합니다.")
  @ApiResponse(responseCode = "200", description = "갱신 완료", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommonResponse.class)))
  public ResponseEntity<CommonResponse<Void>> updateWinningResults(
      @RequestParam @NotNull @Min(1) Long drawRound) {
    try {
      lottoHistoryService.updateWinningResults(drawRound);
      return ResponseEntity.ok(CommonResponse.success(null, drawRound + "회차 응모 결과 갱신 완료"));
    } catch (Exception e) {
      return ResponseEntity.status(500).body(
          CommonResponse.error("UPDATE_FAILED", "결과 갱신 중 오류가 발생했습니다."));
    }
  }

  @GetMapping("/stats/heatmap-range")
  public ResponseEntity<CommonResponse<LottoNumberHitmapResponse>> getHitmapMatrixRange(
      @RequestParam @Min(1) long startRound,
      @RequestParam @Min(1) long endRound) {
    LottoNumberHitmapResponse matrix = resultService.getHitMapMatrixByRange(startRound, endRound);
    return ResponseEntity.ok(CommonResponse.success(matrix, "히트맵 범위 지정 데이터 조회 성공"));
  }

  @Operation(summary = "번호별 등장 횟수 분포", description = """
        startRound ~ endRound 범위 내에서 각 번호(1~45)의 총 등장 횟수를 반환합니다.
        예: {1: 238, 2: 201, ..., 45: 198}
      """)
  @GetMapping("/stats/number-distribution-range")
  public ResponseEntity<CommonResponse<Map<Integer, Integer>>> getNumberDistributionByRange(
      @RequestParam @Min(1) long startRound,
      @RequestParam @Min(1) long endRound) {

    Map<Integer, Integer> result = resultService.getNumberDistributionByRange(startRound, endRound);
    return ResponseEntity.ok(CommonResponse.success(result, "번호 분포 통계 조회 성공"));
  }

  @GetMapping("/stats/history-heatmap")
  @Operation(summary = "히스토리컬 히트맵 조회", description = "지정된 회차 범위(startRound ~ endRound) 내에서 각 번호(1~45번)가 등장한 회차를 매핑하여 반환합니다.")
  @ApiResponse(responseCode = "200", description = "히트맵 데이터 반환 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = HistoricalHeatmapResponse.class)))
  public ResponseEntity<CommonResponse<HistoricalHeatmapResponse>> getHistoricalHeatmap(@RequestParam @Min(1) long startRound, @RequestParam @Min(1) long endRound) {
    Map<Integer, Map<Long, Integer>> matrix = resultService.getHistoricalHitmap(startRound, endRound);
    return ResponseEntity.ok(CommonResponse.success(new HistoricalHeatmapResponse(matrix), "히스토리컬 히트맵 조회 성공"));
  }
    
  @GetMapping("/latest")
  public ResponseEntity<CommonResponse<LottoLatestResponse>> getLatestLotto() {
    LottoLatestResponse response = resultService.getLatestDraw();
    return ResponseEntity.ok(CommonResponse.success(response));
  }

}
