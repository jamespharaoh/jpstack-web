package wbs.console.async;

import static wbs.utils.time.TimeUtils.shorterThan;
import static wbs.web.utils.JsonUtils.jsonEncode;
import static wbs.web.utils.JsonUtils.jsonObjectGetInteger;
import static wbs.web.utils.JsonUtils.jsonObjectGetObject;
import static wbs.web.utils.JsonUtils.jsonObjectGetString;
import static wbs.web.utils.JsonUtils.jsonObjectParse;

import com.google.gson.JsonObject;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.console.session.UserSessionVerifyLogic;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("consoleAsyncConnection")
@Accessors (fluent = true)
public
class ConsoleAsyncConnection
	implements ConsoleAsyncConnectionListener {

	// singleton dependencies

	@SingletonDependency
	ConsoleAsyncManager consoleAsyncManager;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	UserSessionVerifyLogic userSessionVerifyLogic;

	// properties

	@Getter @Setter
	String connectionId;

	@Getter @Setter
	ConsoleAsyncConnectionProvider connectionProvider;

	// state

	Boolean connected = true;

	Instant lastMessage =
		Instant.now ();

	// implementation

	@Override
	public
	void handleMessageReceived (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String messageString) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"handleMessage");

		) {

			lastMessage =
				Instant.now ();

			try {

				JsonObject messageJson =
					jsonObjectParse (
						messageString);

				String sessionId =
					jsonObjectGetString (
						messageJson,
						"sessionId");

				Long userId =
					jsonObjectGetInteger (
						messageJson,
						"userId");

				String endpointPath =
					jsonObjectGetString (
						messageJson,
						"endpoint");

				JsonObject payload =
					jsonObjectGetObject (
						messageJson,
						"payload");

				if (
					! userSessionVerifyLogic.userSessionVerify (
						taskLogger,
						sessionId,
						userId,
						false)
				) {

					taskLogger.warningFormat (
						"Async message ignored due to authentication failure");

					return;

				}

				ConsoleAsyncEndpoint asyncEndpoint =
					consoleAsyncManager.asyncEndpointForPathRequired (
						endpointPath);

				ConnectionHandleImplementation connectionHandle =
					new ConnectionHandleImplementation ()

					.endpointPath (
						endpointPath);

				asyncEndpoint.message (
					taskLogger,
					connectionHandle,
					userId,
					payload);

			} catch (Exception exception) {

				taskLogger.errorFormatException (
					exception,
					"Error handling async message");

			}

		}

	}

	@Override
	public
	void handleConnectionClosed (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"handleConnectionClosed");

		) {

			connected = false;

		}

	}

	public
	Boolean isFresh () {

		return shorterThan (
			new Duration (
				lastMessage,
				Instant.now ()),
			freshnessDuration);

	}

	// connection handle class

	@Accessors (fluent = true)
	class ConnectionHandleImplementation
		implements ConsoleAsyncConnectionHandle {

		// properties

		@Getter @Setter
		String endpointPath;

		// implementation

		@Override
		public
		String connectionId () {
			return ConsoleAsyncConnection.this.connectionId;
		}

		@Override
		public
		Boolean isConnected () {
			return connected;
		}

		@Override
		public
		Boolean isFresh () {
			return ConsoleAsyncConnection.this.isFresh ();
		}

		@Override
		public
		void send (
				@NonNull TaskLogger parentTaskLogger,
				@NonNull JsonObject payload) {

			try (

				TaskLogger taskLogger =
					logContext.nestTaskLogger (
						parentTaskLogger,
						"send");

			) {

				JsonObject message =
					new JsonObject ();

				message.addProperty (
					"endpoint",
					endpointPath);

				message.add (
					"payload",
					payload);

				String messageJson =
					jsonEncode (
						message);

				connectionProvider.sendMessage (
					taskLogger,
					messageJson);

			}

		}

	}

	// constants

	public final static
	Duration freshnessDuration =
		Duration.standardSeconds (15l);

}
