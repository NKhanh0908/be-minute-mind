package com.be.minutemind.aspect;

import com.be.minutemind.annotation.RateLimit;
import org.springframework.web.server.ResponseStatusException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final LettuceBasedProxyManager<byte[]> proxyManager;

    @Around("@annotation(rateLimit)")
    public Object enforceRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        
        String ip = request.getRemoteAddr();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        // For auth endpoints, rate limit by IP
        String key = "vilo:ratelimit:" + ip + ":" + className + ":" + methodName;

        Bucket bucket = proxyManager.builder().build(key.getBytes(), () -> {
            Refill refill = Refill.intervally(rateLimit.requests(), Duration.ofSeconds(rateLimit.perSeconds()));
            Bandwidth limit = Bandwidth.classic(rateLimit.requests(), refill);
            return BucketConfiguration.builder().addLimit(limit).build();
        });

        if (bucket.tryConsume(1)) {
            return joinPoint.proceed();
        } else {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded");
        }
    }
}
