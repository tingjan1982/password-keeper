package io.eion.security.passkeeper.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.eion.security.passkeeper.service.bean.SecureAccount;
import io.eion.security.passkeeper.service.bean.SecureAccountRequest;
import io.eion.security.passkeeper.service.exception.SecureAccountException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class is used by DefaultSecureAccountService class so make it package local.
 * <p>
 * Created by vagrant on 9/14/16.
 */
@Service
public class DefaultSecureAccountStore implements SecureAccountStore {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSecureAccountStore.class);

    public static final String ACCOUNT_EXT = ".account";

    @Value("${security.keystore.location}")
    private String keystoreLocation;

    @Autowired
    private Gson gson;

    /**
     * Key is the username. Value is the lock that is used to synchronize read and write operation
     * of the account file.
     */
    private ConcurrentHashMap<String, ReentrantReadWriteLock> locks = new ConcurrentHashMap<>();

    @Override
    public void storeSecureAccount(final SecureAccount secureAccount) throws Exception {
        Assert.notNull(secureAccount);

        final String username = secureAccount.getUsername();
        final File secureAccountFile = this.createSecureAccountFile(username);
        final Map<String, SecureAccount> secureAccountMap = this.loadSecureAccountMap(username, secureAccountFile);
        final String accountAlias = secureAccount.getAccountAlias();
        secureAccountMap.put(accountAlias, secureAccount);

        this.saveSecureAccountFile(username, secureAccountFile, secureAccountMap);
    }

    @Override
    public void deleteSecureAccountStore(final String username) throws Exception {
        Assert.notNull(username);

        final File secureAccountFile = this.createSecureAccountFile(username);

        if (secureAccountFile.exists()) {
            final boolean delete = secureAccountFile.delete();

            if (!delete) {
                throw new SecureAccountException("Secure account store for user is not deleted for some reason: " + username);
            }
        }
    }

    @Override
    public Optional<SecureAccount> getSecureAccount(final SecureAccountRequest secureAccountRequest) throws Exception {
        Assert.notNull(secureAccountRequest);

        final String username = secureAccountRequest.getUsername();
        final File secureAccountFile = this.createSecureAccountFile(username);
        final Map<String, SecureAccount> secureAccountMap = this.loadSecureAccountMap(username, secureAccountFile);
        final SecureAccount secureAccount = secureAccountMap.get(secureAccountRequest.getAccountAlias());
        return Optional.ofNullable(secureAccount);
    }

    @Override
    public void deleteSecureAccountPassword(final SecureAccountRequest secureAccountRequest) throws Exception {
        Assert.notNull(secureAccountRequest);

        final String username = secureAccountRequest.getUsername();
        final File secureAccountFile = this.createSecureAccountFile(username);
        final Map<String, SecureAccount> secureAccountMap = this.loadSecureAccountMap(username, secureAccountFile);
        final SecureAccount removed = secureAccountMap.remove(secureAccountRequest.getAccountAlias());
        logger.info("Removed secure account: {}", removed);

        this.saveSecureAccountFile(username, secureAccountFile, secureAccountMap);
    }

    private Map<String, SecureAccount> loadSecureAccountMap(final String username, final File secureAccountFile) throws Exception {
        Assert.notNull(secureAccountFile);
        Map<String, SecureAccount> secureAccountMap = new HashMap<>();

        if (secureAccountFile.exists()) {
            final ReentrantReadWriteLock lock = this.obtainReadWriteLock(username);
            final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
            readLock.lock();

            try (final InputStream is = new FileSystemResource(secureAccountFile).getInputStream()) {

                final String secureAccountJSON = IOUtils.toString(is);

                if (!StringUtils.isEmpty(secureAccountJSON)) {
                    Type mapType = new TypeToken<Map<String, SecureAccount>>() { }.getType();
                    secureAccountMap = this.gson.fromJson(secureAccountJSON, mapType);
                }
            }

            readLock.unlock();
        }

        return secureAccountMap;
    }

    private void saveSecureAccountFile(final String username, final File secureAccountFile, final Map<String, SecureAccount> secureAccountMap) throws IOException {
        Assert.notNull(secureAccountFile);
        Assert.notNull(secureAccountMap);

        final ReentrantReadWriteLock lock = this.obtainReadWriteLock(username);
        final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
        writeLock.lock();

        try (FileWriter fileWriter = new FileWriter(secureAccountFile)) {
            final String secureAccountJSON = this.gson.toJson(secureAccountMap);
            IOUtils.write(secureAccountJSON, fileWriter);
            fileWriter.flush();
        }

        writeLock.unlock();
    }

    private ReentrantReadWriteLock obtainReadWriteLock(final String username) {
        ReentrantReadWriteLock lock = this.locks.get(username);

        if (lock == null) {
            ReentrantReadWriteLock cached = new ReentrantReadWriteLock();
            lock = this.locks.putIfAbsent(username, cached);

            if (lock == null) {
                lock = cached;
            }
        }

        return lock;
    }

    private File createSecureAccountFile(final String username) {
        Assert.notNull(username);
        return new File(this.keystoreLocation, username + ACCOUNT_EXT);
    }
}
