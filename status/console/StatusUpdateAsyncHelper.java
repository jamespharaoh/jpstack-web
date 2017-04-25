package wbs.platform.status.console;

import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.presentInstances;
import static wbs.utils.string.StringUtils.emptyStringIfNull;
import static wbs.utils.string.StringUtils.joinWithSeparator;

import java.util.List;
import java.util.concurrent.Future;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import lombok.NonNull;

import org.apache.commons.lang3.tuple.Pair;

import wbs.console.async.ConsoleAsyncConnectionHandle;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.core.console.ConsoleAsyncSubscription;
import wbs.platform.deployment.logic.DeploymentLogic;
import wbs.platform.deployment.model.ConsoleDeploymentRec;
import wbs.platform.scaffold.console.RootConsoleHelper;
import wbs.platform.scaffold.model.RootRec;
import wbs.platform.user.model.UserRec;

import wbs.utils.time.TimeFormatter;

@SingletonComponent ("statusUpdateAsyncHelper")
public
class StatusUpdateAsyncHelper
	implements ConsoleAsyncSubscription.Helper <Object> {

	// singleton dependencies

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
	WbsConfig wbsConfig;

	// details

	@Override
	public
	String endpointPath () {
		return "/status/update";
	}

	@Override
	public
	String endpointName () {
		return "Status update";
	}

	// implementation

	@Override
	public
	Object newSubscription (
			@NonNull TaskLogger parentTaskLogger) {

		return new Object ();

	}

	@Override
	public
	void prepareUpdate (
			@NonNull TaskLogger parentTaskLogger) {

		doNothing ();

	}

	@Override
	public
	void updateSubscriber (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Object state,
			@NonNull ConsoleAsyncConnectionHandle connectionHandle,
			@NonNull Transaction transaction,
			@NonNull UserRec user,
			@NonNull UserPrivChecker privChecker) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"updateSubscriber");

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
				user));

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

		connectionHandle.send (
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
			@NonNull UserRec user) {

		RootRec root =
			rootHelper.findRequired (0l);

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
			timeFormatter.timestampTimezoneString (
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

}
