package io.eion.security.passkeeper.service;

import io.eion.security.passkeeper.service.annotation.WriteOperation;
import io.eion.security.passkeeper.service.bean.SecureAccount;
import io.eion.security.passkeeper.service.bean.SecureAccountRequest;

import java.util.List;
import java.util.Optional;

/**
 * Created by vagrant on 9/13/16.
 */
public interface SecureAccountService {

    @WriteOperation
    SecureAccount createUser(String username, String masterPassword);

    @WriteOperation
    void markDeleteUser(String username, String masterPassword);

    @WriteOperation
    void deleteUser(String username, String masterPassword);

    @WriteOperation
    SecureAccount createSecureAccount(SecureAccountRequest secureAccountRequest);

    Optional<SecureAccount> getSecureAccount(SecureAccountRequest secureAccountRequest);

    List<String> getSecureAccountAliases(SecureAccountRequest secureAccountRequest);

    @WriteOperation
    SecureAccount updateSecureAccount(SecureAccountRequest secureAccountRequest);

    @WriteOperation
    Optional<SecureAccount> deleteSecureAccount(SecureAccountRequest secureAccountRequest);

}
