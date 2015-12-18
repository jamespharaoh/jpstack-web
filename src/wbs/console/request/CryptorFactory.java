package wbs.console.request;

import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.stringToUnicodeBytes;

import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.inject.Inject;
import javax.inject.Provider;

import lombok.NonNull;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.config.WbsConfig;

@SingletonComponent ("cryptorFactory")
public
class CryptorFactory {

	// dependencies

	@Inject
	WbsConfig wbsConfig;

	// prototype dependencies

	@Inject
	Provider<CryptorImplementation> cryptorProvider;

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
				stringToUnicodeBytes (
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
