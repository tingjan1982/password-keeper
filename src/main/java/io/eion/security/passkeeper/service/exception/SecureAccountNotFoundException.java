package io.eion.security.passkeeper.service.exception;

/**
 * @author <a href="joelin@digitalriver.com">Joe Lin</a>
 */
public class SecureAccountNotFoundException extends SecureAccountException {

    public SecureAccountNotFoundException(final String msg) {
        super(msg);
    }
}
