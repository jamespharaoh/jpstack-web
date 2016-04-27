package wbs.clients.apn.chat.core.daemon;

import java.util.List;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.Interval;

import wbs.clients.apn.chat.core.model.ChatObjectHelper;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.core.model.ChatStatsObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserType;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.daemon.AbstractDaemonService;

@Log4j
@SingletonComponent ("chatStatsDaemon")
public
class ChatStatsDaemon
	extends AbstractDaemonService {

	// dependencies

	@Inject
	ChatObjectHelper chatHelper;

	@Inject
	ChatStatsObjectHelper chatStatsHelper;

	@Inject
	ChatUserObjectHelper chatUserHelper;

	@Inject
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
				this);

		List<ChatRec> chats =
			chatHelper.findAll ();

		transaction.close ();

		for (ChatRec chat
				: chats) {

			doStats (
				timestamp,
				chat.getId ());

		}

	}

	void doStats (
			Instant timestamp,
			int chatId) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		ChatRec chat =
			chatHelper.find (
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
