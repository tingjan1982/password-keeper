package io.eion.security.passkeeper.service;

import io.eion.security.passkeeper.service.bean.SecureAccount;
import io.eion.security.passkeeper.service.bean.SecureAccountRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by vagrant on 9/13/16.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DefaultSecureAccountServiceTest {

    @Autowired
    private DefaultSecureAccountService secureAccountService;

    @Value("${security.keystore.location}")
    private String keystoreLocation;

    @Test
    public void testCreateUser() throws Exception {

        final SecureAccount defaultSecureAccount = this.secureAccountService.createUser("dummy", "dummy password");

        assertEquals("default", defaultSecureAccount.getAccountAlias());
        assertNotNull(defaultSecureAccount.getEncryptedPassword());
        assertEquals("password", defaultSecureAccount.getPassword());

        this.secureAccountService.deleteUser("dummy", "dummy password");
    }

    @Test
    public void testDeleteUser() throws Exception {

        final File keystoreDirectory = new File(this.keystoreLocation);
        this.secureAccountService.createUser("dummy", "dummy password");
        final int fileCountBefore = keystoreDirectory.listFiles((dir, name) -> name.startsWith("dummy")).length;
        assertEquals(2, fileCountBefore);

        this.secureAccountService.deleteUser("dummy", "dummy password");

        final int fileCountAfter= keystoreDirectory.listFiles((dir, name) -> name.startsWith("dummy")).length;
        assertEquals(0, fileCountAfter);
    }


    @Test
    public void testCreateSecureAccount() throws Exception {

        final SecureAccountRequest secureAccountRequest = new SecureAccountRequest("joelin", "master", "gc");
        final SecureAccount secureAccount = this.secureAccountService.createSecureAccount(secureAccountRequest, "password");

        assertEquals("joelin", secureAccount.getUsername());
        assertEquals("gc", secureAccount.getAccountAlias());
        assertNotNull(secureAccount.getEncryptedPassword());
        assertEquals("password", secureAccount.getPassword());
    }

    @Test
    public void getSecureAccount() throws Exception {

        final SecureAccountRequest secureAccountRequest = new SecureAccountRequest("secure", "this is netflix", "bigblue");
        this.secureAccountService.createSecureAccount(secureAccountRequest, "1qaz2wsx");

        final Optional<SecureAccount> retrievedSecureAccountOptional = this.secureAccountService.getSecureAccount(secureAccountRequest);
        assertTrue(retrievedSecureAccountOptional.isPresent());

        final SecureAccount retrievedSecureAccount = retrievedSecureAccountOptional.get();
        assertEquals("secure", retrievedSecureAccount.getUsername());
        assertEquals("bigblue", retrievedSecureAccount.getAccountAlias());
        assertNotNull(retrievedSecureAccount.getEncryptedPassword());
        assertEquals("1qaz2wsx", retrievedSecureAccount.getPassword());
    }

    @Test
    public void updateSecureAccount() throws Exception {

    }



}