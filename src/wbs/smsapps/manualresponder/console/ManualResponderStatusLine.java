package wbs.smsapps.manualresponder.console;

import static wbs.utils.collection.CacheUtils.cacheGet;
import static wbs.utils.thread.ConcurrentUtils.futureValue;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.inject.Provider;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.JsonObject;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import org.joda.time.Duration;

import wbs.console.feature.FeatureChecker;
import wbs.console.part.PagePart;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.status.console.StatusLine;
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

	LoadingCache <Long, PerOperatorCaches> cachesByUserId =
		CacheBuilder.newBuilder ()

		.maximumSize (
			maxCacheSize)

		.expireAfterAccess (
			cacheExpiryTime.getMillis (),
			TimeUnit.MILLISECONDS)

		.build (
			new CacheLoader <Long, PerOperatorCaches> () {

			@Override
			public
			PerOperatorCaches load (
					@NonNull Long userId) {

				return new PerOperatorCaches ()

					.numThisHourCache (
						manualResponderNumThisHourCacheProvider.get ()
							.userId (userId))

					.numTodayCache (
						manualResponderNumTodayCacheProvider.get ()
							.userId (userId))

				;

			}

		});

	// details

	@Override
	public
	String typeName () {
		return "manual-responder";
	}

	// implementation

	@Override
	public
	PagePart createPagePart (
			@NonNull Transaction parentTransaction) {

		return manualResponderStatusLinePartProvider.get ();

	}

	@Override
	public
	Future <JsonObject> getUpdateData (
			@NonNull Transaction parentTransaction,
			@NonNull UserPrivChecker privChecker) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getUpdateData");

		) {

			JsonObject updateData =
				new JsonObject ();

			if (
				! featureChecker.checkFeatureAccess (
					transaction,
					privChecker,
					"queue_items_status_line")
			) {

				updateData.addProperty (
					"numToday",
					0);

				updateData.addProperty (
					"numThisHour",
					0);

				return futureValue (
					updateData);

			}

			// find or create per-operator caches

			PerOperatorCaches caches =
				cacheGet (
					cachesByUserId,
					privChecker.userIdRequired ());

			if (caches == null) {

				caches =
					new PerOperatorCaches ();

				cachesByUserId.put (
					privChecker.userIdRequired (),
					caches);

			}

			// return update script

			updateData.addProperty (
				"numToday",
				caches.numTodayCache.get (
					transaction));

			updateData.addProperty (
				"numThisHour",
				caches.numThisHourCache.get (
					transaction));

			return futureValue (
				updateData);

		}

	}

	// helpers

	@Accessors (fluent = true)
	@Data
	class PerOperatorCaches {
		ManualResponderNumThisHourCache numThisHourCache;
		ManualResponderNumTodayCache numTodayCache;
	}

	// constants

	public final static
	Long maxCacheSize = 1024l;

	public final static
	Duration cacheExpiryTime =
		Duration.standardHours (1l);

}
