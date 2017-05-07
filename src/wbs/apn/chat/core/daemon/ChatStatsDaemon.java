package wbs.apn.chat.core.daemon;

import static wbs.utils.collection.IterableUtils.iterableMapToList;

import java.util.List;

import lombok.NonNull;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.Interval;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.platform.daemon.AbstractDaemonService;

import wbs.apn.chat.core.model.ChatObjectHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.core.model.ChatStatsObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserType;

@SingletonComponent ("chatStatsDaemon")
public
class ChatStatsDaemon
	extends AbstractDaemonService {

	// dependencies

	@SingletonDependency
	ChatObjectHelper chatHelper;

	@SingletonDependency
	ChatStatsObjectHelper chatStatsHelper;

	@SingletonDependency
	ChatUserObjectHelper chatUserHelper;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	// details

	@Override
	protected
	String getThreadName () {

		return "ChatStatsDaemon";

	}

	// implementation

	@Override
	protected
	void runService () {

		for (;;) {

			Instant fiveMinuteTime;

			try {

				fiveMinuteTime =
					waitForFiveMinuteTime ();

			} catch (InterruptedException exception) {

				return;

			}

			try (

				OwnedTaskLogger taskLogger =
					logContext.createTaskLogger (
						"runService ()");

			) {

				taskLogger.debugFormat (
					"Doing stats");

				doStats (
					taskLogger,
					fiveMinuteTime);

			}

		}

	}

	Instant waitForFiveMinuteTime ()
			throws InterruptedException {

		DateTime startTime =
			DateTime.now ();

		Instant nextTime =
			DateTime
				.now ()
				.withMillisOfSecond (0)
				.withSecondOfMinute (0)
				.withMinuteOfHour (
					+ startTime.getMinuteOfHour ()
					- startTime.getMinuteOfHour () % 5)
				.plusMinutes (5)
				.toInstant ();

		for (;;) {

			Instant loopTime =
				Instant.now ();

			if (loopTime.isAfter (nextTime))
				return nextTime;

			Thread.sleep (
				new Interval (
					loopTime,
					nextTime
				).toDurationMillis ());

		}


	}

	void doStats (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Instant timestamp) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"doStats");

		) {

			List <Long> chatIds =
				getChatIds (
					taskLogger);

			chatIds.forEach (
				chatId ->
					doChat (
						taskLogger,
						timestamp,
						chatId));

		}

	}

	private
	List <Long> getChatIds (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnly (
					logContext,
					parentTaskLogger,
					"getChatIds");

		) {

			return iterableMapToList (
				chatHelper.findAll (
					transaction),
				ChatRec::getId);

		}

	}

	private
	void doChat (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Instant timestamp,
			@NonNull Long chatId) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"doChat");

		) {

			ChatRec chat =
				chatHelper.findRequired (
					transaction,
					chatId);

			long numUsers =
				chatUserHelper.countOnline (
					transaction,
					chat,
					ChatUserType.user);

			long numMonitors =
				chatUserHelper.countOnline (
					transaction,
					chat,
					ChatUserType.monitor);

			// insert stats

			chatStatsHelper.insert (
				transaction,
				chatStatsHelper.createInstance ()

				.setChat (
					chat)

				.setTimestamp (
					timestamp)

				.setNumUsers (
					numUsers)

				.setNumMonitors (
					numMonitors));

			transaction.commit ();

		}

	}

}
