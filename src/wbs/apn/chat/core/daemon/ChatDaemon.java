package wbs.apn.chat.core.daemon;

import static wbs.framework.utils.etc.Misc.instantToDate;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.Interval;

import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.bill.model.ChatUserCreditMode;
import wbs.apn.chat.contact.logic.ChatMessageLogic;
import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.contact.model.ChatMessageMethod;
import wbs.apn.chat.contact.model.ChatMessageObjectHelper;
import wbs.apn.chat.contact.model.ChatMessageRec;
import wbs.apn.chat.contact.model.ChatMessageStatus;
import wbs.apn.chat.contact.model.ChatUserInitiationLogObjectHelper;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.core.model.ChatObjectHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.core.model.ChatStatsObjectHelper;
import wbs.apn.chat.core.model.ChatStatsRec;
import wbs.apn.chat.date.logic.ChatDateLogic;
import wbs.apn.chat.scheme.model.ChatSchemeRec;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserType;
import wbs.apn.chat.user.info.logic.ChatInfoLogic;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.platform.daemon.AbstractDaemonService;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.exception.logic.ExceptionLogic;

import com.google.common.base.Optional;

@SingletonComponent ("chatDaemon")
@Log4j
public
class ChatDaemon
	extends AbstractDaemonService {

	@Inject
	ChatCreditLogic chatCreditLogic;

	@Inject
	ChatDateLogic chatDateLogic;

	@Inject
	ChatObjectHelper chatHelper;

	@Inject
	ChatInfoLogic chatInfoLogic;

	@Inject
	ChatMessageObjectHelper chatMessageHelper;

	@Inject
	ChatMessageLogic chatMessageLogic;

	@Inject
	ChatMiscLogic chatMiscLogic;

	@Inject
	ChatSendLogic chatSendLogic;

	@Inject
	ChatStatsObjectHelper chatStatsHelper;

	@Inject
	ChatUserObjectHelper chatUserHelper;

	@Inject
	ChatUserInitiationLogObjectHelper chatUserInitiationLogHelper;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	Database database;

	@Inject
	EventLogic eventLogic;

	@Inject
	ExceptionLogic exceptionLogic;

	@Inject
	ObjectManager objectManager;

	@Inject
	Random random;

	@Getter @Setter
	int quickSleepSeconds = 10; // ten seconds

	@Getter @Setter
	int sleepSeconds = 60; // one minute

	/**
	 * Sole constructor.
	 */
	public
	ChatDaemon () {
	}

	@Override
	protected
	String getThreadName () {
		throw new UnsupportedOperationException ();
	}

	/**
	 * Starts main loop thread and returns.
	 */
	@Override
	public
	void createThreads () {

		createThread (
			"ChatA",
			new Runnable () {

			@Override
			public
			void run () {
				mainLoop ();
			}

		});

		createThread (
			"ChatB",
			new Runnable () {

			@Override
			public
			void run () {
				statsLoop ();
			}

		});

		createThread (
			"ChatD",
			new Runnable () {

			@Override
			public
			void run () {
				creditLoop ();
			}

		});

	}

	void doUser (
			int chatUserId) {

		Date timestamp =
			new Date ();

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		ChatUserRec chatUser =
			chatUserHelper.find (chatUserId);

		ChatRec chat =
			chatUser.getChat ();

		// only process online users

		if (! chatUser.getOnline ())
			return;

		// see if they need logging off after the logoff time

		if (chatUser.getDeliveryMethod () == ChatMessageMethod.sms
				&& (chatUser.getLastAction () == null
					|| chatUser.getLastAction ().getTime () + chat.getTimeLogoff () * 1000 < timestamp.getTime ())) {

			log.info (
				stringFormat (
					"Automatically logging off user %s",
					chatUser.getCode ()));

			chatMiscLogic.userLogoffWithMessage (
				chatUser,
				null,
				true);

			transaction.commit ();

			return;

		}

		// see if they need logging off after the web logoff time

		long webLogoffTime =
			+ transaction.timestamp ().getTime ()
			- chat.getTimeWebLogoff () * 1000;

		if (chatUser.getDeliveryMethod () != ChatMessageMethod.sms
				&& chatUser.getLastMessagePoll ().getTime () < webLogoffTime) {

			log.info (
				stringFormat (
					"Automatically logging off %s user %s",
					chatUser.getDeliveryMethod (),
					chatUser.getCode ()));

			chatUserLogic.logoff (
				chatUser,
				true);

			transaction.commit ();

			return;

		}

		// see if the need forcefully logging off
		/*if (!chatLogic.userSpendCheck(chatUser, false, null)) {
			logger.info("Chat user failed spend check "
					+ chatUser.getCode());
			return;
		}*/

		// --- below here is just for sms users
		if (chatUser.getDeliveryMethod () != ChatMessageMethod.sms)
			return;

		// ignore deleted users

		if (chatUser.getNumber () == null) {

			log.warn (
				stringFormat (
					"Logging off %s: no number",
					objectManager.objectPath (chatUser)));

			chatUser.setOnline (false);

			return;
		}

		// then see if they need a message from someone
		if (

			(chatUser.getLastSend () == null
				|| chatUser.getLastSend ().getTime ()
						+ chat.getTimeSend () * 1000
					< timestamp.getTime ())

			&& (chatUser.getLastReceive () == null
				|| chatUser.getLastReceive ().getTime ()
						+ chat.getTimeReceive () * 1000
					< timestamp.getTime ())

			&& (chatUser.getLastInfo () == null
				|| chatUser.getLastInfo ().getTime ()
						+ chat.getTimeInfo () * 1000
					< timestamp.getTime ())

			&& (chatUser.getLastPic () == null
				|| chatUser.getLastPic ().getTime ()
						+ chat.getTimeInfo () * 1000
					< timestamp.getTime ())

		) {

			log.info (
				stringFormat (
					"Sending info to user %s",
					objectManager.objectPathMini (
						chatUser)));

			chatInfoLogic.sendUserInfos (
				chatUser,
				1,
				null);

		}

		// then see if they need asking what their name is

		if (

			chatUser.getName () == null

			&& (chatUser.getLastNameHint () == null
				|| chatUser.getLastNameHint ().getTime ()
						+ chat.getTimeName () * 1000
					< timestamp.getTime ())

			&& chatUser.getLastJoin () != null

			&& chatUser.getLastJoin ().getTime ()
					+ chat.getTimeNameJoin () * 1000
				< timestamp.getTime ()

		) {

			log.info ("Sending name hint to user " + chatUser.getCode ());

			chatInfoLogic.sendNameHint (chatUser);

		}

		// or a dating hint
		/*
		if ((chatUser.getDateMode() == null || chatUser.getDateMode() == ChatUserDateMode.none)
				&& (chatUser.getLastDateHint() == null || chatUser
						.getLastDateHint().getTime()
						+ 7 * 24 * 60 * 60 * 1000 < timestamp.getTime())
				&& chatUser.getLastJoin() != null
				&& chatUser.getLastJoin().getTime() + 5 * 60 * 1000 < timestamp
						.getTime()) {

			logger.info ("Sending date join hint to user "
					+ chatUser.getCode());

			chatDateLogic.chatUserDateJoinHint (chatUser);

		}*/

		// see if they need a picture hint

		if (

			chatUser.getChatUserImageList ().isEmpty ()

			&& (chatUser.getLastPicHint () == null
				|| chatUser.getLastPicHint ().getTime ()
						+ chat.getTimePicHint () * 1000
					< timestamp.getTime ())

			&& chatUser.getLastJoin () != null

			&& chatUser.getLastJoin ().getTime ()
					+ 15 * 60 * 1000
				< timestamp.getTime ()

		) {

			log.info ("Sending pic hint to user " + chatUser.getCode ());

			chatInfoLogic.sendPicHint (chatUser);

		}

		// or another pic hint

		if (

			! chatUser.getChatUserImageList ().isEmpty ()

			&& (chatUser.getLastPicHint () == null
				|| chatUser.getLastPicHint ().getTime ()
						+ chat.getTimePicHint () * 1000
					< timestamp.getTime ())

			&& (chatUser.getLastPic () == null
				|| chatUser.getLastPic ().getTime ()
						+ chat.getTimePicHint () * 1000
					< timestamp.getTime ())

			&& chatUser.getLastJoin () != null

			&& chatUser.getLastJoin ().getTime ()
					+ 15 * 60 * 1000
				< timestamp.getTime ()

		) {

			log.info ("Sending pic hint 2 to user " + chatUser.getCode ());

			chatInfoLogic.sendPicHint2 (chatUser);

		}

		transaction.commit ();

	}

	private
	void doUserCredit (
			int chatUserId) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		ChatUserRec chatUser =
			chatUserHelper.find (
				chatUserId);

		chatCreditLogic.userBill (
			chatUser,
			false);

		transaction.commit ();

	}

	private
	void checkCredit () {

		log.debug (
			"Checking for all users with negative credit");

		@Cleanup
		Transaction transaction =
			database.beginReadOnly ();

		Instant threeMonthsAgo =
			transaction
				.now ()
				.minus (Duration.standardDays (90));

		log.debug (
			stringFormat (
				"Chat billing after %s",
				threeMonthsAgo));

		List<ChatUserRec> users =
			chatUserHelper.findWantingBill (
				instantToDate (
					threeMonthsAgo));

		transaction.close ();

		log.debug (
			stringFormat (
				"Chat billing after %s",
				users.size ()));

		for (ChatUserRec chatUser
				: users) {

			doUserCredit (
				chatUser.getId ());

		}

	}

	void doUserAdultExpiry (
			int chatUserId) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		ChatUserRec chatUser =
			chatUserHelper.find (
				chatUserId);

		ChatSchemeRec chatScheme =
			chatUser.getChatScheme ();

		String chatUserPath =
			objectManager.objectPath (
				chatUser,
				null,
				true);

		// make sure adult expiry is set

		if (chatUser.getAdultExpiry () == null) {

			log.warn (
				stringFormat (
					"Skipped adult expiry for %s (field is null)",
					chatUserPath));

			return;

		}

		// make sure adult expiry time is in past

		if (transaction.timestamp ().getTime ()
				< chatUser.getAdultExpiry ().getTime ()) {

			log.warn (
				stringFormat (
					"Skipped adult expiry for %s (time is in future)",
					chatUserPath));

			return;

		}

		log.info (
			stringFormat (
				"Performing adult expiry for %s (time is %s)",
				chatUserPath,
				chatUser.getAdultExpiry ()));

		// update the user

		chatUser.setAdultVerified (false);
		chatUser.setAdultExpiry (null);

		if (chatUser.getBlockAll ()) {

			log.info (
				stringFormat (
					"Not sending adult expiry message to %s due to block all",
					chatUserPath));

		} else if (chatUser.getNumber () == null) {

			log.info (
				stringFormat (
					"Not sending adult expiry message to %s due to deletion",
					chatUserPath));

		} else if (chatUser.getChatScheme () == null) {

			log.info (
				stringFormat (
					"Not sending adult expiry message to %s due to lack of scheme",
					chatUserPath));

		} else {

			// send them a message

			if (chatScheme.getRbFreeRouter () != null) {

				chatSendLogic.sendSystemRbFree (
					chatUser,
					Optional.<Integer>absent (),
					"adult_expiry",
					Collections.<String,String>emptyMap ());

			} else {

				log.warn (
					stringFormat (
						"Not sending adult expiry to %s as no route is ",
						chatUserPath,
						"configured"));

			}

		}

		transaction.commit ();

	}

	void checkAdultExpiry () {

		log.debug (
			"Checking for all users whose adult verification has expired");

		@Cleanup
		Transaction transaction =
			database.beginReadOnly ();

		List<ChatUserRec> chatUsers =
			chatUserHelper.findAdultExpiryLimit (
				1000);

		transaction.close ();

		for (ChatUserRec chatUser
				: chatUsers) {

			doUserAdultExpiry (
				chatUser.getId ());

		}

	}

	void checkOnline () {

		log.debug ("Checking online users for action needed");

		@Cleanup
		Transaction transaction =
			database.beginReadOnly ();

		List<ChatUserRec> users =
			chatUserHelper.findOnline (
				ChatUserType.user);

		transaction.close ();

		for (ChatUserRec chatUser : users)
			doUser (chatUser.getId ());

	}

	void checkSignupTimeout () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		for (ChatRec chat
				: chatHelper.findAll ()) {

			Instant createdTime =
				DateTime.now ()
					.minusSeconds (
						chat.getTimeSignupTimeout ())
					.toInstant ();

			List<ChatMessageRec> messages =
				chatMessageHelper.findSignupTimeout (
					chat,
					createdTime);

			for (ChatMessageRec message
					: messages) {

				message.setStatus (
					ChatMessageStatus.signupTimeout);

			}

		}

		transaction.commit ();

	}

	void mainLoop () {

		// sleep for a random time to stagger different threads

		try {
			Thread.sleep (sleepSeconds * random.nextInt (1000));
		} catch (InterruptedException e) {
			return;
		}

		while (true) {

			ProfileLogger prof =
				new ProfileLogger (log, "Cycle main");

			try {

				// do stuff

				prof.lap ("logoff");
				checkOnline ();

				prof.lap ("credit modes");
				checkCreditModes ();

				prof.lap ("adult expiry");
				checkAdultExpiry ();

				prof.lap ("signup timeout");
				checkSignupTimeout ();

				prof.end ();

			} catch (Exception exception) {

				exceptionLogic.logThrowable (
					"daemon",
					"Chat daemon",
					exception,
					null,
					false);

				prof.error (exception);

			}

			// then sleep for a fixed period

			try {

				Thread.sleep (sleepSeconds * 1000);

			} catch (InterruptedException e) {

				return;

			}

		}

	}

	void creditLoop () {

		// sleep for a random time to stagger different threads

		try {
			Thread.sleep (sleepSeconds * random.nextInt (1000));
		} catch (InterruptedException e) {
			return;
		}

		while (true) {

			log.debug ("Cycle credit starting");

			long startTime =
				System.currentTimeMillis ();

			long t0 = startTime, t1;

			try {

				log.info ("Cycle credit starting");
				checkCredit ();
				t1 = System.currentTimeMillis ();
				log.debug ("Cycle credit complete " + (t1 - t0) + "ms");

				long endTime = System.currentTimeMillis ();
				log.debug ("Cycle credit complete " + (endTime - startTime)
						+ "ms");

			} catch (Exception exception) {

				exceptionLogic.logThrowable (
					"daemon",
					"Chat daemon",
					exception,
					null,
					false);

				long endTime =
					System.currentTimeMillis ();

				log.error("Cycle aborted (" + (endTime - startTime) + "ms)");

			}

			// then sleep for a fixed period
			try {
				Thread.sleep(sleepSeconds * 1000);
			} catch (InterruptedException exception) {
				return;
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
			Instant timestamp) {

		// get list of chats

		@Cleanup
		Transaction transaction =
			database.beginReadOnly ();

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
			database.beginReadWrite ();

		ChatRec chat =
			chatHelper.find (chatId);

		int numUsers =
			chatUserHelper.countOnline (
				chat,
				ChatUserType.user);

		int numMonitors =
			chatUserHelper.countOnline (
				chat,
				ChatUserType.monitor);

		// insert stats

		chatStatsHelper.insert (
			new ChatStatsRec ()

			.setChat (
				chat)

			.setTimestamp (
				instantToDate (
					timestamp))

			.setNumUsers (
				numUsers)

			.setNumMonitors (
				numMonitors));

		transaction.commit ();

	}

	void statsLoop () {

		while (true) {

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

	void checkCreditModes () {

		log.info ("Checking for users to put on strict credit...");

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		for (ChatUserRec chatUser
				: chatUserHelper.findWantingStrict ()) {

			log.info (
				stringFormat (
					"Putting %s on strict credit",
					chatUser));

			chatUser.setCreditMode (
				ChatUserCreditMode.strict);

			eventLogic.createEvent (
				"chat_user_auto_strict",
				chatUser);

		}

		transaction.commit ();

	}

}
