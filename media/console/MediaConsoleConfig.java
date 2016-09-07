package wbs.platform.media.console;

import javax.inject.Inject;

import wbs.console.request.Cryptor;
import wbs.console.request.CryptorFactory;
import wbs.framework.component.annotations.SingletonComponent;

@SingletonComponent ("mediaConsoleConfig")
public
class MediaConsoleConfig {

	@Inject
	CryptorFactory cryptorFactory;

	@SingletonComponent ("mediaCryptor")
	public
	Cryptor mediaCryptor () {

		return cryptorFactory.makeCryptor (
			"media");

	}

}
