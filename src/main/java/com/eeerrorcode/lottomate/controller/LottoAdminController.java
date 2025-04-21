package com.eeerrorcode.lottomate.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eeerrorcode.lottomate.domain.dto.CommonResponse;
import com.eeerrorcode.lottomate.service.lotto.LottoScheduler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/lotto")
@RequiredArgsConstructor
@Tag(name = "Lotto Admin", description = "로또 수동 실행 / 관리자 API")
public class LottoAdminController {

  private final LottoScheduler lottoScheduler;

  @Operation(
    summary = "최신 회차 크롤링 수동 실행",
    description = "현재 저장된 최신 회차 +1 기준으로, 수동으로 로또 결과를 크롤링합니다.\n" +
                  "실제 스케줄러와 동일한 로직이며, 테스트나 예외 발생 시 수동으로 실행할 수 있습니다."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "크롤링 작업이 수동으로 실행됨"),
    @ApiResponse(responseCode = "500", description = "서버 내부 오류 발생")
  })
  @PostMapping("/crawl-latest")
  public CommonResponse<String> crawlLatestRound() {
    lottoScheduler.crawlLatestRoundIfNeeded();
    return CommonResponse.success("크롤링 작업이 수동으로 실행되었습니다.");
  }
  // 시간 자체도 관리자가 직접 수정할 수 있도록 변환 할 것
}
