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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/**
 * Created by vagrant on 9/14/16.
 */
@RestController
@RequestMapping("/secure/users")
public class SecureAccountController {

    private static final Logger logger = LoggerFactory.getLogger(SecureAccountController.class);

    static final String MASTER_PASSWORD_HEADER_KEY = "x-pk-master-password";

    @Autowired
    private SecureAccountService secureAccountService;


    @RequestMapping(value = "/{username}", method = RequestMethod.HEAD)
    public ResponseEntity<Void> authenticateUser(@PathVariable final String username,
                                                 @RequestHeader(MASTER_PASSWORD_HEADER_KEY) String masterPassword) {

        final boolean authenticated = this.secureAccountService.authenticateUser(username, masterPassword);
        return authenticated ? ResponseEntity.accepted().build() : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    /**
     * Creates a new user.
     *
     * @param username
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<SecureAccountResponse> createUser(@RequestParam("username") final String username,
                                                            @RequestHeader(MASTER_PASSWORD_HEADER_KEY) String masterPassword) {

        logger.debug("Create user: {}", username);

        final SecureAccount defaultSecureAccount = this.secureAccountService.createUser(username, masterPassword);

        final String message = username + " is created successfully with a default account alias.";
        final SecureAccountResponse secureAccountResponse = new SecureAccountResponse(message, defaultSecureAccount);
        return ResponseEntity.ok(secureAccountResponse);
    }

    @RequestMapping(value = "/{username}/markDelete", method = RequestMethod.DELETE)
    public ResponseEntity<String> markDeleteUser(@PathVariable("username") final String username,
                                                 @RequestHeader(MASTER_PASSWORD_HEADER_KEY) String masterPassword) {

        logger.debug("Mark delete user: {}", username);

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
                                             @RequestHeader(MASTER_PASSWORD_HEADER_KEY) final String masterPassword) {

        logger.debug("Delete user: {}", username);

        this.secureAccountService.deleteUser(username, masterPassword);
        return ResponseEntity.ok("User has been deleted successfully.");
    }


    @RequestMapping(value = "/{username}/accounts", method = RequestMethod.POST)
    public ResponseEntity<SecureAccountResponse> createSecureAccount(@PathVariable final String username,
                                                                     @RequestHeader(MASTER_PASSWORD_HEADER_KEY) final String masterPassword,
                                                                     @RequestParam("accountAlias") final String accountAlias,
                                                                     @RequestParam("accountUsername") final String accountUsername,
                                                                     @RequestParam("password") final String passwordToEncrypt) {

        final SecureAccountRequest request = SecureAccountRequest.builder()
                .username(username)
                .masterPassword(masterPassword)
                .accountUsername(accountUsername)
                .accountAlias(accountAlias)
                .password(passwordToEncrypt).build();
        logger.debug("Create secure account: {}", request);

        final SecureAccount secureAccount = this.secureAccountService.createSecureAccount(request);

        final SecureAccountResponse secureAccountResponse = new SecureAccountResponse("Secure account is created.", secureAccount);
        return ResponseEntity.ok(secureAccountResponse);
    }


    /**
     * Gets SecureAccount for user identified as username.
     *
     * @param username
     * @param accountAlias
     * @param masterPassword
     * @return
     */
    @RequestMapping(value = "/{username}/accounts/{accountAlias:.+}", method = RequestMethod.GET)
    public ResponseEntity<?> getSecureAccount(@PathVariable final String username,
                                              @PathVariable final String accountAlias,
                                              @RequestHeader(MASTER_PASSWORD_HEADER_KEY) final String masterPassword) {

        final SecureAccountRequest request = SecureAccountRequest.builder()
                .username(username)
                .masterPassword(masterPassword)
                .accountAlias(accountAlias).build();
        logger.debug("Get secure account: {}", request);

        final Optional<SecureAccount> secureAccount = this.secureAccountService.getSecureAccount(request);

        if (secureAccount.isPresent()) {
            return ResponseEntity.ok(secureAccount.get());
        }

        return new ResponseEntity<>("Secure account is not found: " + accountAlias, HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/{username}/accounts", method = RequestMethod.GET)
    public ResponseEntity<?> getSecureAccountAliases(@PathVariable final String username,
                                                     @RequestHeader(MASTER_PASSWORD_HEADER_KEY) final String masterPassword) {

        logger.debug("Get secure account aliases: {}", username);

        final SecureAccountRequest request = SecureAccountRequest.builder()
                .username(username)
                .masterPassword(masterPassword).build();
        final List<String> secureAccountAliases = this.secureAccountService.getSecureAccountAliases(request);

        return ResponseEntity.ok(secureAccountAliases);
    }

    @RequestMapping(value = "/{username}/accounts/{accountAlias}", method = RequestMethod.POST)
    public ResponseEntity<SecureAccount> updateSecureAccount(@PathVariable final String username,
                                                             @PathVariable final String accountAlias,
                                                             @RequestHeader(MASTER_PASSWORD_HEADER_KEY) final String masterPassword,
                                                             @RequestParam("accountUsername") final String accountUsername,
                                                             @RequestParam("password") final String passwordToUpdate) {

        final SecureAccountRequest request = SecureAccountRequest.builder()
                .username(username)
                .masterPassword(masterPassword)
                .accountAlias(accountAlias)
                .accountUsername(accountUsername)
                .password(passwordToUpdate).build();
        logger.debug("Update secure account: {}", request);

        final SecureAccount secureAccount = this.secureAccountService.updateSecureAccount(request);
        return ResponseEntity.ok(secureAccount);
    }

    /**
     * Deletes SecureAccount referenced by account alias.
     *
     * @param username
     * @param accountAlias
     * @param masterPassword
     * @return
     */
    @RequestMapping(value = "/{username}/accounts/{accountAlias}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteSecureAccount(@PathVariable final String username,
                                                 @PathVariable final String accountAlias,
                                                 @RequestHeader(MASTER_PASSWORD_HEADER_KEY) final String masterPassword) {

        final SecureAccountRequest request = SecureAccountRequest.builder()
                .username(username)
                .masterPassword(masterPassword)
                .accountAlias(accountAlias).build();
        logger.debug("Delete secure account: {}", request);

        final Optional<SecureAccount> secureAccount = this.secureAccountService.deleteSecureAccount(request);

        if (secureAccount.isPresent()) {
            return ResponseEntity.ok("Secure account has been deleted: " + accountAlias);
        } else {
            return new ResponseEntity<>("Secure account is not found: " + accountAlias, HttpStatus.NOT_FOUND);
        }
    }
}
