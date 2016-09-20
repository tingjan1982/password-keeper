package io.eion.security.passkeeper.service.bean;

import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Created by vagrant on 9/13/16.
 */
@Data
public class SecureAccountRequest {

    private final String username;

    private final String masterPassword;

    private final String accountAlias;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("username", username)
                .append("masterPassword", "xxxxx")
                .append("accountAlias", accountAlias)
                .toString();
    }
}
