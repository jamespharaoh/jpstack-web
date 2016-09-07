package wbs.platform.text.console;

import javax.inject.Inject;

import wbs.console.request.Cryptor;
import wbs.console.request.CryptorFactory;
import wbs.framework.component.annotations.SingletonComponent;

@SingletonComponent ("textConsoleConfig")
public
class TextConsoleConfig {

	@Inject
	CryptorFactory cryptorFactory;

	@SingletonComponent ("textCryptor")
	public
	Cryptor textCryptor () {

		return cryptorFactory.makeCryptor (
			"text");

	}

}
