package wbs.console.async;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import com.google.common.base.Optional;
import com.google.gson.JsonObject;

import lombok.NonNull;

import wbs.framework.logging.TaskLogger;

public
interface ConsoleAsyncConnectionHandle {

	String connectionId ();

	Boolean isConnected ();
	Boolean isFresh ();

	void send (
			TaskLogger parentTaskLogger,
			JsonObject payload,
			Optional <String> messageId);

	default
	void send (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull JsonObject payload) {

		send (
			parentTaskLogger,
			payload,
			optionalAbsent ());

	}

	default
	void send (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull JsonObject payload,
			@NonNull String messageId) {

		send (
			parentTaskLogger,
			payload,
			optionalOf (
				messageId));

	}

}
