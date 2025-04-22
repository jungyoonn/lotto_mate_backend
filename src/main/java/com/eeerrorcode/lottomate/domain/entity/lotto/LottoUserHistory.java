package com.eeerrorcode.lottomate.domain.entity.lotto;

import com.eeerrorcode.lottomate.domain.entity.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lotto_user_history") // 명시적 테이블 이름 부여
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LottoUserHistory extends BaseEntity{
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "draw_round", nullable = false)
  private Long drawRound;

  @Column(name = "numbers", nullable = false, length = 50)
  private String numbers; 

  @Column(name = "is_auto", nullable = false)
  private boolean isAuto;

  @Column(name = "name")
  private String name;

  @Column(name = "is_subscribed", nullable = false)
  private boolean isSubscribed;

  @Column(name = "winning_rank")
  private Integer winningRank;

  @Column(name = "winning_amount")
  private Long winningAmount; 

  @Column(name = "is_claimed", nullable = false)
  private boolean isClaimed; 
}
