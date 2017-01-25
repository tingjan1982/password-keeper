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
import org.springframework.web.context.WebApplicationContext;

import java.io.File;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
    private WebApplicationContext wac;

    @Autowired
    private SecureAccountController controller;

    @Value("${security.keystore.location}")
    private String keystoreLocation;

    private MockMvc mockMvc;

    @Before
    public void prepare() throws Exception {
        //this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).alwaysDo(print()).build();
        this.mockMvc = MockMvcBuilders.standaloneSetup(this.controller)
                .setControllerAdvice(new GlobalExceptionResolver())
                .alwaysDo(print()).build();

        this.mockMvc.perform(post("/secure/users").header(SecureAccountController.MASTER_PASSWORD_HEADER_KEY, MASTER_PASSWORD).param("username", USER_NAME))
                .andExpect(status().isOk());
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
    public void authenticateUser() throws Exception {

        this.mockMvc.perform(head("/secure/users/" + USER_NAME).header(SecureAccountController.MASTER_PASSWORD_HEADER_KEY, MASTER_PASSWORD))
                .andExpect(status().isAccepted());
    }

    @Test
    public void createUser_UserAlreadyExist() throws Exception {

        this.mockMvc.perform(post("/secure/users").header(SecureAccountController.MASTER_PASSWORD_HEADER_KEY, MASTER_PASSWORD).param("username", USER_NAME))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deleteUser() throws Exception {

        this.mockMvc.perform(delete("/secure/users/" + USER_NAME).header(SecureAccountController.MASTER_PASSWORD_HEADER_KEY, MASTER_PASSWORD))
                .andExpect(status().isBadRequest());


        this.mockMvc.perform(delete("/secure/users/" + USER_NAME + "/markDelete").header(SecureAccountController.MASTER_PASSWORD_HEADER_KEY, MASTER_PASSWORD))
                .andExpect(status().isOk());

        this.mockMvc.perform(delete("/secure/users/" + USER_NAME).header(SecureAccountController.MASTER_PASSWORD_HEADER_KEY, MASTER_PASSWORD))
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/secure/users/" + USER_NAME + "/accounst").header(SecureAccountController.MASTER_PASSWORD_HEADER_KEY, MASTER_PASSWORD))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createSecureAccount() throws Exception {

        this.mockMvc.perform(post("/secure/users/" + USER_NAME + "/accounts").header(SecureAccountController.MASTER_PASSWORD_HEADER_KEY, MASTER_PASSWORD)
                .param("accountAlias", "gmail")
                .param("accountUsername", "dummy@gmail.com")
                .param("password", "dummypass"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.secureAccount.username").value(USER_NAME))
                .andExpect(jsonPath("$.secureAccount.accountAlias").value("gmail"))
                .andExpect(jsonPath("$.secureAccount.accountUsername").value("dummy@gmail.com"))
                .andExpect(jsonPath("$.secureAccount.password").value("dummypass"))
                .andExpect(jsonPath("$.secureAccount.encryptedPassword").exists());

        this.mockMvc.perform(post("/secure/users/" + USER_NAME + "/accounts").header(SecureAccountController.MASTER_PASSWORD_HEADER_KEY, MASTER_PASSWORD)
                .param("accountAlias", "gmail")
                .param("accountUsername", "dummy@gmail.com")
                .param("password", "dummypass"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getSecureAccount() throws Exception {

        this.mockMvc.perform(get("/secure/users/" + USER_NAME + "/accounts/default").header(SecureAccountController.MASTER_PASSWORD_HEADER_KEY, MASTER_PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.encryptedPassword").exists());

        this.mockMvc.perform(get("/secure/users/" + USER_NAME + "/accounts/doesnotexist").header(SecureAccountController.MASTER_PASSWORD_HEADER_KEY, MASTER_PASSWORD))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getSecureAccountAliases() throws Exception {

        this.mockMvc.perform(get("/secure/users/" + USER_NAME + "/accounts").header(SecureAccountController.MASTER_PASSWORD_HEADER_KEY, MASTER_PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$.[0]").value("default"));
    }

    @Test
    public void updateSecureAccount() throws Exception {

        this.mockMvc.perform(post("/secure/users/" + USER_NAME + "/accounts/default").header(SecureAccountController.MASTER_PASSWORD_HEADER_KEY, MASTER_PASSWORD)
                .param("accountUsername", "username")
                .param("password", "changed"))
                .andExpect(status().isOk());
    }

    @Test
    public void deleteSecureAccount() throws Exception {

        this.mockMvc.perform(delete("/secure/users/" + USER_NAME + "/accounts/default").header(SecureAccountController.MASTER_PASSWORD_HEADER_KEY, MASTER_PASSWORD))
                .andExpect(status().isOk());
    }

}