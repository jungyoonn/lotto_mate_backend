package com.eeerrorcode.lottomate.domain.entity.payment;

public enum CancellationType {
  IMMEDIATE_CANCEL, END_OF_PERIOD, REFUND_REQUEST, PAYMENT_FAILED, ADMIN_CANCEL;
  
  /**
   * 표시용 이름 반환
   * @return 취소 유형의 한글 이름
   */
  public String getDisplayName() {
    switch(this) {
    case IMMEDIATE_CANCEL:
      return "즉시 취소";
    case END_OF_PERIOD:
      return "기간 종료 후 취소";
    case REFUND_REQUEST:
      return "환불 요청";
    case PAYMENT_FAILED:
      return "결제 실패";
    case ADMIN_CANCEL:
      return "관리자 취소";
    default:
      return this.name();
    }
  }
}
