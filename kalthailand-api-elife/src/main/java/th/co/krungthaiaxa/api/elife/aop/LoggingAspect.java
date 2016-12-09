package th.co.krungthaiaxa.api.elife.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import th.co.krungthaiaxa.api.common.log.LogUtil;

import java.time.Instant;
import java.util.Arrays;

/**
 * Aspect for logging execution of service and repository Spring components.
 */
@Aspect
public class LoggingAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("within(th.co.krungthaiaxa.api.elife.initiation..*) || within(th.co.krungthaiaxa.api.elife.products.*..*)")
    public void loggingPointcut() {
    }

    @AfterThrowing(pointcut = "loggingPointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        LOGGER.error("Exception in {}.{}() with cause = {} and exception {}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(), e.getCause(), e);
    }

    @Around("loggingPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Instant startTime = Instant.now();
        LOGGER.debug("Begin: {}.{}() with argument[s] = {}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(), Arrays.toString(joinPoint.getArgs()));
        try {
            Object result = joinPoint.proceed();
            String msg = String.format("End: %s.%s() with result = %s", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(), result);
            LogUtil.logFinishing(startTime, msg);
            return result;
        } catch (IllegalArgumentException e) {
            String msg = String.format("Error: %s.%s() with exception: %s", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(), e);
            LogUtil.logFinishing(startTime, msg);
            throw e;
        }
    }
}
