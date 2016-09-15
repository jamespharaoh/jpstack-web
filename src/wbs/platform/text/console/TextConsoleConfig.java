package wbs.platform.text.console;

import wbs.console.request.Cryptor;
import wbs.console.request.CryptorFactory;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;

@SingletonComponent ("textConsoleConfig")
public
class TextConsoleConfig {

	@SingletonDependency
	CryptorFactory cryptorFactory;

	@SingletonComponent ("textCryptor")
	public
	Cryptor textCryptor () {

		return cryptorFactory.makeCryptor (
			"text");

	}

}
