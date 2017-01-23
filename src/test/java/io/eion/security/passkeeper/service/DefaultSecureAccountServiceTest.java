package io.eion.security.passkeeper.service;

import io.eion.security.passkeeper.service.bean.SecureAccount;
import io.eion.security.passkeeper.service.bean.SecureAccountRequest;
import io.eion.security.passkeeper.service.exception.SecureAccountNotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Created by vagrant on 9/13/16.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DefaultSecureAccountServiceTest {

    private static final String USER_NAME = DefaultSecureAccountServiceTest.class.getSimpleName();

    private static final String MASTER_PASSWORD = "password";

    @Autowired
    private DefaultSecureAccountService secureAccountService;

    @Value("${security.keystore.location}")
    private String keystoreLocation;

    @Test
    public void testCRUD() {
        final SecureAccount defaultSecureAccount = this.secureAccountService.createUser(USER_NAME, MASTER_PASSWORD);
        assertEquals(DefaultSecureAccountService.DEFAULT_ACCOUNT, defaultSecureAccount.getAccountAlias());
        assertEquals(DefaultSecureAccountService.DEFAULT_USER_NAME, defaultSecureAccount.getAccountUsername());
        assertNotNull(defaultSecureAccount.getEncryptedPassword());
        assertEquals(DefaultSecureAccountService.DEFAULT_PASSWORD, defaultSecureAccount.getPassword());

        final String accountAlias = "newaccount";
        final String accountUsername = "a@b.email";
        final String accountPassword = "dummy";

        final SecureAccountRequest newSecureAccount = SecureAccountRequest.builder()
                .username(USER_NAME)
                .masterPassword(MASTER_PASSWORD)
                .accountAlias(accountAlias)
                .accountUsername(accountUsername)
                .password(accountPassword).build();

        final SecureAccount secureAccount = this.secureAccountService.createSecureAccount(newSecureAccount);
        assertEquals(USER_NAME, secureAccount.getUsername());
        assertEquals(accountAlias, secureAccount.getAccountAlias());
        assertEquals(accountUsername, secureAccount.getAccountUsername());
        assertEquals(accountPassword, secureAccount.getPassword());
        assertNotNull(secureAccount.getEncryptedPassword());

        final Optional<SecureAccount> nullableSecureAccount = this.secureAccountService.getSecureAccount(newSecureAccount);
        final SecureAccount retrievedSecureAccount = nullableSecureAccount.get();
        assertEquals(USER_NAME, retrievedSecureAccount.getUsername());
        assertEquals(accountAlias, retrievedSecureAccount.getAccountAlias());
        assertEquals(accountUsername, retrievedSecureAccount.getAccountUsername());
        assertNotNull(retrievedSecureAccount.getEncryptedPassword());
        assertEquals(accountPassword, retrievedSecureAccount.getPassword());

        final SecureAccountRequest updateSecureAccount = SecureAccountRequest.builder()
                .username(USER_NAME)
                .masterPassword(MASTER_PASSWORD)
                .accountAlias(accountAlias)
                .accountUsername(accountUsername)
                .password("updated").build();
        final SecureAccount updatedSecureAccount = this.secureAccountService.updateSecureAccount(updateSecureAccount);
        assertEquals("updated", updatedSecureAccount.getPassword());

        this.secureAccountService.markDeleteUser(USER_NAME, MASTER_PASSWORD);
        this.secureAccountService.deleteUser(USER_NAME, MASTER_PASSWORD);

        try {
            this.secureAccountService.getSecureAccountAliases(newSecureAccount);
            fail();
        } catch (SecureAccountNotFoundException e) {
            // expected
        }
    }
}