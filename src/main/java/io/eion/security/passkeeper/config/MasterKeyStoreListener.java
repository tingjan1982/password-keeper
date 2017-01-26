package io.eion.security.passkeeper.config;

import io.eion.security.passkeeper.service.SecureAccountService;
import io.eion.security.passkeeper.service.bean.SecureAccount;
import io.eion.security.passkeeper.service.bean.SecureAccountRequest;
import io.eion.security.passkeeper.service.event.UserCreationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author <a href="joelin@digitalriver.com">Joe Lin</a>
 */
@Component
public class MasterKeyStoreListener {

    private static final Logger logger = LoggerFactory.getLogger(MasterKeyStoreListener.class);

    private final SecureAccountService secureAccountService;

    @Value("${security.master.username}")
    private String username;

    @Value("${security.master.password}")
    private String password;


    public MasterKeyStoreListener(@Autowired final SecureAccountService secureAccountService) {
        this.secureAccountService = secureAccountService;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void provision(final ContextRefreshedEvent event) {

        try {
            this.secureAccountService.createUser(this.username, this.password);
        } catch (Exception e) {
            // ignored
        }
    }

    @EventListener(UserCreationEvent.class)
    public void storeUser(final UserCreationEvent userCreationEvent) {

        final SecureAccountRequest createdUserAccountRequest = userCreationEvent.getSecureAccountRequest();
        logger.info("Adding user [{}] to master", createdUserAccountRequest.getUsername());


        SecureAccountRequest masterAccountRequest = SecureAccountRequest.builder()
                .username(this.username)
                .masterPassword(this.password)
                .accountAlias(createdUserAccountRequest.getUsername())
                .accountUsername(createdUserAccountRequest.getUsername())
                .password(createdUserAccountRequest.getMasterPassword()).build();

        final SecureAccount secureAccount = this.secureAccountService.createSecureAccount(masterAccountRequest);
        logger.info("User [{}] is added to master", secureAccount.getAccountAlias());
    }
}
