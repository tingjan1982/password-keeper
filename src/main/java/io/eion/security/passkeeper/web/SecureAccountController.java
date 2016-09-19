package io.eion.security.passkeeper.web;

import io.eion.security.passkeeper.service.SecureAccountService;
import io.eion.security.passkeeper.service.bean.SecureAccount;
import io.eion.security.passkeeper.service.bean.SecureAccountRequest;
import io.eion.security.passkeeper.web.bean.SecureAccountResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Created by vagrant on 9/14/16.
 */
@RestController
@RequestMapping("/secure/users")
public class SecureAccountController {

    private static final Logger logger = LoggerFactory.getLogger(SecureAccountController.class);

    @Autowired
    private SecureAccountService secureAccountService;


    /**
     * Creates a new user.
     *
     * @param username
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<SecureAccountResponse> createUser(@RequestParam("username") final String username,
                                                            @RequestParam("masterPassword") String masterPassword) {

        final SecureAccount defaultSecureAccount = this.secureAccountService.createUser(username, masterPassword);

        final String message = "Your user is created successfully. A default account is created for you to try out secure account retrieval.";
        final SecureAccountResponse secureAccountResponse = new SecureAccountResponse(message, defaultSecureAccount);
        return ResponseEntity.ok(secureAccountResponse);
    }

    @RequestMapping(value = "/{username}/markDelete", method = RequestMethod.DELETE)
    public ResponseEntity<String> markDeleteUser(@PathVariable("username") final String username,
                                                 @RequestParam("masterPassword") String masterPassword) {

        this.secureAccountService.markDeleteUser(username, masterPassword);
        return ResponseEntity.ok("User has been marked as deleted. You need to confirm this by calling DELETE /{username}.");
    }

    /**
     * Deletes the user.
     *
     * @param username
     * @param masterPassword
     * @return
     */
    @RequestMapping(value = "/{username}", method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteUser(@PathVariable("username") final String username,
                                             @RequestParam("masterPassword") String masterPassword) {

        this.secureAccountService.deleteUser(username, masterPassword);
        return ResponseEntity.ok("User has been deleted successfully.");
    }

    /**
     * Gets SecureAccount for user identified as username.
     *
     * @param username
     * @param accountAlias
     * @param masterPassword
     * @return
     */
    @RequestMapping(value = "/{username}/accounts/{accountAlias}", method = RequestMethod.GET)
    public ResponseEntity<?> getSecureAccount(@PathVariable final String username,
                                                          @PathVariable final String accountAlias,
                                                          @RequestParam("masterPassword") String masterPassword) {

        logger.info("Username: {}, alias: {}, masterPassword: {}", username, accountAlias, "xxxxx");

        final SecureAccountRequest request = new SecureAccountRequest(username, masterPassword, accountAlias);
        final Optional<SecureAccount> secureAccount = this.secureAccountService.getSecureAccount(request);

        if (secureAccount.isPresent()) {
            return ResponseEntity.ok(secureAccount.get());
        }

        return new ResponseEntity<>("User is not found: " + username, HttpStatus.NOT_FOUND);
    }


}
