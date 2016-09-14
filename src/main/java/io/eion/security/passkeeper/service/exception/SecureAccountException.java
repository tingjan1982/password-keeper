package io.eion.security.passkeeper.service.exception;

import org.springframework.core.NestedRuntimeException;

/**
 * Created by vagrant on 9/14/16.
 */
public class SecureAccountException extends NestedRuntimeException {


    public SecureAccountException(final String msg) {
        super(msg);
    }

    public SecureAccountException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
