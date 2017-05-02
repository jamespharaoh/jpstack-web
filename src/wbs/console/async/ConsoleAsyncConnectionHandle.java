package wbs.console.async;

import com.google.gson.JsonObject;

import wbs.framework.database.Transaction;

public
interface ConsoleAsyncConnectionHandle {

	String connectionId ();

	Boolean isConnected ();
	Boolean isFresh ();

	void send (
			Transaction parentTransaction,
			JsonObject payload);

}
