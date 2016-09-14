package io.eion.security.passkeeper.web;

import io.eion.security.passkeeper.service.SecureAccountService;
import io.eion.security.passkeeper.service.bean.SecureAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by vagrant on 9/14/16.
 */
@RestController
@RequestMapping("/secure/accounts")
public class SecureAccountController {

    @Autowired
    private SecureAccountService secureAccountService;


    @RequestMapping(value = "/{username}", method = RequestMethod.GET)
    public SecureAccount getSecureAccount(@PathVariable final String username) {


        return null;
    }

}
