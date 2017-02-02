/**
 * Created by zigza on 02.02.2017.
 */
package com.intita.wschat.aspects;

        import org.aspectj.lang.ProceedingJoinPoint;
        import org.aspectj.lang.annotation.Around;
        import org.aspectj.lang.annotation.Aspect;
        import org.slf4j.Logger;
        import org.slf4j.LoggerFactory;
        import org.springframework.core.annotation.Order;
        import org.springframework.stereotype.Component;
        import org.springframework.util.StopWatch;

@Aspect
@Order(value=4)
@Component
public class LoggingAspect {
    //@Around("execution(* com.blablabla.server..*.*(..))")
    //@Around("execution(* com.intita.forum.services..*.*(..))")
    @Around("execution(* com.intita.wschat.services..*(..)) || execution(* com.intita.wschat.repositories..*(..)) || execution(* com.intita.wschat.web..*(..))")
    public Object logMethod(ProceedingJoinPoint joinPoint) throws Throwable{
        final Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass().getName());
        Object retVal = null;

        try {
            StringBuffer startMessageStringBuffer = new StringBuffer();

            startMessageStringBuffer.append("Start method ");
            startMessageStringBuffer.append(joinPoint.getSignature().getName());
            startMessageStringBuffer.append("(");

            Object[] args = joinPoint.getArgs();
            for (int i = 0; i < args.length; i++) {
                startMessageStringBuffer.append(args[i]).append(",");
            }
            if (args.length > 0) {
                startMessageStringBuffer.deleteCharAt(startMessageStringBuffer.length() - 1);
            }

            startMessageStringBuffer.append(")");

            //logger.trace(startMessageStringBuffer.toString());


            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            retVal = joinPoint.proceed();

            stopWatch.stop();
            long executionTime = stopWatch.getTotalTimeMillis();
            if (executionTime<1000) return retVal;
            logger.info(startMessageStringBuffer.toString());
            StringBuffer endMessageStringBuffer = new StringBuffer();
            endMessageStringBuffer.append("WARNING! SLOW METHOD. Finish method ");
            endMessageStringBuffer.append(joinPoint.getSignature().getName());
            endMessageStringBuffer.append("(..); execution time: ");
            endMessageStringBuffer.append(stopWatch.getTotalTimeMillis());
            endMessageStringBuffer.append(" ms;");

            //logger.trace(endMessageStringBuffer.toString());
            logger.info(endMessageStringBuffer.toString());
        } catch (Throwable ex) {
            StringBuffer errorMessageStringBuffer = new StringBuffer();

            // Create error message
            logger.error(errorMessageStringBuffer.toString(), ex);

            throw ex;
        }

        return retVal;
    }
}
