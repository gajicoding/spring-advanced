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

import java.lang.annotation.Annotation;
import java.time.LocalDateTime;

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
        Annotation[][] parameterAnnotations = ((MethodSignature) joinPoint.getSignature()).getMethod().getParameterAnnotations();
        Object[] args = joinPoint.getArgs();
        Object requestBody = null;
        for (int i = 0; i < parameterAnnotations.length; i++) {
            for (Annotation annotation : parameterAnnotations[i]) {
                if (annotation.annotationType().equals(RequestBody.class)) {
                    requestBody = args[i];
                    break;
                }
            }
            if (requestBody != null) break;
        }

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
