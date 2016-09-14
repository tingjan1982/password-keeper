package io.eion.security.passkeeper.service;

import io.eion.security.passkeeper.service.bean.SecureAccount;
import io.eion.security.passkeeper.service.bean.SecureAccountRequest;
import io.eion.security.passkeeper.service.exception.SecureAccountException;
import io.eion.security.passkeeper.service.util.PasswordEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.util.Optional;

/**
 * Reference to create keystore and create secret key entry:
 * http://www.javacirecep.com/java-security/java-how-to-store-secret-keys-in-keystore/
 * <p>
 * KeyStore concept:
 * http://docs.oracle.com/javase/8/docs/api/java/security/KeyStore.html
 * <p>
 * Type of keystore to use:
 * http://stackoverflow.com/questions/11536848/keystore-type-which-one-to-use
 * <p>
 * Created by vagrant on 9/13/16.
 */
@Service
public class DefaultSecureAccountService implements SecureAccountService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSecureAccountService.class);

    private static final String KEYSTORE_EXT = ".jceks";

    @Value("${security.keystore.location}")
    private String keystoreLocation;

    @Autowired
    private SecureAccountStore secureAccountStore;

    @Autowired
    private PasswordEncryptor passwordEncryptor;


    @Override
    public SecureAccount createSecureAccount(final SecureAccountRequest secureAccountRequest, final String passwordToEncrypt) {
        Assert.notNull(secureAccountRequest);
        Assert.notNull(passwordToEncrypt);

        try {
            final String encodedSecretKey = this.loadOrCreateSecretKey(secureAccountRequest);
            logger.info("Resolved secret key: {}", encodedSecretKey);

            final String masterPassword = secureAccountRequest.getMasterPassword();
            final String encryptPassword = this.passwordEncryptor.encryptPassword(encodedSecretKey, masterPassword, passwordToEncrypt);
            logger.info("Encrypted password: {}", encryptPassword);

            final SecureAccount secureAccount = new SecureAccount(secureAccountRequest.getUsername(), secureAccountRequest.getAccountAlias(), encryptPassword, passwordToEncrypt);
            this.secureAccountStore.storeSecureAccount(secureAccount);

            return secureAccount;

        } catch (Exception e) {
            throw new SecureAccountException(e.getMessage(), e);
        }
    }

    @Override
    public Optional<SecureAccount> getSecureAccount(final SecureAccountRequest secureAccountRequest) {
        Assert.notNull(secureAccountRequest);

        try {
            final Optional<String> secureAccountPassword = this.secureAccountStore.getSecureAccountPassword(secureAccountRequest);
            SecureAccount secureAccount = null;

            if (secureAccountPassword.isPresent()) {
                final String username = secureAccountRequest.getUsername();
                final String alias = secureAccountRequest.getAccountAlias();
                final String secretKey = this.loadOrCreateSecretKey(secureAccountRequest);
                final String encryptedPassword = secureAccountPassword.get();
                final String decryptedPassword = this.passwordEncryptor.decryptPassword(secretKey, secureAccountRequest.getMasterPassword(), encryptedPassword);
                secureAccount = new SecureAccount(username, alias, encryptedPassword, decryptedPassword);
            }

            return Optional.ofNullable(secureAccount);

        } catch (Exception e) {
            throw new SecureAccountException(e.getMessage(), e);
        }
    }

    @Override
    public SecureAccount updateSecureAccount(final SecureAccountRequest secureAccountRequest, final String passwordToUpdate) {




        return null;
    }

    /**
     * Load or create a secret key for an user on a given account alias found in SecureAccountRequest.
     *
     * Reference on Base64 vs Hex encoding since we use hex encoding for secret key:
     * http://stackoverflow.com/questions/3183841/base64-vs-hex-for-sending-binary-content-over-the-internet-in-xml-doc
     *
     * @param secureAccountRequest
     * @return
     * @throws Exception
     */
    private String loadOrCreateSecretKey(final SecureAccountRequest secureAccountRequest) throws Exception {

        final KeyStore keyStore = this.loadOrCreateKeyStore(secureAccountRequest);
        final String masterPassword = secureAccountRequest.getMasterPassword();
        final KeyStore.PasswordProtection keyPassword = new KeyStore.PasswordProtection(masterPassword.toCharArray());
        final String alias = secureAccountRequest.getAccountAlias();
        KeyStore.Entry secretKeyEntry = keyStore.getEntry(alias, keyPassword);
        SecretKey secretKey;

        if (secretKeyEntry == null) {
            secretKey = KeyGenerator.getInstance("AES").generateKey();
            secretKeyEntry = new KeyStore.SecretKeyEntry(secretKey);
            keyStore.setEntry(secureAccountRequest.getAccountAlias(), secretKeyEntry, keyPassword);
            this.saveKeyStore(secureAccountRequest, keyStore);
        } else {
            secretKey = ((KeyStore.SecretKeyEntry) secretKeyEntry).getSecretKey();
        }

        return new String(Hex.encode(secretKey.getEncoded()));
    }

    private void saveKeyStore(final SecureAccountRequest secureAccountRequest, final KeyStore keyStore) throws Exception {
        Assert.notNull(secureAccountRequest);
        Assert.notNull(keyStore);

        final File keyStoreFile = this.createKeyStoreFile(secureAccountRequest);
        try (final FileOutputStream stream = new FileOutputStream(keyStoreFile)) {
            keyStore.store(stream, secureAccountRequest.getMasterPassword().toCharArray());
        }
    }

    /**
     * Load or create a keystore per user to store secret keys for each account alias.
     *
     * @param secureAccountRequest
     * @return
     */
    private KeyStore loadOrCreateKeyStore(final SecureAccountRequest secureAccountRequest) throws Exception {
        Assert.notNull(secureAccountRequest.getUsername());
        Assert.notNull(secureAccountRequest.getMasterPassword());

        final KeyStore keyStore = KeyStore.getInstance("JCEKS");
        final File keystoreFile = this.createKeyStoreFile(secureAccountRequest);
        final char[] masterPasswordArr = secureAccountRequest.getMasterPassword().toCharArray();

        if (keystoreFile.exists()) {
            try (final FileInputStream fis = new FileInputStream(keystoreFile)) {
                keyStore.load(fis, masterPasswordArr);
            }

        } else {
            try (final FileOutputStream fos = new FileOutputStream(keystoreFile)) {
                keyStore.load(null, null);
                keyStore.store(fos, masterPasswordArr);
            }
        }

        return keyStore;
    }

    private File createKeyStoreFile(final SecureAccountRequest secureAccountRequest) {
        return new File(this.keystoreLocation, secureAccountRequest.getUsername() + KEYSTORE_EXT);
    }
}
