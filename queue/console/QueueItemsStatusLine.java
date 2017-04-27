package wbs.platform.queue.console;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.ThreadUtils.threadInterruptAndJoinIgnoreInterrupt;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.thread.ConcurrentUtils.futureValue;
import static wbs.utils.time.TimeUtils.laterThan;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

import javax.inject.Provider;

import com.google.gson.JsonObject;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.console.part.PagePart;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.NormalLifecycleTeardown;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.tools.EasyReadWriteLock;
import wbs.framework.component.tools.EasyReadWriteLock.HeldLock;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.queue.logic.DummyQueueCache;
import wbs.platform.queue.logic.MasterQueueCache;
import wbs.platform.queue.logic.QueueCache;
import wbs.platform.status.console.StatusLine;
import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.model.UserRec;

import wbs.utils.thread.ThreadManager;

@SingletonComponent ("queueItemStatusLine")
public
class QueueItemsStatusLine
	implements StatusLine {

	// dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	DummyQueueCache dummyQueueCache;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ThreadManager threadManager;

	@SingletonDependency
	UserConsoleHelper userHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <MasterQueueCache> masterQueueCacheProvider;

	@PrototypeDependency
	Provider <QueueItemsStatusLinePart> queueItemsStatusLinePart;

	@PrototypeDependency
	Provider <QueueSubjectSorter> queueSubjectSorterProvider;

	// state

	EasyReadWriteLock lock =
		EasyReadWriteLock.instantiate ();

	Thread backgroundThread;

	ConcurrentMap <Long, UserData> userDatas =
		new ConcurrentHashMap<> ();

	Instant lastUpdate;

	volatile
	boolean forceUpdate =
		true;

	// details

	@Override
	public
	String typeName () {
		return "queue-items";
	}

	@Override
	public
	PagePart createPagePart (
			@NonNull TaskLogger parentTaskLogger) {

		return queueItemsStatusLinePart.get ();

	}

	// life cycle

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		backgroundThread =
			threadManager.startThread (
				this::backgroundLoop,
				"queue-item-status-line");

	}

	@NormalLifecycleTeardown
	public
	void teardown (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"teardown");

		) {

			if (
				isNotNull (
					backgroundThread)
			) {

				taskLogger.noticeFormat (
					"Stopping queue item background thread");

				threadInterruptAndJoinIgnoreInterrupt (
					backgroundThread);

			}

		}

	}

	// public implementation

	@Override
	public
	Future <JsonObject> getUpdateData (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull UserPrivChecker privChecker) {

		try (

			HeldLock heldLock =
				lock.read ();

		) {

			Long userId =
				privChecker.userIdRequired ();

			UserData userData =
				userDatas.computeIfAbsent (
					userId,
					_userId -> {

				forceUpdate =
					true;

				return new UserData ()

					.userId (
						userId)

					.totalAvailableItems (
						0l)

					.userClaimedItems (
						0l);

			});

			synchronized (userData) {

				userData

					.lastContact (
						Instant.now ());

				JsonObject updateData =
					new JsonObject ();

				updateData.addProperty (
					"total",
					userData.totalAvailableItems ());

				updateData.addProperty (
					"claimed",
					userData.userClaimedItems ());

				return futureValue (
					updateData);

			}

		}

	}

	// private implementation

	private
	void backgroundLoop () {

		for (;;) {

			while (
				! needToUpdate ()
			) {

				try {

					Thread.sleep (
						sleepDuration.getMillis ());

				} catch (InterruptedException interruptedException) {
					return;
				}

			}

			updateAllUsers ();

		}

	}

	private
	boolean needToUpdate () {

		return (

			forceUpdate

			|| laterThan (
				Instant.now (),
				lastUpdate.plus (
					updateDuration))

		);

	}

	private
	void updateAllUsers () {

		try (

			TaskLogger taskLogger =
				logContext.createTaskLogger (
					"updateAllUsers");

		) {

			Instant startTime =
				Instant.now ();

			// clean out old users

			Instant idleTime =
				startTime.minus (
					idleDuration);

			try (

				HeldLock heldLock =
					lock.write ();

			) {

				userDatas.values ().removeIf (
					userData ->
						laterThan (
							idleTime,
							userData.lastContact ()));

			}

			// begin transaction and create cache

			try (

				OwnedTransaction transaction =
					database.beginReadOnly (
						taskLogger,
						stringFormat (
							"%s.%s ()",
							getClass ().getSimpleName (),
							"getLatestData"),
						this);

			) {

				QueueCache queueCache =
					masterQueueCacheProvider.get ();

				// update users

				userDatas.values ().forEach (
					userData -> {

					UserRec user =
						userHelper.findRequired (
							userData.userId ());

					SortedQueueSubjects sortedSubjects =
						queueSubjectSorterProvider.get ()

						.queueCache (
							queueCache)

						.loggedInUser (
							user)

						.effectiveUser (
							user)

						.sort (
							taskLogger);

					synchronized (userData) {

						userData

							.totalAvailableItems (
								sortedSubjects.totalAvailableItems ())

							.userClaimedItems (
								sortedSubjects.userClaimedItems ());

					}

				});

			}

			// tidy up

			forceUpdate =
				false;

			lastUpdate =
				startTime;

		}

	}

	@Accessors (fluent = true)
	@Data
	static
	class UserData {

		Long userId;

		Instant lastContact;

		Long totalAvailableItems;
		Long userClaimedItems;

	}

	// constants

	public final static
	Duration sleepDuration =
		Duration.standardSeconds (
			1);

	public final static
	Duration updateDuration =
		Duration.standardSeconds (
			5);

	public final static
	Duration idleDuration =
		Duration.standardMinutes (
			5);

}
