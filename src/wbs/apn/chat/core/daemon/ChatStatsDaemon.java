package wbs.apn.chat.core.daemon;

import java.util.List;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.Interval;

import wbs.apn.chat.user.core.model.ChatUserType;
import wbs.apn.chat.core.model.ChatObjectHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.core.model.ChatStatsObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.daemon.AbstractDaemonService;

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
			Instant timestamp) {

		// get list of chats

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				"ChatStatsDaemon.doStats (timestamp)",
				this);

		List<ChatRec> chats =
			chatHelper.findAll ();

		transaction.close ();

		for (
			ChatRec chat
				: chats
		) {

			doStats (
				timestamp,
				chat.getId ());

		}

	}

	void doStats (
			@NonNull Instant timestamp,
			@NonNull Long chatId) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ChatStatsDaemon.doStats (timestamp, chatId)",
				this);

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
