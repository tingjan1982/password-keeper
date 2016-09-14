package io.eion.security.passkeeper.service.bean;

import lombok.Data;

/**
 * Created by vagrant on 9/13/16.
 */
@Data
public class SecureAccountRequest {

    private final String username;

    private final String masterPassword;

    private final String accountAlias;
}
