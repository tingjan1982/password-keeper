package io.eion.security.passkeeper.service;

import io.eion.security.passkeeper.service.bean.SecureAccount;
import io.eion.security.passkeeper.service.bean.SecureAccountRequest;
import io.eion.security.passkeeper.service.event.UserCreationEvent;
import io.eion.security.passkeeper.service.exception.SecureAccountException;
import io.eion.security.passkeeper.service.util.PasswordEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
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

    static final String DEFAULT_ACCOUNT = "default";

    static final String DEFAULT_USER_NAME = "username";

    static final String DEFAULT_PASSWORD = "password";

    @Value("${security.delete.delay}")
    private int deleteDelay;

    @Autowired
    private KeystoreManager keystoreManager;

    @Autowired
    private SecureAccountStore secureAccountStore;

    @Autowired
    private PasswordEncryptor passwordEncryptor;

    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    private ApplicationEventPublisher publisher;

    private ConcurrentHashMap<String, ScheduledFuture> usersMarkDeleted = new ConcurrentHashMap<>();


    @Override
    public boolean authenticateUser(final String username, final String masterPassword) {

        final SecureAccountRequest secureAccountRequest = SecureAccountRequest.builder()
                .username(username)
                .masterPassword(masterPassword).build();

        try {
            this.keystoreManager.getKeyStore(secureAccountRequest);
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public SecureAccount createUser(final String username, final String masterPassword) {

        final SecureAccountRequest secureAccountRequest = SecureAccountRequest.builder()
                .username(username)
                .masterPassword(masterPassword)
                .accountUsername(DEFAULT_USER_NAME)
                .accountAlias(DEFAULT_ACCOUNT)
                .password(DEFAULT_PASSWORD).build();
        try {
            final KeyStore keyStore = this.keystoreManager.createKeyStore(secureAccountRequest);
            this.addToMaster(secureAccountRequest);
            return this.createSecureAccount(secureAccountRequest);

        } catch (Exception e) {
            if (e instanceof SecureAccountException) {
                throw SecureAccountException.class.cast(e);
            }

            final String errorMsg = "Unexpected error while creating key store: " + e.getMessage();
            logger.error(errorMsg, e);
            throw new SecureAccountException(errorMsg, e);
        }
    }

    private void addToMaster(final SecureAccountRequest secureAccountRequest) {
        Assert.notNull(secureAccountRequest);

        this.publisher.publishEvent(new UserCreationEvent(secureAccountRequest));
    }

    /**
     * Marks the user as deleted before the actual user deletion can happen.
     *
     * @param username
     * @param masterPassword
     */
    @Override
    public void markDeleteUser(final String username, final String masterPassword) {

        if (this.usersMarkDeleted.containsKey(username)) {
            return;
        }

        final long delay = TimeUnit.SECONDS.toMillis(this.deleteDelay);
        final PeriodicTrigger trigger = new PeriodicTrigger(delay);
        trigger.setInitialDelay(delay);

        final ScheduledFuture<?> future = this.taskScheduler.schedule(() -> {
            logger.info("Remove user from the mark deletion list: {}", username);
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

        try {
            final SecureAccountRequest secureAccountRequest = SecureAccountRequest.builder()
                    .username(username)
                    .masterPassword(masterPassword).build();
            this.keystoreManager.deleteKeyStore(secureAccountRequest);
            this.secureAccountStore.deleteSecureAccountStore(username);

        } catch (Exception e) {
            logger.error("Unexpected error while deleting user: " + e.getMessage(), e);
            throw new SecureAccountException(e.getMessage(), e);
        }
    }

    @Override
    public SecureAccount createSecureAccount(final SecureAccountRequest secureAccountRequest) {
        Assert.notNull(secureAccountRequest);

        try {
            final Optional<SecureAccount> nullableSecureAccount = this.secureAccountStore.getSecureAccount(secureAccountRequest);
            nullableSecureAccount.ifPresent(secureAccount -> {
                throw new SecureAccountException("Secure account already exists: " + secureAccountRequest.getAccountAlias());
            });

            return this.updateSecureAccount(secureAccountRequest);

        } catch (Exception e) {
            if (e instanceof SecureAccountException) {
                throw SecureAccountException.class.cast(e);
            }

            throw new SecureAccountException("Unexpected error while creating account: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<SecureAccount> getSecureAccount(final SecureAccountRequest secureAccountRequest) {
        Assert.notNull(secureAccountRequest);

        try {
            final Optional<SecureAccount> nullableSecureAccount = this.secureAccountStore.getSecureAccount(secureAccountRequest);
            SecureAccount secureAccount = null;

            if (nullableSecureAccount.isPresent()) {
                final SecureAccount retrievedSecureAccount = nullableSecureAccount.get();

                final String username = secureAccountRequest.getUsername();
                final KeyStore keyStore = this.keystoreManager.getKeyStore(secureAccountRequest);
                final String secretKey = this.keystoreManager.getSecretKey(keyStore, secureAccountRequest);

                final String encryptedPassword = retrievedSecureAccount.getEncryptedPassword();
                final String decryptedPassword = this.passwordEncryptor.decryptPassword(secretKey, secureAccountRequest.getMasterPassword(), encryptedPassword);
                secureAccount = SecureAccount.builder()
                        .username(username)
                        .accountAlias(retrievedSecureAccount.getAccountAlias())
                        .accountUsername(retrievedSecureAccount.getAccountUsername())
                        .encryptedPassword(encryptedPassword)
                        .password(decryptedPassword).build();
            }

            return Optional.ofNullable(secureAccount);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new SecureAccountException(e.getMessage(), e);
        }
    }

    @Override
    public List<String> getSecureAccountAliases(final SecureAccountRequest secureAccountRequest) {

        final KeyStore keyStore;
        try {
            keyStore = this.keystoreManager.getKeyStore(secureAccountRequest);
            final List<String> aliasList = new ArrayList<>();
            final Enumeration<String> aliases = keyStore.aliases();

            while (aliases.hasMoreElements()) {
                aliasList.add(aliases.nextElement());
            }

            return aliasList;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);

            if (SecureAccountException.class.isAssignableFrom(e.getClass())) {
                throw SecureAccountException.class.cast(e);
            }

            throw new SecureAccountException(e.getMessage(), e);
        }
    }

    @Override
    public SecureAccount updateSecureAccount(final SecureAccountRequest secureAccountRequest) {

        try {
            final KeyStore keyStore = this.keystoreManager.getKeyStore(secureAccountRequest);
            final String encodedSecretKey = this.keystoreManager.getSecretKey(keyStore, secureAccountRequest);
            logger.info("Resolved secret key: {}", encodedSecretKey);

            final String masterPassword = secureAccountRequest.getMasterPassword();
            final String password = secureAccountRequest.getPassword();
            final String encryptPassword = this.passwordEncryptor.encryptPassword(encodedSecretKey, masterPassword, password);
            logger.info("Encrypted password: {}", encryptPassword);

            final SecureAccount secureAccount = SecureAccount.builder()
                    .username(secureAccountRequest.getUsername())
                    .accountAlias(secureAccountRequest.getAccountAlias())
                    .accountUsername(secureAccountRequest.getAccountUsername())
                    .encryptedPassword(encryptPassword)
                    .password(password).build();
            this.secureAccountStore.storeSecureAccount(secureAccount);

            return secureAccount;

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new SecureAccountException(e.getMessage(), e);
        }
    }

    @Override
    public Optional<SecureAccount> deleteSecureAccount(final SecureAccountRequest secureAccountRequest) {
        Assert.notNull(secureAccountRequest);

        final Optional<SecureAccount> secureAccount = this.getSecureAccount(secureAccountRequest);

        if (secureAccount.isPresent()) {
            try {
                final KeyStore keyStore = this.keystoreManager.getKeyStore(secureAccountRequest);
                keyStore.deleteEntry(secureAccountRequest.getAccountAlias());
                this.keystoreManager.saveKeyStore(secureAccountRequest, keyStore);
                this.secureAccountStore.deleteSecureAccountPassword(secureAccountRequest);

            } catch (Exception e) {
                final String errorMsg = "Error while trying to delete secure account: " + e.getMessage();
                logger.error(errorMsg, e);
                throw new SecureAccountException(errorMsg, e);
            }
        }

        return secureAccount;
    }
}
