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
 * 어떤 메서드, 어떤 서비스, 어떤 컨트롤러를 탔는지?
 * 로또메이트 전반의 흐름 제어 확인용 AOP입니다.
 * - 진입, 반환 시점의 로그를 기록합니다.
 * - 예외는 GlobalExceptionHandler가 처리하도록 별다른 처리는 하지 않았습니다.
 */
@Aspect
@Component
@Log4j2
public class CommonLogger {

  // Controller 진입 포인트
  @Pointcut("execution(* com.eeerrorcode.lottomate.controller..*(..))")
  public void controllerMethods() {
  }

  // Service 진입 포인트
  @Pointcut("execution(* com.eeerrorcode.lottomate.service..*(..))")
  public void serviceMethods() {
  }

  // 공통 진입 로그
  @Before("controllerMethods() || serviceMethods()")
  public void logBefore(JoinPoint joinPoint) {
    String methodName = joinPoint.getSignature().toShortString();
    Object[] args = joinPoint.getArgs();
    log.info("→ 진입점: {} | 파라미터: {}", methodName, Arrays.toString(args));
  }

  // 공통 반환 로그 (요약 출력)
  @AfterReturning(pointcut = "controllerMethods() || serviceMethods()", returning = "result")
  public void logAfterReturning(JoinPoint joinPoint, Object result) {
    String methodName = joinPoint.getSignature().toShortString();

    if (result == null) {
      log.info("← 반환점: {} | 타입: null | 리턴턴: null", methodName);
      return;
    }

    String resultType = result.getClass().getSimpleName();
    String resultStr = result.toString();

    if (resultStr.length() > 70) {
      log.info("← 반환점: {} | 타입: {} | 리턴: (길이가 길어 생략됩니다 - 길이 {}자)", methodName, resultType, resultStr.length());
    } else {
      log.info("← 반환점: {} | 타입: {} | 리턴턴: {}", methodName, resultType, resultStr);
    }
  }

}
