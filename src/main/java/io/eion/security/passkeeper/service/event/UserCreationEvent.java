package io.eion.security.passkeeper.service.event;

import io.eion.security.passkeeper.service.bean.SecureAccountRequest;
import org.springframework.context.ApplicationEvent;

/**
 * @author <a href="joelin@digitalriver.com">Joe Lin</a>
 */
public class UserCreationEvent extends ApplicationEvent {

    /**
     * Create a new ApplicationEvent.
     *
     * @param userCreationRequest the object on which the event initially occurred (never {@code null})
     */
    public UserCreationEvent(final SecureAccountRequest userCreationRequest) {
        super(userCreationRequest);
    }

    public SecureAccountRequest getSecureAccountRequest() {
        return SecureAccountRequest.class.cast(this.getSource());
    }
}
