package com.eeerrorcode.lottomate.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eeerrorcode.lottomate.service.LottoCrawlerService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lotto")
public class LottoController {

    private final LottoCrawlerService crawlerService;

    @PostMapping("/crawlall")
    public ResponseEntity<?> crawlAll() {
        crawlerService.crawlAll();
        return ResponseEntity.ok("전체 회차 크롤링을 성공적으로 완료했습니다.");
    }
    
    
}
