package wbs.console.server;

import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;
import static wbs.utils.string.StringUtils.stringFormat;

import java.io.IOException;

import lombok.NonNull;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.glassfish.grizzly.websockets.WebSocketEngine;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.ComponentManagerShutdownBegun;
import wbs.framework.component.annotations.ComponentManagerStartupComplete;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.utils.io.RuntimeIoException;

@SingletonComponent ("consoleServer")
public
class ConsoleServer {

	// singleton components

	@SingletonDependency
	ConsoleWebSocketApplication consoleWebSocketApplication;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	WbsConfig wbsConfig;

	// state

	HttpServer httpServer;

	// life cycle

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

			// ensure console-server is not configured

			taskLogger.debugFormat (
				"Check <console-server> is configured");

			if (
				isNull (
					wbsConfig.consoleServer ())
			) {

				throw new RuntimeException (
					stringFormat (
						"No <console-server> configuration"));

			}

			// create http server

			taskLogger.debugFormat (
				"Create http server");

			httpServer =
				HttpServer.createSimpleServer (
					null,
					toJavaIntegerRequired (
						wbsConfig.consoleServer ().listenPort ()));


			// enable web sockets

			taskLogger.debugFormat (
				"Enable web sockets");

			WebSocketAddOn webSocketAddOn =
				new WebSocketAddOn ();

			httpServer.getListeners ().forEach (
				listener ->
					listener.registerAddOn (
						webSocketAddOn));

			taskLogger.debugFormat (
				"Register console web socket application");

			WebSocketEngine.getEngine ().register (
				"",
				"/_async",
				consoleWebSocketApplication);

		}

	}

	@ComponentManagerStartupComplete
	public
	void startupComplete (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"startupComplete");

		) {

			// start http server

			taskLogger.debugFormat (
				"Start http server");

			try {

				httpServer.start ();

			} catch (IOException ioException) {

				throw new RuntimeIoException (
					ioException);

			}

			taskLogger.noticeFormat (
				"Started console server on port %s",
				integerToDecimalString (
					wbsConfig.consoleServer ().listenPort ()));

		}

	}

	@ComponentManagerShutdownBegun
	public
	void tearDown (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"tearDown");

		) {

			// do nothing if http server not started

			taskLogger.debugFormat (
				"Check if http server is started");

			if (

				isNull (
					httpServer)

				|| ! httpServer.isStarted ()

			) {
				return;
			}

			// shut down

			taskLogger.debugFormat (
				"Shut down http server");

			try {

				httpServer.shutdown ().wait ();

			} catch (InterruptedException interruptedException) {

				taskLogger.warningFormat (
					"Interrupted while waiting for console server to stop");

				httpServer.shutdownNow ();

			}

			taskLogger.noticeFormat (
				"Stopped console server on port %s",
				integerToDecimalString (
					wbsConfig.consoleServer ().listenPort ()));

		}

	}

}