package wbs.platform.text.console;

import wbs.console.request.Cryptor;
import wbs.framework.application.annotations.SingletonComponent;

@SingletonComponent ("textConsoleConfig")
public
class TextConsoleConfig {

	@SingletonComponent ("textCryptor")
	public
	Cryptor textCryptor () {
		return new Cryptor ();
	}

}
