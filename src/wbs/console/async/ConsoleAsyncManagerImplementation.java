package wbs.console.async;

import static wbs.utils.collection.MapUtils.mapItemForKeyRequired;
import static wbs.utils.etc.Misc.contains;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.utils.random.RandomLogic;

@SingletonComponent ("consoleAsyncManager")
public
class ConsoleAsyncManagerImplementation
	implements ConsoleAsyncManager {

	// singleton dependencies

	@SingletonDependency
	Map <String, ConsoleAsyncEndpoint <?>> asyncEndpointsByComponentName;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RandomLogic randomLogic;

	// prototype depedencies

	@PrototypeDependency
	ComponentProvider <ConsoleAsyncConnection> consoleAsyncConnectionProvider;

	// state

	Map <String, ConsoleAsyncEndpoint <?>> asyncEndpointsByEndpoint;

	Map <String, ConsoleAsyncConnection> connectionsByConnectionId =
		new HashMap<> ();

	Map <Long, List <ConsoleAsyncConnection>> connectionsByUserId =
		new HashMap<> ();

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

			ImmutableMap.Builder <String, ConsoleAsyncEndpoint <?>>
				asyncEndpointsByEndpointBuilder =
					ImmutableMap.builder ();

			Set <String> asyncEndpointPaths =
				new HashSet <String> ();

			for (
				Map.Entry <String, ConsoleAsyncEndpoint <?>> asyncEndpointEntry
					: asyncEndpointsByComponentName.entrySet ()
			) {

				String componentName =
					asyncEndpointEntry.getKey ();

				ConsoleAsyncEndpoint <?> asyncEndpoint =
					asyncEndpointEntry.getValue ();

				taskLogger.debugFormat (
					"Setting up endpoint with component name: %s",
					componentName);

				String endpointPath =
					asyncEndpoint.endpointPath ();

				if (
					contains (
						asyncEndpointPaths,
						endpointPath)
				) {

					taskLogger.errorFormat (
						"Duplicated endpoint path: %s",
						endpointPath);

					continue;

				}

				asyncEndpointsByEndpointBuilder.put (
					endpointPath,
					asyncEndpoint);

				asyncEndpointPaths.add (
					endpointPath);

			}

			asyncEndpointsByEndpoint =
				asyncEndpointsByEndpointBuilder.build ();

		}

	}

	// implementation

	@Override
	public
	ConsoleAsyncConnectionListener newConnection (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ConsoleAsyncConnectionProvider connectionProvider) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"newConnection");

		) {

			ConsoleAsyncConnection connection =
				consoleAsyncConnectionProvider.provide (
					taskLogger)

				.connectionId (
					randomLogic.generateLowercase (20))

				.connectionProvider (
					connectionProvider)

			;

			connectionsByConnectionId.put (
				connection.connectionId (),
				connection);

			return connection;

		}

	}

	@Override
	public
	void closeConnection (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String connectionId) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"closeConnection");

		) {

			ConsoleAsyncConnection connection =
				mapItemForKeyRequired (
					connectionsByConnectionId,
					connectionId);

			connection.handleConnectionClosed (
				taskLogger);

			connectionsByConnectionId.remove (
				connectionId);

		}

	}

	@Override
	public
	ConsoleAsyncEndpoint <?> asyncEndpointForPathRequired (
			@NonNull String endpointPath) {

		return mapItemForKeyRequired (
			asyncEndpointsByEndpoint,
			endpointPath);

	}

}
