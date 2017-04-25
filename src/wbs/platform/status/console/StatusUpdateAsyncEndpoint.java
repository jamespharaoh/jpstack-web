package wbs.platform.status.console;

import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.collection.MapUtils.mapContainsKey;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.presentInstances;
import static wbs.utils.string.StringUtils.emptyStringIfNull;
import static wbs.utils.string.StringUtils.joinWithSeparator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import javax.inject.Provider;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.tuple.Pair;

import wbs.console.async.ConsoleAsyncConnectionHandle;
import wbs.console.async.ConsoleAsyncEndpoint;
import wbs.console.priv.UserPrivChecker;
import wbs.console.priv.UserPrivCheckerBuilder;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.NormalLifecycleTeardown;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.deployment.logic.DeploymentLogic;
import wbs.platform.deployment.model.ConsoleDeploymentRec;
import wbs.platform.scaffold.console.RootConsoleHelper;
import wbs.platform.scaffold.model.RootRec;
import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.model.UserRec;

import wbs.utils.time.TimeFormatter;

@SingletonComponent ("statusUpdateAsyncEndpoint")
public
class StatusUpdateAsyncEndpoint
	implements ConsoleAsyncEndpoint {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	DeploymentLogic deploymentLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RootConsoleHelper rootHelper;

	@SingletonDependency
	StatusLineManager statusLineManager;

	@SingletonDependency
	TimeFormatter timeFormatter;

	@SingletonDependency
	UserConsoleHelper userHelper;

	@SingletonDependency
	WbsConfig wbsConfig;

	// prototype dependencies

	@PrototypeDependency
	Provider <UserPrivCheckerBuilder> userPrivCheckerBuilderProvider;

	// state

	Thread backgroundThread;

	Map <String, Subscriber> subscribersByConnectionId =
		new HashMap<> ();

	// details

	@Override
	public
	String endpointPath () {
		return "/status/update";
	}

	// life cycle

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"setup");

		taskLogger.noticeFormat (
			"Status update async endpoint starting");

		backgroundThread =
			new Thread (
				this::backgroundThread);

		backgroundThread.start ();

	}

	@NormalLifecycleTeardown
	public
	void tearDown (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"tearDown");

		if (
			isNull (
				backgroundThread)
		) {
			return;
		}

		taskLogger.noticeFormat (
			"Status update async endpoint shutting down");

		backgroundThread.interrupt ();

		try {

			backgroundThread.wait ();

		} catch (InterruptedException interruptedException) {

			taskLogger.fatalFormat (
				"Interrupted while waiting for shutdown");

		}

	}

	// implementation

	@Override
	public
	void message (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ConsoleAsyncConnectionHandle connectionHandle,
			@NonNull Long userId,
			@NonNull JsonObject jsonObject) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"message");

		synchronized (this) {

			if (
				mapContainsKey (
					subscribersByConnectionId,
					connectionHandle.connectionId ())
			) {

				taskLogger.errorFormat (
					"Duplicate subscription for connection id: %s",
					connectionHandle.connectionId ());

				return;

			}

			subscribersByConnectionId.put (
				connectionHandle.connectionId (),
				new Subscriber ()

				.connectionHandle (
					connectionHandle)

				.userId (
					userId)

			);

		}

	}

	// private implementation

	private
	void backgroundThread () {

		for (;;) {

			try {

				sendUpdates ();

				Thread.sleep (
					1000);

			} catch (InterruptedException interruptedException) {
				return;
			}

		}

	}

	private
	void sendUpdates () {

		TaskLogger taskLogger =
			logContext.createTaskLogger (
				"sendUpdates");

		synchronized (this) {

			Set <String> closedConnectionIds =
				new HashSet<> ();

			try (

				Transaction transaction =
					database.beginReadOnly (
						taskLogger,
						"sendUpdates",
						this);

			) {

				for (
					Map.Entry <String, Subscriber> subscriberEntry
						: subscribersByConnectionId.entrySet ()
				) {

					String connectionId =
						subscriberEntry.getKey ();

					Subscriber subscriber =
						subscriberEntry.getValue ();

					if (! subscriber.connectionHandle ().isConnected ()) {

						closedConnectionIds.add (
							connectionId);

						continue;

					}

					if (! subscriber.connectionHandle ().isFresh ()) {

						continue;

					}

					updateSubscriber (
						taskLogger,
						transaction,
						subscriber);

				}

			}

			closedConnectionIds.forEach (
				subscribersByConnectionId::remove);

		}

	}

	private
	void updateSubscriber (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Transaction transaction,
			@NonNull Subscriber subscriber) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"updateSubscriber");

		UserPrivChecker privChecker =
			userPrivCheckerBuilderProvider.get ()

			.userId (
				subscriber.userId ())

			.build (
				taskLogger);

		List <Pair <String, Future <JsonObject>>> futures =
			iterableMapToList (
				statusLine ->
					Pair.of (
						statusLine.typeName (),
						statusLine.getUpdateData (
							taskLogger,
							privChecker)),
				statusLineManager.getStatusLines ());

		JsonObject payload =
			new JsonObject ();

		JsonArray updates =
			new JsonArray ();

		addUpdate (
			updates,
			"core",
			buildUpdateCore (
				taskLogger,
				transaction,
				subscriber));

		for (
			Pair <String, Future <JsonObject>> future
				: futures
		) {

			try {

				addUpdate (
					updates,
					future.getKey (),
					future.getValue ().get ());

			} catch (Exception exception) {

				taskLogger.errorFormatException (
					exception,
					"Error getting status update for %s",
					future.getKey ());

			}

		}

		payload.add (
			"updates",
			updates);

		subscriber.connectionHandle ().send (
			taskLogger,
			payload);

	}

	private
	void addUpdate (
			@NonNull JsonArray updates,
			@NonNull String updateType,
			@NonNull JsonObject updateData) {

		JsonObject update =
			new JsonObject ();

		update.addProperty (
			"type",
			updateType);

		update.add (
			"data",
			updateData);

		updates.add (
			update);

	}

	private
	JsonObject buildUpdateCore (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Transaction transaction,
			@NonNull Subscriber subscriber) {

		RootRec root =
			rootHelper.findRequired (0l);

		UserRec user =
			userHelper.findRequired (
				subscriber.userId ());

		ConsoleDeploymentRec consoleDeployment =
			deploymentLogic.thisConsoleDeployment ();

		JsonObject update =
			new JsonObject ();

		update.addProperty (
			"header",
			joinWithSeparator (
				" – ",
				presentInstances (
					optionalOf (
						"Status"),
					optionalFromNullable (
						consoleDeployment.getStatusLabel ()),
						optionalOf (
						deploymentLogic.gitVersion ()))));

		update.addProperty (
			"timestamp",
			timeFormatter.timestampString (
				timeFormatter.timezone (
					ifNull (
						user.getDefaultTimezone (),
						user.getSlice ().getDefaultTimezone (),
						wbsConfig.defaultTimezone ())),
				transaction.now ()));

		update.addProperty (
			"notice",
			emptyStringIfNull (
				root.getNotice ()));

		return update;

	}

	// subscriber class

	@Accessors (fluent = true)
	@Data
	static
	class Subscriber {
		ConsoleAsyncConnectionHandle connectionHandle;
		Long userId;
	}

}
