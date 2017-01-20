package io.eion.security.passkeeper.service.bean;

import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Created by vagrant on 9/13/16.
 */
@Value
@Builder
public class SecureAccount {

    private transient final String username;

    private final String accountAlias;

    private final String accountUsername;

    private final String encryptedPassword;

    private transient final String password;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("username", username)
                .append("accountAlias", accountAlias)
                .append("accountUsername", accountUsername)
                .append("encryptedPassword", encryptedPassword)
                .append("password", "<masked>")
                .toString();
    }
}
