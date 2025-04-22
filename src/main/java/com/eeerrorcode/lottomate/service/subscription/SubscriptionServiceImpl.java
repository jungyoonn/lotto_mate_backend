package com.eeerrorcode.lottomate.service.subscription;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.eeerrorcode.lottomate.domain.dto.payment.*;
import com.eeerrorcode.lottomate.domain.dto.subscription.*;
import com.eeerrorcode.lottomate.domain.entity.payment.*;
import com.eeerrorcode.lottomate.domain.entity.user.User;
import com.eeerrorcode.lottomate.exeption.ResourceNotFoundException;
import com.eeerrorcode.lottomate.exeption.SubscriptionException;
import com.eeerrorcode.lottomate.repository.UserRepository;
import com.eeerrorcode.lottomate.repository.payment.*;
import com.eeerrorcode.lottomate.service.payment.PaymentService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Transactional
@Log4j2
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {
  private final SubscriptionRepository subscriptionRepository;
  private final PaymentRepository paymentRepository;
  private final SubscriptionPlanRepository subscriptionPlanRepository;
  private final UserRepository userRepository;
  private final PaymentMethodRepository paymentMethodRepository;
  private final SubscriptionCancellationRepository subscriptionCancellationRepository;
  private final PaymentService paymentService;
  private final SubscriptionPlanService subscriptionPlanService;

  @Override
  public SubscriptionResponseDto getSubscriptionInfo(Long userId) {
    // 활성 상태의 구독 정보 조희
    List<SubscriptionResponseDto> subscriptions = toResponseDtoList(subscriptionRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, SubscriptionStatus.ACTIVE));
        
    if (subscriptions.isEmpty()) {
      // 활성 구독이 없을 경우, 취소된 구독 조회
      subscriptions = toResponseDtoList(subscriptionRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, SubscriptionStatus.CANCELLED));
      if (subscriptions.isEmpty()) {
        return null; // 구독 정보 없음
      }
    }

    // 가장 최근 구독 정보, 결제 정보 가져오기
    SubscriptionResponseDto subscription = subscriptions.get(0);
    PaymentResponseDto paymentResponseDto = toDto(paymentRepository.findTopBySubscriptionIdAndPaymentStatusOrderByPaymentDateDesc(
      subscription.getId(), PaymentStatus.COMPLETE).orElse(null));

    PaymentMethodDto paymentMethodDto = subscription.getPaymentMethod();

    subscription.setPaymentMethod(paymentMethodDto);
    subscription.setRecentPayment(paymentResponseDto);

    return subscription;
  }

  @Override
  public Long createSubscription(Long userId, SubscriptionCreateRequestDto requestDto) {
    userRepository.findById(userId)
      .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + userId));

    // 플랜 정보 조회 (basic, standard, premium)
    Long planId = subscriptionPlanRepository.findByNameIgnoreCase(requestDto.getPlan())
      .orElseThrow(() -> new ResourceNotFoundException("구독 플랜을 찾을 수 없습니다: " + requestDto.getPlan())).getId();
          
    // 결제 수단 조회
    PaymentMethodDto paymentMethodDto = null;
    if (requestDto.getPaymentMethodId() != null) {
      paymentMethodDto = toDto(paymentMethodRepository.findByIdAndUserId(requestDto.getPaymentMethodId(), userId)
        .orElseThrow(() -> new ResourceNotFoundException("결제 수단을 찾을 수 없습니다: " + requestDto.getPaymentMethodId())));
    }

    // 구독 기간 설정 (월간/연간)
    int durationMonths = "monthly".equals(requestDto.getPeriod()) ? 1 : 12;
    
    // 시작일, 종료일, 다음 결제일 설정
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime endDate = now.plusMonths(durationMonths);

    SubscriptionDto subscriptionDto = SubscriptionDto.builder()
      .userId(userId)
      .planId(planId)
      .status(SubscriptionStatus.PENDING) // 결제 전이므로 PENDING 상태
      .startDate(now)
      .endDate(endDate)
      .autoRenewal(requestDto.isAutoRenewal())
      .nextPaymentDate(endDate)
      .paymentMethod(paymentMethodDto)
      .build();

    Long subscriptionId = subscriptionRepository.save(toEntity(subscriptionDto)).getId();
    log.info("구독 생성 완료 => {}", subscriptionId);

    return subscriptionId;
  }

  @Override
  public Long verifyPaymentAndActivateSubscription(Long userId, SubscriptionVerifyPaymentRequestDto requestDto) {
    // 결제 검증 (포트원 API 호출)
    paymentService.verifyPayment(requestDto.getImpUid(), requestDto.getMerchantUid(), requestDto.getAmount());

    SubscriptionPlanDto planDto = subscriptionPlanService.getPlanByName(requestDto.getPlan());
    if (planDto == null) {
      throw new ResourceNotFoundException("구독 플랜을 찾을 수 없습니다: " + requestDto.getPlan());
    }

    userRepository.findById(userId)
      .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + userId));

    // 구독 기간 설정 (월간/연간)
    int durationMonths = "monthly".equals(requestDto.getPeriod()) ? 1 : 12;
      
    // 시작일, 종료일, 다음 결제일 설정
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime endDate = now.plusMonths(durationMonths);
    LocalDateTime nextPaymentDate = endDate;

    // 이미 존재하는 활성 구독 확인
    List<SubscriptionDto> activeSubscriptions = getSubscriptionsByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE);
    
    for (SubscriptionDto activeSubscription : activeSubscriptions) {
      // Dto 수정 (취소 상태로)
      activeSubscription.setStatus(SubscriptionStatus.CANCELLED);
      activeSubscription.setAutoRenewal(false);
      
      // 종료일 설정 (이미 지났으면 현재 시간으로)
      if (activeSubscription.getEndDate() == null || activeSubscription.getEndDate().isBefore(now)) {
        activeSubscription.setEndDate(now);
      }
      
      subscriptionRepository.save(toEntity(activeSubscription));
    }

    // 새 구독 Dto 생성
    SubscriptionDto newSubscriptionDto = SubscriptionDto.builder()
      .userId(userId)
      .planId(planDto.getId())
      .planName(planDto.getName())
      .status(SubscriptionStatus.ACTIVE)
      .startDate(now)
      .endDate(endDate)
      .autoRenewal(true) // 기본적으로 자동 갱신 활성화
      .nextPaymentDate(nextPaymentDate)
      .build();
    
    // Dto를 저장
    Long subscriptionId = subscriptionRepository.save(toEntity(newSubscriptionDto)).getId();

    // 결제 정보 DTO 생성
    PaymentDto paymentDto = PaymentDto.builder()
      .userId(userId)
      .subscriptionId(subscriptionId)
      .amount(requestDto.getAmount())
      .paymentMethod("CARD") // 포트원 결제는 카드 결제로 가정
      .paymentStatus(PaymentStatus.COMPLETE)
      .merchantUid(requestDto.getMerchantUid())
      .impUid(requestDto.getImpUid())
      .paymentDate(now)
      .refundAmount(BigDecimal.ZERO)
      .build();

    // Dto를 저장
    paymentRepository.save(toEntity(paymentDto));

    log.info("구독 활성화 완료 => {}", subscriptionId);

    return subscriptionId;
  }

  /**
  * 사용자 ID와 상태로 구독 목록 조회 - Dto로 반환하는 헬퍼 메서드
  */
  private List<SubscriptionDto> getSubscriptionsByUserIdAndStatus(Long userId, SubscriptionStatus status) {
    return subscriptionRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status)
      .stream()
      .map(this::toDto)
      .collect(Collectors.toList());
  }

  @Override
  public Long cancelSubscription(Long userId, SubscriptionCancelRequestDto requestDto) {
    // requestDto에 cancellationType이 없는 경우 기본값 설정
    CancellationType cancellationType = requestDto.getCancellationType();
    if (cancellationType == null) {
      cancellationType = CancellationType.END_OF_PERIOD; // 기본값 설정
    }
    
    return cancelSubscriptionWithHistory(userId, requestDto, cancellationType);
  }

  /**
   * 구독 ID로 구독 정보 조회 - Dto로 반환하는 헬퍼 메서드
   */
  private SubscriptionDto getSubscriptionById(Long subscriptionId) {
    return subscriptionRepository.findById(subscriptionId)
      .map(this::toDto)
      .orElse(null);
  }

  /**
   * 사용자 ID와 구독 ID로 구독 정보 조회 - Dto로 반환하는 헬퍼 메서드
   */
  private SubscriptionDto getSubscriptionByIdAndUserId(Long subscriptionId, Long userId) {
    return subscriptionRepository.findByIdAndUserId(subscriptionId, userId)
      .map(this::toDto)
      .orElse(null);
  }

  /**
   * 구독 취소와 취소 이력 생성을 하나의 트랜잭션으로 처리
   */
  @Transactional
  public Long cancelSubscriptionWithHistory(Long userId, SubscriptionCancelRequestDto requestDto, CancellationType cancellationType) {
    // 구독 정보 조회 - DTO로 변환
    SubscriptionDto subscriptionDto = getSubscriptionByIdAndUserId(requestDto.getSubscriptionId(), userId);
    if (subscriptionDto == null) {
      throw new ResourceNotFoundException("구독 정보를 찾을 수 없습니다: " + requestDto.getSubscriptionId());
    }

    if (subscriptionDto.getStatus() == SubscriptionStatus.CANCELLED) {
      throw new SubscriptionException("이미 취소된 구독입니다.");
    }
    
    // DTO 수정
    subscriptionDto.setStatus(SubscriptionStatus.CANCELLED);
    subscriptionDto.setAutoRenewal(false);
    
    // 수정된 DTO 저장
    subscriptionRepository.save(toEntity(subscriptionDto));
    
    // 취소 이력 생성을 위한 DTO 생성
    SubscriptionCancellationRequestDto cancellationRequestDto = SubscriptionCancellationRequestDto.builder()
      .subscriptionId(subscriptionDto.getId())
      .reason(requestDto.getReason())
      .cancellationType(cancellationType)
      .build();
        
    // 취소 이력 생성 - 내부 메서드 이용
    Long cancellationId = createCancellation(userId, cancellationRequestDto);
    
    log.info("구독 취소 및 이력 생성 완료: subscriptionId={}, cancellationId={}", 
      subscriptionDto.getId(), cancellationId);
    
    return subscriptionDto.getId();
  }

  @Override
  public SubscriptionDetailsResponseDto getSubscriptionDetails(Long userId, String impUid) {
    // 결제 정보로 구독 정보 조회
    Payment payment = paymentRepository.findByImpUidAndUserId(impUid, userId)
      .orElseThrow(() -> new ResourceNotFoundException("결제 정보를 찾을 수 없습니다: " + impUid));
        
    if (payment.getSubscription() == null) {
      throw new ResourceNotFoundException("해당 결제와 연결된 구독 정보가 없습니다.");
    }
    
    Subscription subscription = payment.getSubscription();
    SubscriptionPlan plan = subscription.getPlan();
    
    // 구독 기간 (monthly/yearly) 결정
    String period = plan.getDurationMonths() <= 1 ? "monthly" : "yearly";
    
    return SubscriptionDetailsResponseDto.builder()
      .id(subscription.getId())
      .plan(plan.getName().toLowerCase())
      .period(period)
      .amount(payment.getAmount())
      .startDate(subscription.getStartDate())
      .nextPaymentDate(subscription.getNextPaymentDate())
      .merchantUid(payment.getMerchantUid())
      .build();
  }

  @Override
  public List<SubscriptionResponseDto> getAllSubscriptions(Long userId) {
    List<Subscription> subscriptions = subscriptionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    return toResponseDtoList(subscriptions);
  }

  @Override
  public Long updateAutoRenewal(Long userId, Long subscriptionId, boolean autoRenewal) {
    SubscriptionDto subscription = toDto(subscriptionRepository.findByIdAndUserId(subscriptionId, userId)
        .orElseThrow(() -> new ResourceNotFoundException("구독 정보를 찾을 수 없습니다: " + subscriptionId)));
        
    subscription.setAutoRenewal(autoRenewal);
    Long subsId = subscriptionRepository.save(toEntity(subscription)).getId();
    
    log.info("자동 갱신 설정 변경 완료: {}, autoRenewal => {}", subsId, autoRenewal);
    
    return subsId;
  }

  @Override
  public Long changePlan(Long userId, Long subscriptionId, String newPlanName) {
    SubscriptionDto subscription = toDto(subscriptionRepository.findByIdAndUserId(subscriptionId, userId)
      .orElseThrow(() -> new ResourceNotFoundException("구독 정보를 찾을 수 없습니다: " + subscriptionId)));
        
    // 현재 플랜과 동일한 경우 변경 없음
    String planName = subscriptionPlanRepository.findById(subscription.getPlanId())
      .orElseThrow(() -> new ResourceNotFoundException("구독 정보를 찾을 수 없습니다: " + subscription.getPlanId())).getName();

    if (planName.equalsIgnoreCase(newPlanName)) {
      return subscriptionId;
    }
    
    // 새 플랜 조회
    // SubscriptionPlan newPlan = 
    subscriptionPlanRepository.findByNameIgnoreCase(newPlanName)
      .orElseThrow(() -> new ResourceNotFoundException("구독 플랜을 찾을 수 없습니다: " + newPlanName));
        
    // 다음 결제일에 적용될 플랜 변경
    // 실제로는 다음 결제 시 적용될 플랜 정보만 저장하고, 결제 시점에 플랜 변경 처리
    // 이 구현에서는 변경 이력 기록 정도만 하고 실제 변경은 아직 안함
    log.info("다음 결제일에 플랜 변경 예약: {} -> {}", planName, newPlanName);
    
    return subscriptionId;
  }

  @Override
  public Long changePaymentMethod(Long userId, Long subscriptionId, Long paymentMethodId) {
    SubscriptionDto subscription = toDto(subscriptionRepository.findByIdAndUserId(subscriptionId, userId)
      .orElseThrow(() -> new ResourceNotFoundException("구독 정보를 찾을 수 없습니다: " + subscriptionId)));
        
    PaymentMethodDto paymentMethod = toDto(paymentMethodRepository.findByIdAndUserId(paymentMethodId, userId)
      .orElseThrow(() -> new ResourceNotFoundException("결제 수단을 찾을 수 없습니다: " + paymentMethodId)));
        
    subscription.setPaymentMethod(paymentMethod);
    Long subId = subscriptionRepository.save(toEntity(subscription)).getId();
    
    log.info("결제 수단 변경 완료: {}, paymentMethodId={}", subId, paymentMethodId);
    
    return subId;
  }

  @Override
  public Long createCancellation(Long userId, SubscriptionCancellationRequestDto requestDto) {
    // 구독 정보 조회 - Dto로 변환
    SubscriptionDto subscriptionDto = getSubscriptionById(requestDto.getSubscriptionId());
    if (subscriptionDto == null) {
      throw new ResourceNotFoundException("구독 정보를 찾을 수 없습니다: " + requestDto.getSubscriptionId());
    }
    
    // 사용자 권한 확인
    if (!subscriptionDto.getUserId().equals(userId)) {
      throw new SubscriptionException("해당 구독에 대한 권한이 없습니다.");
    }
    
    // 취소 이력 Dto 생성
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime endDate;
    
    // 취소 유형에 따라 종료일 결정
    if (requestDto.getCancellationType() == CancellationType.IMMEDIATE_CANCEL 
        || requestDto.getCancellationType() == CancellationType.REFUND_REQUEST) {
      endDate = now;
    } else {
      endDate = subscriptionDto.getEndDate();
    }
    
    // 취소 이력 생성 및 저장
    SubscriptionCancellationDto cancellationDto = SubscriptionCancellationDto.builder()
    .subscriptionId(requestDto.getSubscriptionId())
    .userId(userId)
    .cancellationDate(now)
    .reason(requestDto.getReason())
    .effectiveEndDate(endDate)
    .refundProcessed(false)
    .adminProcessed(false)
    .cancellationType(requestDto.getCancellationType())
    .build();
      
    // DTO를 엔티티로 변환 후 저장
    SubscriptionCancellation cancellation = SubscriptionCancellation.builder()
    .subscription(subscriptionRepository.getReferenceById(cancellationDto.getSubscriptionId()))
    .user(userRepository.getReferenceById(cancellationDto.getUserId()))
    .cancellationDate(cancellationDto.getCancellationDate())
    .reason(cancellationDto.getReason())
    .effectiveEndDate(cancellationDto.getEffectiveEndDate())
    .refundProcessed(cancellationDto.isRefundProcessed())
    .adminProcessed(cancellationDto.isAdminProcessed())
    .cancellationType(cancellationDto.getCancellationType())
    .build();

    Long cancellationId = subscriptionCancellationRepository.save(cancellation).getId();
    log.info("구독 취소 이력 생성 완료: {}", cancellationId);
    
    return cancellationId;
  }

  @Override
  public List<SubscriptionCancellationResponseDto> getCancellationHistory(Long userId) {
    List<SubscriptionCancellation> cancellations = subscriptionCancellationRepository.findByUserIdOrderByCancellationDateDesc(userId);
    return toCancellationResponseDtoList(cancellations);
  }

  @Override
  public SubscriptionCancellationDto getCancellationDetail(Long userId, Long cancellationId) {
    SubscriptionCancellationDto cancellation = toCancellationDto(subscriptionCancellationRepository.findById(cancellationId)
      .orElseThrow(() -> new ResourceNotFoundException("취소 이력을 찾을 수 없습니다: " + cancellationId)));
    
    // 사용자 권한 확인
    if (!cancellation.getUserId().equals(userId)) {
      throw new SubscriptionException("해당 취소 이력에 대한 권한이 없습니다.");
    }
    
    return cancellation;
  }

  @Override
  public Long processCancellationAdmin(SubscriptionCancellationAdminRequestDto requestDto) {
    SubscriptionCancellationDto cancellation = toCancellationDto(subscriptionCancellationRepository.findById(requestDto.getCancellationId())
      .orElseThrow(() -> new ResourceNotFoundException("취소 이력을 찾을 수 없습니다: " + requestDto.getCancellationId())));
    
    // 관리자 처리 상태 업데이트
    cancellation.setAdminProcessed(requestDto.isAdminProcessed());
    cancellation.setAdminNote(requestDto.getAdminNote());
    
    // 환불 처리 요청인 경우
    if (requestDto.isProcessRefund() && !cancellation.isRefundProcessed()) {
      // 환불 로직 구현 (PaymentService 호출)
      paymentService.processRefund(cancellation.getSubscriptionId());
      
      // 환불 처리 완료로 상태 변경
      cancellation.setRefundProcessed(true);
    }
    
    Long savedCancellationId = subscriptionCancellationRepository.save(toCancellationEntity(cancellation)).getId();
    log.info("구독 취소 관리자 처리 완료: {}", savedCancellationId);
    
    return savedCancellationId;
  }
  
  @Override
