package io.eion.security.passkeeper.service;

import io.eion.security.passkeeper.service.bean.SecureAccountRequest;

import java.security.KeyStore;

/**
 * @author <a href="joelin@digitalriver.com">Joe Lin</a>
 */
public interface KeystoreManager {

    KeyStore createKeyStore(SecureAccountRequest secureAccountRequest) throws Exception;

    KeyStore getKeyStore(SecureAccountRequest secureAccountRequest) throws Exception;

    void saveKeyStore(SecureAccountRequest secureAccountRequest, KeyStore keyStore) throws Exception;

    void deleteKeyStore(SecureAccountRequest secureAccountRequest) throws Exception;

    String getSecretKey(KeyStore keyStore, SecureAccountRequest secureAccountRequest) throws Exception;
}
