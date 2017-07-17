package wbs.console.request;

import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringToUtf8;

import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.StrongPrototypeDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("cryptorFactory")
public
class CryptorFactory {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	WbsConfig wbsConfig;

	// prototype dependencies

	@StrongPrototypeDependency
	ComponentProvider <CryptorImplementation> cryptorProvider;

	// implementation

	public
	Cryptor makeCryptor (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String name) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeCryptor");

		) {

			KeyGenerator keyGenerator =
				KeyGenerator.getInstance (
					"Blowfish");

			SecureRandom secureRandom =
				SecureRandom.getInstance (
					"SHA1PRNG");

			secureRandom.setSeed (
				stringToUtf8 (
					stringFormat (
						"%s/%s",
						wbsConfig.cryptorSeed (),
						name)));

			keyGenerator.init (
				128,
				secureRandom);

			SecretKey secretKey =
				keyGenerator.generateKey ();

			return cryptorProvider.provide (
				taskLogger,
				cryptor ->
					cryptor

				.secretKey (
					secretKey)

			);

		} catch (Exception exception) {

			throw new RuntimeException (
				exception);

		}

	}

}
