package wbs.console.request;

import wbs.framework.logging.TaskLogger;

public
interface Cryptor {

	String encryptInteger (
			TaskLogger parentTaskLogger,
			Long input);

	Long decryptInteger (
			TaskLogger parentTaskLogger,
			String input);

}
