package io.eion.security.passkeeper.service;

import io.eion.security.passkeeper.service.bean.SecureAccount;
import io.eion.security.passkeeper.service.bean.SecureAccountRequest;

import java.util.Optional;

/**
 * Created by vagrant on 9/13/16.
 */
public interface SecureAccountService {

    SecureAccount createSecureAccount(SecureAccountRequest secureAccountRequest, String passwordToEncrypt);

    Optional<SecureAccount> getSecureAccount(SecureAccountRequest secureAccountRequest);

    SecureAccount updateSecureAccount(SecureAccountRequest secureAccountRequest, String passwordToUpdate);

}
