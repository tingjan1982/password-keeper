package io.eion.security.passkeeper.service;

import io.eion.security.passkeeper.service.bean.SecureAccount;
import io.eion.security.passkeeper.service.bean.SecureAccountRequest;
import io.eion.security.passkeeper.service.exception.SecureAccountException;
import io.eion.security.passkeeper.service.util.PasswordEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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

    @Value("${security.delete.delay}")
    private int deleteDelay;

    @Autowired
    private SecureAccountStore secureAccountStore;

    @Autowired
    private PasswordEncryptor passwordEncryptor;

    @Autowired
    private TaskScheduler taskScheduler;

    private ConcurrentHashMap<String, ScheduledFuture> usersMarkDeleted = new ConcurrentHashMap<>();


    @Override
    public SecureAccount createUser(final String username, final String masterPassword) {

        final SecureAccountRequest secureAccountRequest = new SecureAccountRequest(username, masterPassword, "default");
        return this.createSecureAccount(secureAccountRequest, "password");
    }

    /**
     * Marks the user as deleted before the actual user deletion can happen.
     *
     * @param username
     * @param masterPassword
     */
    @Override
    public void markDeleteUser(final String username, final String masterPassword) {

        this.authenticateUser(username, masterPassword);
        final long delay = TimeUnit.SECONDS.toMillis(this.deleteDelay);
        final PeriodicTrigger trigger = new PeriodicTrigger(delay);
        trigger.setInitialDelay(delay);

        final ScheduledFuture<?> future = this.taskScheduler.schedule(() -> {
            logger.info("Remove user from the mark deletion list");
            final ScheduledFuture retrievedFuture = this.usersMarkDeleted.remove(username);

            if (retrievedFuture != null) {
                retrievedFuture.cancel(true);
            }

        }, trigger);

        this.usersMarkDeleted.putIfAbsent(username, future);
    }

    /**
     * Deletes the user by first check if keystore exists, and verify the password by
     * loading the store with the master password.
     *
     * @param username
     * @param masterPassword
     */
    @Override
    public void deleteUser(final String username, final String masterPassword) {

        final ScheduledFuture userFuture = this.usersMarkDeleted.get(username);

        if (userFuture == null) {
            throw new SecureAccountException("You need to mark delete this user before you can actually delete the user: " + username);
        }

        final File keyStoreFile = this.createKeyStoreFile(username);
        this.authenticateUser(username, masterPassword);
        final boolean deleted = keyStoreFile.delete();

        if (!deleted) {
            throw new SecureAccountException("Delete user failed: " + username);
        }

        try {
            this.secureAccountStore.deleteSecureAccountStore(username);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new SecureAccountException(e.getMessage(), e);
        }
    }

    public void authenticateUser(final String username, final String masterPassword) {
        final File keyStoreFile = this.createKeyStoreFile(username);

        if (keyStoreFile.exists()) {
            final SecureAccountRequest secureAccountRequest = new SecureAccountRequest(username, masterPassword, null);
            try {
                this.loadOrCreateKeyStore(secureAccountRequest);
            } catch (Exception e) {
                throw new SecureAccountException("User is not authenticated: " + e.getMessage(), e);
            }
        } else {
            throw new SecureAccountException("User does not exist: " + username);
        }
    }

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
            logger.error(e.getMessage(), e);
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
            logger.error(e.getMessage(), e);
            throw new SecureAccountException(e.getMessage(), e);
        }
    }

    @Override
    public SecureAccount updateSecureAccount(final SecureAccountRequest secureAccountRequest, final String passwordToUpdate) {
        return null;
    }

    @Override
    public Optional<SecureAccount> deleteSecureAccount(final SecureAccountRequest secureAccountRequest) {
        return null;
    }

    /**
     * Load or create a secret key for an user on a given account alias found in SecureAccountRequest.
     * <p>
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
        Assert.notNull(secureAccountRequest.getUsername());
        Assert.notNull(keyStore);

        final File keyStoreFile = this.createKeyStoreFile(secureAccountRequest.getUsername());
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
        final File keystoreFile = this.createKeyStoreFile(secureAccountRequest.getUsername());
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

    private File createKeyStoreFile(final String username) {
        Assert.notNull(username);
        return new File(this.keystoreLocation, username + KEYSTORE_EXT);
    }
}
