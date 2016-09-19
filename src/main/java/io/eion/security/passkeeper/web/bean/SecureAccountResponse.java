package io.eion.security.passkeeper.web.bean;

import io.eion.security.passkeeper.service.bean.SecureAccount;
import lombok.Value;

/**
 * Created by vagrant on 9/19/16.
 */
@Value
public class SecureAccountResponse {

    private final String message;

    private final SecureAccount secureAccount;
}
