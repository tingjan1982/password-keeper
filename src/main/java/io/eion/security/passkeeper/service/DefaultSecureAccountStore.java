package io.eion.security.passkeeper.service;

import com.google.gson.Gson;
import io.eion.security.passkeeper.service.bean.SecureAccount;
import io.eion.security.passkeeper.service.bean.SecureAccountRequest;
import io.eion.security.passkeeper.service.exception.SecureAccountException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This class is used by DefaultSecureAccountService class so make it package local.
 * <p>
 * Created by vagrant on 9/14/16.
 */
@Service
class DefaultSecureAccountStore implements SecureAccountStore {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSecureAccountStore.class);

    private static final String ACCOUNT_EXT = ".account";

    @Value("${security.keystore.location}")
    private String keystoreLocation;

    private Gson gson = new Gson();

    @Override
    public void storeSecureAccount(final SecureAccount secureAccount) throws Exception {
        Assert.notNull(secureAccount);

        final File secureAccountFile = this.createSecureAccountFile(secureAccount.getUsername());
        Map<String, String> secureAccountMap = this.loadSecureAccountMap(secureAccountFile);
        final String accountAlias = secureAccount.getAccountAlias();

        if (secureAccountMap.get(accountAlias) != null) {
            throw new Exception("Account alias already exist: " + accountAlias);
        }

        secureAccountMap.put(accountAlias, secureAccount.getEncryptedPassword());

        this.saveSecureAccountFile(secureAccountFile, secureAccountMap);
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
    public Optional<String> getSecureAccountPassword(final SecureAccountRequest secureAccountRequest) throws Exception {
        Assert.notNull(secureAccountRequest);

        final File secureAccountFile = this.createSecureAccountFile(secureAccountRequest.getUsername());
        final Map<String, String> secureAccountMap = this.loadSecureAccountMap(secureAccountFile);
        String secureAccountPassword = secureAccountMap.get(secureAccountRequest.getAccountAlias());
        return Optional.ofNullable(secureAccountPassword);
    }

    @Override
    public void deleteSecureAccountPassword(final SecureAccountRequest secureAccountRequest) throws Exception {
        Assert.notNull(secureAccountRequest);

        final File secureAccountFile = this.createSecureAccountFile(secureAccountRequest.getUsername());
        final Map<String, String> secureAccountMap = this.loadSecureAccountMap(secureAccountFile);
        final String removed = secureAccountMap.remove(secureAccountRequest.getAccountAlias());
        logger.info("Removed secure account: {}, encrypted: {}", secureAccountRequest.getAccountAlias(), removed);
        this.saveSecureAccountFile(secureAccountFile, secureAccountMap);
    }

    private Map<String, String> loadSecureAccountMap(final File secureAccountFile) throws Exception {
        Assert.notNull(secureAccountFile);
        Map<String, String> secureAccountMap = new HashMap<>();

        if (secureAccountFile.exists()) {
            try (final InputStream is = new FileSystemResource(secureAccountFile).getInputStream()) {

                final String secureAccountJSON = IOUtils.toString(is);

                if (!StringUtils.isEmpty(secureAccountJSON)) {
                    secureAccountMap = this.gson.fromJson(secureAccountJSON, Map.class);
                }
            }
        }

        return secureAccountMap;
    }

    private void saveSecureAccountFile(final File secureAccountFile, final Map<String, String> secureAccountMap) throws IOException {
        Assert.notNull(secureAccountFile);
        Assert.notNull(secureAccountMap);

        try (FileWriter fileWriter = new FileWriter(secureAccountFile)) {
            final String secureAccountJSON = this.gson.toJson(secureAccountMap);
            IOUtils.write(secureAccountJSON, fileWriter);
            fileWriter.flush();
        }
    }

    private File createSecureAccountFile(final String username) {
        Assert.notNull(username);
        return new File(this.keystoreLocation, username + ACCOUNT_EXT);
    }
}
