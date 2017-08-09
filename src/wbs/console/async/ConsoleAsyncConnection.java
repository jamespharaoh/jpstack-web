package wbs.console.async;

import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.TypeUtils.classEqualSafe;
import static wbs.utils.etc.TypeUtils.dynamicCastRequired;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.time.TimeUtils.shorterThan;
import static wbs.web.utils.JsonUtils.jsonEncode;
import static wbs.web.utils.JsonUtils.jsonObjectGetInteger;
import static wbs.web.utils.JsonUtils.jsonObjectGetObject;
import static wbs.web.utils.JsonUtils.jsonObjectGetString;
import static wbs.web.utils.JsonUtils.jsonObjectParse;

import com.google.common.base.Optional;
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
import wbs.framework.data.tools.DataFromJson;
import wbs.framework.data.tools.DataToJson;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
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

			OwnedTaskLogger taskLogger =
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

				String messageId =
					jsonObjectGetString (
						messageJson,
						"messageId");

				JsonObject payloadJson =
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
						"Async message authentication failure");

					sendAuthFailure (
						taskLogger);

					return;

				}

				ConsoleAsyncEndpoint <?> asyncEndpoint =
					consoleAsyncManager.asyncEndpointForPathRequired (
						endpointPath);

				Object payloadObject;

				if (
					classEqualSafe (
						asyncEndpoint.requestClass (),
						JsonObject.class)
				) {

					payloadObject =
						payloadJson;

				} else {

					DataFromJson dataFromJson =
						new DataFromJson ();

					payloadObject =
						dataFromJson.fromJson (
							asyncEndpoint.requestClass (),
							payloadJson);

				}

				ConnectionHandleImplementation connectionHandle =
					new ConnectionHandleImplementation ()

					.endpointPath (
						endpointPath);

				Optional <?> responseObjectOptional =
					asyncEndpoint.message (
						taskLogger,
						connectionHandle,
						userId,
						genericCastUnchecked (
							payloadObject));

				if (
					optionalIsPresent (
						responseObjectOptional)
				) {

					Object responseObject =
						optionalGetRequired (
							responseObjectOptional);

					JsonObject responseJson;

					if (
						classEqualSafe (
							responseObject.getClass (),
							JsonObject.class)
					) {

						responseJson =
							dynamicCastRequired (
								JsonObject.class,
								responseObject);

					} else {

						DataToJson dataToJson =
							new DataToJson ();

						responseJson =
							dynamicCastRequired (
								JsonObject.class,
								dataToJson.toJson (
									responseObject));

					}

					connectionHandle.send (
						taskLogger,
						responseJson,
						messageId);

				}

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

			OwnedTaskLogger taskLogger =
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

	// private implementation

	private
	void sendAuthFailure (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"sendAuthFailure");

		) {

			JsonObject message =
				new JsonObject ();

			message.addProperty (
				"endpoint",
				"/authentication-error");

			message.add (
				"payload",
				new JsonObject ());

			String messageJson =
				jsonEncode (
					message);

			connectionProvider.sendMessage (
				taskLogger,
				messageJson);

		}

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
				@NonNull JsonObject payload,
				@NonNull Optional <String> messageIdOptional) {

			try (

				OwnedTaskLogger taskLogger =
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

				if (
					optionalIsPresent (
						messageIdOptional)
				) {

					message.addProperty (
						"messageId",
						optionalGetRequired (
							messageIdOptional));

				}

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
