package io.eion.security.passkeeper.service.bean;

import lombok.Value;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Created by vagrant on 9/13/16.
 */
@Value
public class SecureAccount {

    private final String username;

    private final String accountAlias;

    private final String encryptedPassword;

    private final String password;

    public SecureAccount(final String username, final String accountAlias, final String encryptedPassword, final String password) {
        this.username = username;
        this.accountAlias = accountAlias;
        this.encryptedPassword = encryptedPassword;
        this.password = password;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("username", username)
                .append("accountAlias", accountAlias)
                .append("encryptedPassword", encryptedPassword)
                .append("password", "xxxxx")
                .toString();
    }
}
