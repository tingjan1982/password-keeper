package io.eion.security.passkeeper.service.bean;

import lombok.Value;

/**
 * Created by vagrant on 9/13/16.
 */
@Value
public class SecureAccount {

    private final String username;

    private final String accountAlias;

    private final String encryptedPassword;

    private final String password;
}
