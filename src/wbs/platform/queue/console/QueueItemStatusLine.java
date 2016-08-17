package wbs.platform.queue.console;

import static wbs.framework.utils.etc.ConcurrentUtils.futureValue;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.TimeUtils.laterThan;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;

import org.joda.time.Duration;
import org.joda.time.Instant;

import lombok.Cleanup;
import lombok.Data;
import lombok.experimental.Accessors;
import wbs.console.part.PagePart;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.context.EasyReadWriteLock;
import wbs.framework.application.context.EasyReadWriteLock.HeldLock;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.utils.ThreadManager;
import wbs.platform.queue.logic.DummyQueueCache;
import wbs.platform.queue.logic.MasterQueueCache;
import wbs.platform.queue.logic.QueueCache;
import wbs.platform.status.console.StatusLine;
import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserRec;

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

	@Inject
	Database database;

	@Inject
	DummyQueueCache dummyQueueCache;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	ThreadManager threadManager;

	@Inject
	UserConsoleLogic userConsoleLogic;

	@Inject
	UserConsoleHelper userHelper;

	// prototype dependencies

	@Inject
	Provider<MasterQueueCache> masterQueueCacheProvider;

	@Inject
	Provider<QueueItemsStatusLinePart> queueItemsStatusLinePart;

	@Inject
	Provider<QueueSubjectSorter> queueSubjectSorterProvider;

	// state

	EasyReadWriteLock lock =
		EasyReadWriteLock.instantiate ();

	Thread backgroundThread;

	ConcurrentMap<Long,UserData> userDatas =
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

	@PostConstruct
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
					userData.totalAvailableItems (),
					userData.userClaimedItems ()));
	
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
	
				.sort ();

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
