package com.eeerrorcode.lottomate.domain.entity.payment;

public enum PaymentStatus {
  PENDING, COMPLETE, FAILED, REFUNDED, PARTIAL_REFUNDED;  

  /**
   * 표시용 한글 상태명 반환
   * @return 한글 상태명
   */
  public String getDisplayName() {
    switch(this) {
      case PENDING:
        return "대기";
      case COMPLETE:
        return "완료";
      case FAILED:
        return "실패";
      case REFUNDED:
        return "전액 환불";
      case PARTIAL_REFUNDED:
        return "부분 환불";
      default:
        return this.name();
    }
  }
}
