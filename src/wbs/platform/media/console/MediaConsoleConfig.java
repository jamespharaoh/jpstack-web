package wbs.platform.media.console;

import wbs.console.request.Cryptor;
import wbs.console.request.CryptorFactory;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;

@SingletonComponent ("mediaConsoleConfig")
public
class MediaConsoleConfig {

	// singleton dependencies

	@SingletonDependency
	CryptorFactory cryptorFactory;

	// components

	@SingletonComponent ("mediaCryptor")
	public
	Cryptor mediaCryptor () {

		return cryptorFactory.makeCryptor (
			"media");

	}

}
