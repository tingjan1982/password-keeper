package io.eion.security;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PasskeeperApplicationTests {

	private static final Logger logger = LoggerFactory.getLogger(PasskeeperApplicationTests.class);

	@Test
	public void contextLoads() {

	}

	@Test
	public void testSpringSecurityEnryption() {

		final String password = "I AM SHERLOCKED";
		final String salt = KeyGenerators.string().generateKey();

		TextEncryptor encryptor = Encryptors.text(password, salt);

		final String encryptedText = encryptor.encrypt("spygame");
		logger.info("Encrypted text: {}", encryptedText);

		final String decryptedText = encryptor.decrypt(encryptedText);
		assertEquals("spygame", decryptedText);
	}

}
