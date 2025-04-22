package com.eeerrorcode.lottomate.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageUtil {

  /**
   * Pageable 객체 생성 유틸리티
   *
   * @param page       페이지 번호 (0부터 시작)
   * @param size       한 페이지당 항목 수
   * @param sortBy     정렬 기준 컬럼명
   * @param descending true면 DESC, false면 ASC 정렬
   * @return Pageable
   */
  public static Pageable of(int page, int size, String sortBy, boolean descending) {
    Sort.Direction direction = descending ? Sort.Direction.DESC : Sort.Direction.ASC;
    return PageRequest.of(page, size, Sort.by(direction, sortBy));
  }

}