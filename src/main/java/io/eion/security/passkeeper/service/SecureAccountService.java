package io.eion.security.passkeeper.service;

import io.eion.security.passkeeper.service.bean.SecureAccount;
import io.eion.security.passkeeper.service.bean.SecureAccountRequest;

import java.util.List;
import java.util.Optional;

/**
 * Created by vagrant on 9/13/16.
 */
public interface SecureAccountService {

    SecureAccount createUser(String username, String masterPassword);

    void markDeleteUser(String username, String masterPassword);

    void deleteUser(String username, String masterPassword);

    SecureAccount createSecureAccount(SecureAccountRequest secureAccountRequest);

    Optional<SecureAccount> getSecureAccount(SecureAccountRequest secureAccountRequest);

    List<String> getSecureAccountAliases(SecureAccountRequest secureAccountRequest);

    SecureAccount updateSecureAccount(SecureAccountRequest secureAccountRequest);

    Optional<SecureAccount> deleteSecureAccount(SecureAccountRequest secureAccountRequest);

}
