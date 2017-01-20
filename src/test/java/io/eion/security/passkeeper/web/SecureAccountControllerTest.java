package io.eion.security.passkeeper.web;

import io.eion.security.passkeeper.service.DefaultKeystoreManager;
import io.eion.security.passkeeper.service.DefaultSecureAccountStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.File;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author <a href="joelin@digitalriver.com">Joe Lin</a>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class SecureAccountControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(SecureAccountControllerTest.class);

    private static final String USER_NAME = "test-user";

    private static final String MASTER_PASSWORD = "master";

    @Autowired
    private SecureAccountController controller;

    @Value("${security.keystore.location}")
    private String keystoreLocation;

    private MockMvc mockMvc;

    @Before
    public void prepare() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(this.controller)
                .alwaysDo(print()).build();
    }

    @After
    public void after() {

        final File keystore = new File(this.keystoreLocation, USER_NAME + DefaultKeystoreManager.KEYSTORE_EXT);
        logger.debug("{}", keystore.exists());
        keystore.delete();
        final File account = new File(this.keystoreLocation, USER_NAME + DefaultSecureAccountStore.ACCOUNT_EXT);
        logger.debug("{}", account.exists());
        account.delete();

    }

    @Test
    public void createUser() throws Exception {

        this.mockMvc.perform(post("/secure/users").header(SecureAccountController.MASTER_PASSWORD_HEADER_KEY, MASTER_PASSWORD).param("username", USER_NAME))
                .andExpect(status().isOk());
    }

    @Test
    public void markDeleteUser() throws Exception {

    }

    @Test
    public void deleteUser() throws Exception {

    }

    @Test
    public void createSecureAccount() throws Exception {

    }

    @Test
    public void getSecureAccount() throws Exception {

    }

    @Test
    public void getSecureAccountAliases() throws Exception {

    }

    @Test
    public void updateSecureAccount() throws Exception {

    }

    @Test
    public void deleteSecureAccount() throws Exception {

    }

}