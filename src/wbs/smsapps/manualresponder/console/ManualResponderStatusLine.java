package wbs.smsapps.manualresponder.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.thread.ConcurrentUtils.futureValue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import javax.inject.Provider;

import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.console.feature.FeatureChecker;
import wbs.console.part.PagePart;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.status.console.StatusLine;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;

import wbs.smsapps.manualresponder.model.ManualResponderRequestObjectHelper;

@SingletonComponent ("manualResponderStatusLine")
public
class ManualResponderStatusLine
	implements StatusLine {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	FeatureChecker featureChecker;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ManualResponderRequestObjectHelper manualResponderRequestHelper;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserObjectHelper userHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <ManualResponderNumThisHourCache>
	manualResponderNumThisHourCacheProvider;

	@PrototypeDependency
	Provider <ManualResponderNumTodayCache>
	manualResponderNumTodayCacheProvider;

	@PrototypeDependency
	Provider <ManualResponderStatusLinePart>
	manualResponderStatusLinePartProvider;

	// state

	Map <Long, PerOperatorCaches> cachesByUserId =
		new HashMap<> ();

	// details

	@Override
	public
	String getName () {
		return "manualResponder";
	}

	// implementation

	@Override
	public
	PagePart get (
			@NonNull TaskLogger parentTaskLogger) {

		return manualResponderStatusLinePartProvider.get ();

	}

	@Override
	public
	Future <String> getUpdateScript (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"getUpdateScript");

		if (
			! featureChecker.checkFeatureAccess (
				taskLogger,
				"queue_items_status_line")
		) {

			return futureValue (
				"updateManualResponder (0, 0);\n");

		}

		// find or create per-operator caches

		PerOperatorCaches caches =
			cachesByUserId.get (
				userConsoleLogic.userIdRequired ());

		if (caches == null) {

			caches =
				new PerOperatorCaches ();

			cachesByUserId.put (
				userConsoleLogic.userIdRequired (),
				caches);

		}

		// return update script

		return futureValue (
			stringFormat (
				"updateManualResponder (%s, %s);\n",
				integerToDecimalString (
					caches.numTodayCache.get (
						taskLogger)),
				integerToDecimalString (
					caches.numThisHourCache.get (
						taskLogger))));

	}

	// helpers

	@Accessors (fluent = true)
	class PerOperatorCaches {

		ManualResponderNumThisHourCache numThisHourCache =
			manualResponderNumThisHourCacheProvider.get ();

		ManualResponderNumTodayCache numTodayCache =
			manualResponderNumTodayCacheProvider.get ();

	}

}
