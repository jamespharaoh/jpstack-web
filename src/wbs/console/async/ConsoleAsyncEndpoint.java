package wbs.console.async;

import com.google.gson.JsonObject;

import wbs.framework.logging.TaskLogger;

public
interface ConsoleAsyncEndpoint {

	String endpointPath ();

	void message (
			TaskLogger parentTaskLogger,
			ConsoleAsyncConnectionHandle connectionHandle,
			Long userId,
			JsonObject jsonObject);

}
