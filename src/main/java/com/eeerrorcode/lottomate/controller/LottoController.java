package com.eeerrorcode.lottomate.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eeerrorcode.lottomate.domain.dto.ApiResponse;
import com.eeerrorcode.lottomate.domain.dto.lotto.LottoResultResponse;
import com.eeerrorcode.lottomate.service.lotto.*;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;



@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lotto")
public class LottoController {

    private final LottoCrawlerService crawlerService;
    private final LottoResultService resultService;


    // 전체 크롤링 메서드(DB 업데이트용)
    @PostMapping("/crawlall")
    public ResponseEntity<?> crawlAll() {
        crawlerService.crawlAll();
        return ResponseEntity.ok("전체 회차 크롤링을 성공적으로 완료했습니다.");
    }
    
    // DB 상태 확인 메서드
    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<LottoResultResponse>> verifyData() {
        LottoResultResponse verifiedResponse = resultService.getLottoStatus();
        return ResponseEntity.ok(new ApiResponse<>("데이터 검증 완료", verifiedResponse));
    }
    
    
}
