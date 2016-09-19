package io.eion.security.passkeeper.service;

import io.eion.security.passkeeper.service.bean.SecureAccount;
import io.eion.security.passkeeper.service.bean.SecureAccountRequest;

import java.util.Optional;

/**
 * Created by vagrant on 9/14/16.
 */
public interface SecureAccountStore {

    void storeSecureAccount(SecureAccount secureAccount) throws Exception;

    void deleteSecureAccountStore(String username) throws Exception;

    Optional<String> getSecureAccountPassword(SecureAccountRequest secureAccountRequest) throws Exception;
}
