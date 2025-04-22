package com.eeerrorcode.lottomate.aop;

import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

/**
 * 공통 로깅 AOP 클래스입니다.
 * <p>
 * 모든 Controller 및 Service 계층의 진입 및 반환 시점에서 로그를 출력하며,
 * 디버깅, 예외 분석, 호출 흐름 추적 등을 지원합니다.
 * </p>
 * <ul>
 *   <li>진입 시점 로그: 호출 메서드 이름, 파라미터</li>
 *   <li>반환 시점 로그: 결과 타입 및 결과 요약 (70자 이상 생략)</li>
 * </ul>
 *
 * <p>
 * 예외는 {@code GlobalExceptionHandler} 에서 처리되며, AOP에서는 잡지 않습니다.
 * </p>
 *
 * @author DahnDell
 */
@Aspect
@Component
@Log4j2
public class CommonLogger {

  // Controller 진입점
  @Pointcut("execution(* com.eeerrorcode.lottomate.controller..*(..))")
  public void controllerMethods() {
  }

  // Service 진입점
  @Pointcut("execution(* com.eeerrorcode.lottomate.service..*(..))")
  public void serviceMethods() {
  }

  // 공통 진입점 로깅
  @Before("controllerMethods() || serviceMethods()")
  public void logBefore(JoinPoint joinPoint) {
    String methodName = joinPoint.getSignature().toShortString();
    Object[] args = joinPoint.getArgs();
    log.info("→ 진입점: {} | 파라미터: {}", methodName, Arrays.toString(args));
  }

  // 공통 반환점 로깅 (길이 길면 요약 출력)
  @AfterReturning(pointcut = "controllerMethods() || serviceMethods()", returning = "result")
  public void logAfterReturning(JoinPoint joinPoint, Object result) {
    String methodName = joinPoint.getSignature().toShortString();

    if (result == null) {
      log.info("← 반환점: {} | 타입: null | 리턴: null", methodName);
      return;
    }

    String resultType = result.getClass().getSimpleName();
    String resultStr = result.toString();

    if (resultStr.length() > 70) {
      log.info("← 반환점: {} | 타입: {} | 리턴: (생략 - 길이 {}자)", methodName, resultType, resultStr.length());
    } else {
      log.info("← 반환점: {} | 타입: {} | 리턴: {}", methodName, resultType, resultStr);
    }
  }

}
