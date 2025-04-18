package com.eeerrorcode.lottomate.domain.entity.payment;

public enum PaymentMethodType {
  CARD, BANK, PHONE, VIRTUAL_ACCOUNT, KAKAO_PAY, NAVER_PAY, TOSS;

  /**
   * 표시용 한글 상태명 반환
   * @return 한글 상태명
   */
  public String getDisplayName() {
    switch(this) {
      case CARD:
        return "신용/체크카드";
      case BANK:
        return "계좌이체";
      case PHONE:
        return "휴대폰 결제";
      case VIRTUAL_ACCOUNT:
        return "가상계좌";
      case KAKAO_PAY:
        return "카카오페이";
      case NAVER_PAY:
        return "네이버페이";
      case TOSS:
        return "토스";
      default:
        return this.name();
    }
  }
}
