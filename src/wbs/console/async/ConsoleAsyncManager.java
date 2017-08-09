package wbs.console.async;

import wbs.framework.logging.TaskLogger;

public
interface ConsoleAsyncManager {

	ConsoleAsyncConnectionListener newConnection (
			TaskLogger parentTaskLogger,
			ConsoleAsyncConnectionProvider connection);

	void closeConnection (
			TaskLogger parentTaskLogger,
			String connectionId);

	ConsoleAsyncEndpoint <?> asyncEndpointForPathRequired (
			String endpointPath);

}
