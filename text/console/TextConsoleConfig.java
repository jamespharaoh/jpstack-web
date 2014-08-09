package wbs.platform.text.console;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.console.request.Cryptor;

@SingletonComponent ("textConsoleConfig")
public
class TextConsoleConfig {

	@SingletonComponent ("textCryptor")
	public
	Cryptor textCryptor () {
		return new Cryptor ();
	}

}
