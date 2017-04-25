package wbs.console.async;

import com.google.gson.JsonObject;

import wbs.framework.logging.TaskLogger;

public
interface ConsoleAsyncConnectionHandle {

	String connectionId ();

	Boolean isConnected ();
	Boolean isFresh ();

	void send (
			TaskLogger parentTaskLogger,
			JsonObject payload);

}
