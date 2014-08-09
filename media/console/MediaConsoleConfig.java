package wbs.platform.media.console;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.console.request.Cryptor;

@SingletonComponent ("mediaConsoleConfig")
public
class MediaConsoleConfig {

	@SingletonComponent ("mediaCryptor")
	public
	Cryptor mediaCryptor () {
		return new Cryptor ();
	}

}
