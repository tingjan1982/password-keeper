package io.eion.security.passkeeper.service;

import io.eion.security.passkeeper.service.bean.SecureAccountRequest;
import io.eion.security.passkeeper.service.exception.SecureAccountException;
import io.eion.security.passkeeper.service.exception.SecureAccountNotFoundException;
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

/**
 * @author <a href="joelin@digitalriver.com">Joe Lin</a>
 */
@Service
public class DefaultKeystoreManager implements KeystoreManager {

    private static final String KEYSTORE_EXT = ".jceks";

    @Value("${security.keystore.location}")
    private String keystoreLocation;


    /**
     * Creates KeyStore using username as the file name and master password as the store password.
     *
     * @param secureAccountRequest
     * @return
     * @throws Exception
     */
    @Override
    public KeyStore createKeyStore(final SecureAccountRequest secureAccountRequest) throws Exception {
        Assert.notNull(secureAccountRequest);
        Assert.notNull(secureAccountRequest.getUsername());
        Assert.notNull(secureAccountRequest.getMasterPassword());

        final String username = secureAccountRequest.getUsername();
        final File keyStoreFile = this.createKeyStoreFile(username);

        if (keyStoreFile.exists()) {
            throw new SecureAccountException("User already exists: " + username);
        }

        final KeyStore keyStore = KeyStore.getInstance("JCEKS");
        final char[] masterPasswordArr = secureAccountRequest.getMasterPassword().toCharArray();
        try (final FileOutputStream fos = new FileOutputStream(keyStoreFile)) {
            keyStore.load(null, null);
            keyStore.store(fos, masterPasswordArr);
        }

        return keyStore;
    }

    /**
     * Loads KeyStore using username and master password.
     *
     * @param secureAccountRequest
     * @return
     * @throws Exception
     */
    @Override
    public KeyStore getKeyStore(final SecureAccountRequest secureAccountRequest) throws Exception {
        Assert.notNull(secureAccountRequest);
        Assert.notNull(secureAccountRequest.getUsername());
        Assert.notNull(secureAccountRequest.getMasterPassword());

        final String username = secureAccountRequest.getUsername();
        final File keyStoreFile = this.createKeyStoreFile(username);

        if (!keyStoreFile.exists()) {
            throw new SecureAccountNotFoundException("User does not exist: " + username);
        }

        final KeyStore keyStore = KeyStore.getInstance("JCEKS");
        final char[] masterPasswordArr = secureAccountRequest.getMasterPassword().toCharArray();
        try (final FileInputStream fis = new FileInputStream(keyStoreFile)) {
            keyStore.load(fis, masterPasswordArr);
        }

        return keyStore;
    }

    @Override
    public void saveKeyStore(final SecureAccountRequest secureAccountRequest, final KeyStore keyStore) throws Exception {
        Assert.notNull(secureAccountRequest);
        Assert.notNull(secureAccountRequest.getUsername());
        Assert.notNull(keyStore);

        final File keyStoreFile = this.createKeyStoreFile(secureAccountRequest.getUsername());
        try (final FileOutputStream stream = new FileOutputStream(keyStoreFile)) {
            keyStore.store(stream, secureAccountRequest.getMasterPassword().toCharArray());
        }
    }

    @Override
    public void deleteKeyStore(final SecureAccountRequest secureAccountRequest) throws Exception {

        this.getKeyStore(secureAccountRequest);
        final String username = secureAccountRequest.getUsername();
        final File keyStoreFile = this.createKeyStoreFile(username);
        final boolean deleted = keyStoreFile.delete();

        if (!deleted) {
            throw new SecureAccountException("Delete user failed: " + username);
        }
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
    @Override
    public String getSecretKey(final KeyStore keyStore, final SecureAccountRequest secureAccountRequest) throws Exception {

        Assert.notNull(keyStore);
        Assert.notNull(secureAccountRequest);

        final String masterPassword = secureAccountRequest.getMasterPassword();
        final KeyStore.PasswordProtection keyPassword = new KeyStore.PasswordProtection(masterPassword.toCharArray());
        final String accountAlias = secureAccountRequest.getAccountAlias();
        KeyStore.Entry secretKeyEntry = keyStore.getEntry(accountAlias, keyPassword);
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

    private File createKeyStoreFile(final String username) {
        Assert.notNull(username);
        return new File(this.keystoreLocation, username + KEYSTORE_EXT);
    }
}
