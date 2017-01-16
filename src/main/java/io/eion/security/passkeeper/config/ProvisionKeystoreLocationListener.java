/*
 * Copyright (C) 2018
 Digital River, Inc. All Rights Reserved.
 */
package io.eion.security.passkeeper.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author <a href="joelin@digitalriver.com">Joe Lin</a>
 */
@Component
public class ProvisionKeystoreLocationListener {

    private static final Logger logger = LoggerFactory.getLogger(ProvisionKeystoreLocationListener.class);

    @Value("${security.keystore.location}")
    private String keystoreLocation;

    @EventListener(ContextRefreshedEvent.class)
    public void provision(final ContextRefreshedEvent event) {
        logger.info("Stamped at: {}", event.getTimestamp());

        final File keystoreLocation = new File(this.keystoreLocation);

        if (!keystoreLocation.exists()) {
            logger.info("Keystore location does not exist, creating one: {}", this.keystoreLocation);
            keystoreLocation.mkdir();
        }
    }
}
