package wbs.apn.chat.core.daemon;

import static wbs.utils.collection.IterableUtils.iterableMapToList;

import java.util.List;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.Interval;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.daemon.AbstractDaemonService;

import wbs.apn.chat.core.model.ChatObjectHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.core.model.ChatStatsObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserType;

@Log4j
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

			log.debug (
				"Doing stats");

			doStats (
				fiveMinuteTime);

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
			@NonNull Instant timestamp) {

		TaskLogger taskLogger =
			logContext.createTaskLogger (
				"doStats");

		// get list of chats

		List <Long> chatIds;

		try (

			Transaction transaction =
				database.beginReadOnly (
					"ChatStatsDaemon.doStats (timestamp)",
					this);

		) {

			chatIds =
				iterableMapToList (
					ChatRec::getId,
					chatHelper.findAll ());

			transaction.close ();

		}

		for (
			Long chatId
				: chatIds
		) {

			doStats (
				taskLogger,
				timestamp,
				chatId);

		}

	}

	void doStats (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Instant timestamp,
			@NonNull Long chatId) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"doStats");

		try (

			Transaction transaction =
				database.beginReadWrite (
					"ChatStatsDaemon.doStats (timestamp, chatId)",
					this);

		) {

			ChatRec chat =
				chatHelper.findRequired (
					chatId);

			long numUsers =
				chatUserHelper.countOnline (
					chat,
					ChatUserType.user);

			long numMonitors =
				chatUserHelper.countOnline (
					chat,
					ChatUserType.monitor);

			// insert stats

			chatStatsHelper.insert (
				taskLogger,
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
