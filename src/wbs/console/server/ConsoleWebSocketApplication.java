package wbs.console.server;

import lombok.NonNull;

import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.DataFrame;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.SimpleWebSocket;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.WebSocketListener;

import wbs.console.async.ConsoleAsyncConnectionListener;
import wbs.console.async.ConsoleAsyncConnectionProvider;
import wbs.console.async.ConsoleAsyncManager;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("consoleWebSocketApplication")
public
class ConsoleWebSocketApplication
	extends WebSocketApplication {

	// singleton dependencies

	@SingletonDependency
	ConsoleAsyncManager consoleAsyncManager;

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	WebSocket createSocket (
			@NonNull ProtocolHandler handler,
			@NonNull HttpRequestPacket requestPacket,
			@NonNull WebSocketListener ... listeners) {

		return new WebSocketImplementation (
			handler,
			requestPacket,
			listeners);

	}

	// web socket implementation

	class WebSocketImplementation
		extends SimpleWebSocket
		implements ConsoleAsyncConnectionProvider {

		// state

		final
		HttpRequestPacket connectionRequestPacket;

		ConsoleAsyncConnectionListener connectionListener;

		// constructor

		public
		WebSocketImplementation (
				@NonNull ProtocolHandler protocolHandler,
				@NonNull HttpRequestPacket connectionRequestPacket,
				@NonNull WebSocketListener[] listeners) {

			super (
				protocolHandler,
				listeners);

			this.connectionRequestPacket =
				connectionRequestPacket;

		}

		@Override
		public
		void onClose (
				@NonNull DataFrame frame) {

			TaskLogger taskLogger =
				logContext.createTaskLogger (
					"WebSocketImplementation.onClose");

			super.onClose (
				frame);

			connectionListener.handleConnectionClosed (
				taskLogger);

			taskLogger.makeException ();

		}

		@Override
		public
		void onConnect () {

			TaskLogger taskLogger =
				logContext.createTaskLogger (
					"WebSocketImplementation.onMessage");

			super.onConnect ();

			connectionListener =
				consoleAsyncManager.newConnection (
					taskLogger,
					this);

			taskLogger.makeException ();

		}

		@Override
		public
		void onMessage (
				@NonNull String message) {

			TaskLogger taskLogger =
				logContext.createTaskLogger (
					"WebSocketImplementation.onMessage");

			super.onMessage (
				message);

			connectionListener.handleMessageReceived (
				taskLogger,
				message);

			taskLogger.makeException ();

		}

		@Override
		public
		void sendMessage (
				@NonNull TaskLogger parentTaskLogger,
				@NonNull String message) {

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"sendMessage");

			try {

				send (
					message);

			} catch (Exception exception) {

				taskLogger.errorFormatException (
					exception,
					"Error sending message: %s",
					message);

			}

		}

	}

}
