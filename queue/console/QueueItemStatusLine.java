package wbs.platform.queue.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.thread.ConcurrentUtils.futureValue;
import static wbs.utils.time.TimeUtils.laterThan;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

import javax.inject.Provider;

import lombok.Cleanup;
import lombok.Data;
import lombok.experimental.Accessors;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.console.part.PagePart;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.tools.EasyReadWriteLock;
import wbs.framework.component.tools.EasyReadWriteLock.HeldLock;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.queue.logic.DummyQueueCache;
import wbs.platform.queue.logic.MasterQueueCache;
import wbs.platform.queue.logic.QueueCache;
import wbs.platform.status.console.StatusLine;
import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserRec;

import wbs.utils.thread.ThreadManager;

@SingletonComponent ("queueItemStatusLine")
public
class QueueItemStatusLine
	implements StatusLine {

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

	// dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	DummyQueueCache dummyQueueCache;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	ThreadManager threadManager;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

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
	String getName () {
		return "queueItems";
	}

	@Override
	public
	PagePart get () {
		return queueItemsStatusLinePart.get ();
	}

	// life cycle

	@NormalLifecycleSetup
	public
	void setup () {

		backgroundThread =
			threadManager.startThread (
				this::backgroundLoop,
				"queue-item-status-line");

	}

	// public implementation

	@Override
	public
	Future<String> getUpdateScript () {

		@Cleanup
		HeldLock heldLock =
			lock.read ();

		Long userId =
			userConsoleLogic.userIdRequired ();

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

			return futureValue (
				stringFormat (
					"updateQueueItems (%s, %s);\n",
					integerToDecimalString (
						userData.totalAvailableItems ()),
					integerToDecimalString (
						userData.userClaimedItems ())));

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

		TaskLogger taskLogger =
			logContext.createTaskLogger (
				"updateAllUsers");

		Instant startTime =
			Instant.now ();

		// clean out old users

		Instant idleTime =
			startTime.minus (
				idleDuration);

		{

			@Cleanup
			HeldLock heldLock =
				lock.write ();

			userDatas.values ().removeIf (
				userData ->
					laterThan (
						idleTime,
						userData.lastContact ()));

		}

		// begin transaction and create cache

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				stringFormat (
					"%s.%s ()",
					getClass ().getSimpleName (),
					"getLatestData"),
				this);

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

		// tidy up

		transaction.close ();

		forceUpdate =
			false;

		lastUpdate =
			startTime;

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

}
