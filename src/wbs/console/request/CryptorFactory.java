package wbs.console.request;

import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringToUtf8;

import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.inject.Provider;

import lombok.NonNull;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.UninitializedDependency;
import wbs.framework.component.config.WbsConfig;

@SingletonComponent ("cryptorFactory")
public
class CryptorFactory {

	// singleton dependencies

	@SingletonDependency
	WbsConfig wbsConfig;

	// unitialized dependencies

	@UninitializedDependency
	Provider <CryptorImplementation> cryptorProvider;

	// implementation

	public
	Cryptor makeCryptor (
			@NonNull String name) {

		try {

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

			return cryptorProvider.get ()

				.secretKey (
					secretKey);

		} catch (Exception exception) {

			throw new RuntimeException (
				exception);

		}

	}

}
