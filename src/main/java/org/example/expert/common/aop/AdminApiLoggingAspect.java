package org.example.expert.common.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.IntStream;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AdminApiLoggingAspect {

    private final HttpServletRequest httpServletRequest;
    private final ObjectMapper objectMapper;

    @Around("@annotation(org.example.expert.common.annotation.AdminLog)")
    public Object logAdminApi(ProceedingJoinPoint joinPoint) throws Throwable {

        // 요청 시각
        long startTime = System.currentTimeMillis();

        // 사용자 ID 추출
        Long userId = (Long) httpServletRequest.getAttribute("userId");

        // 요청 URL
        String requestUrl = httpServletRequest.getRequestURI();

        // 요청 본문
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();   // 메서드
        Object[] args = joinPoint.getArgs();                                        // 요청 인자 값
        Object requestBody = IntStream.range(0, method.getParameterCount())
                .filter(i -> Arrays.stream(method.getParameterAnnotations()[i])
                        .anyMatch(a -> a.annotationType().equals(RequestBody.class)))   // RequestBody 찾기
                .mapToObj(i -> args[i])
                .findFirst()
                .orElse(null);

        log.info(
            "[ADMIN API REQUEST] userId={}, url={}, time={}, requestBody={}",
            userId, requestUrl, LocalDateTime.now(), objectMapper.writeValueAsString(requestBody)
        );

        // 메서드 실행
        Object response = joinPoint.proceed();

        long duration = System.currentTimeMillis() - startTime;

        log.info(
            "[ADMIN API RESPONSE] userId={}, url={}, duration={}ms, responseBody={}",
            userId, requestUrl, duration, objectMapper.writeValueAsString(response)
        );

        return response;
    }
}
