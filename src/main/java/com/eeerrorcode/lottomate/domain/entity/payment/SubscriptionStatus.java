package com.eeerrorcode.lottomate.domain.entity.payment;

public enum SubscriptionStatus {
  ACTIVE, INACTIVE, PENDING, CANCELLED;

  /**
   * 표시용 한글 상태명 반환
   * @return 한글 상태명
   */
  public String getDisplayName() {
    switch(this) {
      case ACTIVE:
        return "활성";
      case INACTIVE:
        return "비활성";
      case PENDING:
        return "대기";
      case CANCELLED:
        return "취소됨";
      default:
        return this.name();
    }
  }
}
