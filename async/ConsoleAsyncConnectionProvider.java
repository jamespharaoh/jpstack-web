package wbs.console.async;

import wbs.framework.logging.TaskLogger;

public
interface ConsoleAsyncConnectionProvider {

	void sendMessage (
			TaskLogger parentTaskLogger,
			String message);

}
