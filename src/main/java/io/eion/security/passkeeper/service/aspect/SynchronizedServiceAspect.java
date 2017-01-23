package io.eion.security.passkeeper.service.aspect;

import io.eion.security.passkeeper.service.annotation.WriteOperation;
import io.eion.security.passkeeper.service.bean.SecureAccountRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * More information about lock can be found here:
 * http://flex4java.blogspot.com/2015/02/lock-reentrantlock-reentrantreadwritelo.html
 *
 * @author <a href="joelin@digitalriver.com">Joe Lin</a>
 */
@Aspect
@Component
public class SynchronizedServiceAspect {

    private static final Logger logger = LoggerFactory.getLogger(SynchronizedServiceAspect.class);

    /**
     * Key is the username. Value is the lock that is used to synchronize read and write operation
     * within SecureAccountService.
     */
    private ConcurrentHashMap<String, ReentrantReadWriteLock> locks = new ConcurrentHashMap<>();


    @Around("secureAccountService() && args(secureAccountRequest)")
    public Object controlAccess(final ProceedingJoinPoint pjp, final SecureAccountRequest secureAccountRequest) throws Throwable {

        final String username = secureAccountRequest.getUsername();
        final Lock lock = this.obtainReadWriteLock(username, pjp);
        logger.debug("Obtained lock of type {} for user: {}", lock.getClass().getSimpleName(), username);
        try {
            lock.lock();
            logger.trace("Lock for user: {}", username);
            return pjp.proceed();
        } finally {
            lock.unlock();
            logger.trace("Unlock for user: {}", username);
        }
    }

    private Lock obtainReadWriteLock(final String username, final ProceedingJoinPoint pjp) {
        ReentrantReadWriteLock lock = this.locks.get(username);

        if (lock == null) {
            ReentrantReadWriteLock cached = new ReentrantReadWriteLock();
            lock = this.locks.putIfAbsent(username, cached);

            if (lock == null) {
                lock = cached;
            }
        }

        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        final boolean writeOperation = method.isAnnotationPresent(WriteOperation.class);

        return writeOperation ? lock.writeLock() : lock.readLock();
    }

    @Pointcut("target(io.eion.security.passkeeper.service.DefaultSecureAccountService)")
    public void secureAccountService() {

    }
}
