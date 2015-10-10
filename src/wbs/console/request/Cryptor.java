package wbs.console.request;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import lombok.extern.log4j.Log4j;

@Log4j
public
class Cryptor {

	private
	SecretKey key;

	public
	Cryptor () {

		try {

			KeyGenerator keyGenerator =
				KeyGenerator.getInstance ("Blowfish");

			keyGenerator.init (128);

			key =
				keyGenerator.generateKey ();

		} catch (Exception exception) {

			throw new RuntimeException (
				exception);

		}

	}

	public
	String encryptInt (
			int input) {

		try {

			byte[] clearText = new byte[] {
				(byte) ((input & 0xff000000) >> 24),
				(byte) ((input & 0x00ff0000) >> 16),
				(byte) ((input & 0x0000ff00) >> 8),
				(byte) ((input & 0x000000ff) >> 0)
			};

			Cipher cipher =
				Cipher.getInstance ("Blowfish");

			cipher.init
				(Cipher.ENCRYPT_MODE,
				key);

			String ret =
				toHex (
					cipher.doFinal (
						clearText));

			log.debug ("encrypted " + input + " as " + ret);

			return ret;

		} catch (Exception exception) {

			throw new RuntimeException(exception);

		}

	}

	public
	int decryptInt (
			String input) {

		try {

			byte[] cipherText =
				fromHex (input);

			Cipher cipher =
				Cipher.getInstance ("Blowfish");

			cipher.init (
				Cipher.DECRYPT_MODE,
				key);

			byte[] clearText =
				cipher.doFinal (cipherText);

			int ret =
				((clearText[0] & 0xff) << 24)
				| ((clearText[1] & 0xff) << 16)
				| ((clearText[2] & 0xff) << 8)
				| ((clearText[3] & 0xff) << 0);

			log.debug ("decrypted " + input + " as " + ret);

			return ret;

		} catch (Exception exception) {

			throw new RuntimeException (exception);

		}

	}

	public
	String toHex (
			byte[] bytes) {

		StringBuffer stringBuilder =
			new StringBuffer ();

		for (byte b : bytes) {

			stringBuilder.append (
				String.format (
					"%02x",
					b));

		}

		return stringBuilder.toString ();

	}

	public
	byte[] fromHex (
			String hex) {

		if ((hex.length () & 1) == 1)
			throw new IllegalArgumentException ();

		byte[] ret =
			new byte [hex.length () >> 1];

		for (int i = 0; i < ret.length; i++) {

			int j =
				i << 1;

			ret [i] =
				(byte) (
					(decodeHexNibble(hex.charAt (j)) << 4)
					| decodeHexNibble (hex.charAt (j + 1))
				);

		}

		return ret;

	}

	public static
	int decodeHexNibble (
			char ch) {

		switch (ch) {

		case '0':
			return 0;

		case '1':
			return 1;

		case '2':
			return 2;

		case '3':
			return 3;

		case '4':
			return 4;

		case '5':
			return 5;

		case '6':
			return 6;

		case '7':
			return 7;

		case '8':
			return 8;

		case '9':
			return 9;

		case 'a':
		case 'A':
			return 10;

		case 'b':
		case 'B':
			return 11;

		case 'c':
		case 'C':
			return 12;

		case 'd':
		case 'D':
			return 13;

		case 'e':
		case 'E':
			return 14;

		case 'f':
		case 'F':
			return 15;

		}

		throw new IllegalArgumentException ();

	}

}
