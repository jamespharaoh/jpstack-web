package wbs.platform.media.console;

import wbs.console.request.Cryptor;
import wbs.framework.application.annotations.SingletonComponent;

@SingletonComponent ("mediaConsoleConfig")
public
class MediaConsoleConfig {

	@SingletonComponent ("mediaCryptor")
	public
	Cryptor mediaCryptor () {
		return new Cryptor ();
	}

}
