package io.eion.security.passkeeper.service.util;

import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Use Spring Security Crypto module to encrypt and decrypt password. Reference:
 * http://stackoverflow.com/questions/992019/java-256-bit-aes-password-based-encryption
 *
 * Salt is a critical part of securing the password and prevent rainbow table attack:
 * http://dustwell.com/how-to-handle-passwords-bcrypt.html
 *
 * Created by vagrant on 9/13/16.
 */
@Component
public class PasswordEncryptor {

    public String encryptPassword(String salt, String masterPassword, String passwordToEncrypt) {
        Assert.notNull(salt);
        Assert.notNull(masterPassword);

        TextEncryptor encryptor = Encryptors.text(masterPassword, salt);
        return encryptor.encrypt(passwordToEncrypt);
    }

    public String decryptPassword(String salt, String masterPassword, String passwordToDecrypt) {
        Assert.notNull(salt);
        Assert.notNull(masterPassword);

        TextEncryptor decryptor = Encryptors.text(masterPassword, salt);
        return decryptor.decrypt(passwordToDecrypt);
    }
}
