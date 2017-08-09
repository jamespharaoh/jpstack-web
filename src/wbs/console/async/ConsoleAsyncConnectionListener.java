package wbs.console.async;

import wbs.framework.logging.TaskLogger;

public
interface ConsoleAsyncConnectionListener {

	void handleMessageReceived (
			TaskLogger parentTaskLogger,
			String message);

	void handleConnectionClosed (
			TaskLogger parentTaskLogger);

}
