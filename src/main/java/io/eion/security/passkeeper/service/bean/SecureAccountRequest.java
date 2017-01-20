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
public class SecureAccountRequest {

    private final String username;

    private final String masterPassword;

    private final String accountAlias;

    private final String accountUsername;

    private final String password;


    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("username", username)
                .append("masterPassword", "<masked>")
                .append("accountAlias", accountAlias)
                .append("accountUsername", accountUsername)
                .append("password", "<masked")
                .toString();
    }
}
