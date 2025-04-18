package com.eeerrorcode.lottomate.domain.entity.payment;

public enum PaymentLogAction {
  PAYMENT_ATTEMPT,   // 결제 시도
  PAYMENT_SUCCESS,   // 결제 성공
  PAYMENT_FAILED,    // 결제 실패
  REFUND_REQUEST,    // 환불 요청
  REFUND_SUCCESS;    // 환불 성공

  /**
   * 표시용 한글 상태명 반환
   * @return 한글 상태명
   */
  public String getDisplayName() {
    switch(this) {
      case PAYMENT_ATTEMPT:
        return "결제 시도";
      case PAYMENT_SUCCESS:
        return "결제 성공";
      case PAYMENT_FAILED:
        return "결제 실패";
      case REFUND_REQUEST:
        return "환불 요청";
      case REFUND_SUCCESS:
        return "환불 성공";
      default:
        return this.name();
    }
  }
}