public List<SubscriptionResponseDto> getAllSubscriptionsForAdmin() {
  // 모든 구독 정보 조회 (모든 상태)
  List<Subscription> allSubscriptions = subscriptionRepository.findAll();
  List<SubscriptionResponseDto> responseDtoList = toResponseDtoList(allSubscriptions);
  
  // 추가 사용자 정보 설정
  responseDtoList.forEach(dto -> {
    Subscription subscription = subscriptionRepository.findById(dto.getId()).orElse(null);
    if (subscription != null) {
      User user = subscription.getUser();
      // 사용자 정보를 응답 DTO에 설정
      dto.setUserName(user.getName());
      dto.setUserEmail(user.getEmail());
    }
  });
  
  log.info("관리자용 모든 구독 정보 조회: 총 {}건", responseDtoList.size());
  return responseDtoList;
}

@Override
public List<SubscriptionCancellationResponseDto> getAllCancellationRequestsForAdmin() {
  // 미처리된 취소 요청 먼저 조회
  List<SubscriptionCancellation> unprocessedCancellations = 
      subscriptionCancellationRepository.findByAdminProcessedFalseOrderByCancellationDateAsc();
      
  // 최근 처리된 취소 요청도 포함 (최근 7일 이내)
  LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
  List<SubscriptionCancellation> recentProcessedCancellations = 
      subscriptionCancellationRepository.findByCancellationDateBetweenOrderByCancellationDateDesc(
          oneWeekAgo, LocalDateTime.now())
          .stream()
          .filter(cancellation -> cancellation.isAdminProcessed())
          .collect(Collectors.toList());
  
  // 미처리 요청과 최근 처리된 요청 병합
  List<SubscriptionCancellation> allCancellations = new ArrayList<>(unprocessedCancellations);
  allCancellations.addAll(recentProcessedCancellations);
  
  // 응답 DTO로 변환
  List<SubscriptionCancellationResponseDto> responseDtoList = toCancellationResponseDtoList(allCancellations);
  
  // 추가 정보 설정
  responseDtoList.forEach(dto -> {
    SubscriptionCancellation cancellation = subscriptionCancellationRepository.findById(dto.getId()).orElse(null);
    if (cancellation != null) {
      User user = cancellation.getUser();
      // 사용자 정보 설정
      dto.setUserName(user.getName());
      dto.setUserEmail(user.getEmail());
      
      // 관리자 처리 여부 설정
      dto.setAdminProcessed(cancellation.isAdminProcessed());
      
      // 환불 처리 여부 설정
      dto.setRefundProcessed(cancellation.isRefundProcessed());
    }
  });
  
  log.info("관리자용 구독 취소 요청 조회: 총 {}건", responseDtoList.size());
  return responseDtoList;
}
}
