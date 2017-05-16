package wbs.framework.processapi;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;
import static wbs.utils.etc.NullUtils.isNull;

import java.io.IOException;

import lombok.NonNull;

import org.glassfish.grizzly.http.server.HttpServer;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.NormalLifecycleTeardown;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.utils.io.RuntimeIoException;

@SingletonComponent ("processApiServer")
public
class ProcessApiServer {

	// singleton components

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ProcessApiStatusHandler processApiStatusHandler;

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

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

			// do nothing if process-api not configured

			if (
				isNull (
					wbsConfig.processApi ())
			) {
				return;
			}

			// create http server

			httpServer =
				HttpServer.createSimpleServer (
					null,
					toJavaIntegerRequired (
						wbsConfig.processApi ().listenPort ()));

			httpServer.getServerConfiguration ().addHttpHandler (
				processApiStatusHandler,
				"/status");

			// start http server

			try {

				httpServer.start ();

			} catch (IOException ioException) {

				throw new RuntimeIoException (
					ioException);

			}

			taskLogger.noticeFormat (
				"Started process API server on port %s",
				integerToDecimalString (
					wbsConfig.processApi ().listenPort ()));

		}

	}

	@NormalLifecycleTeardown
	public
	void tearDown (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"tearDown");

		) {

			// do nothing if http server not started

			if (

				isNull (
					httpServer)

				|| ! httpServer.isStarted ()

			) {
				return;
			}

			// shut down

			try {

				httpServer.shutdown ().wait ();

			} catch (InterruptedException interruptedException) {

				taskLogger.warningFormat (
					"Interrupted while waiting for process API server to stop");

				httpServer.shutdownNow ();

			}

			taskLogger.noticeFormat (
				"Stopped process API server on port %s",
				integerToDecimalString (
					wbsConfig.processApi ().listenPort ()));

		}

	}

}
