package wbs.clients.apn.chat.user.core.daemon;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;
import wbs.clients.apn.chat.bill.model.ChatUserCreditMode;
import wbs.clients.apn.chat.contact.logic.ChatMessageLogic;
import wbs.clients.apn.chat.contact.model.ChatMonitorInboxRec;
import wbs.clients.apn.chat.contact.model.ChatUserInitiationLogObjectHelper;
import wbs.clients.apn.chat.contact.model.ChatUserInitiationLogRec;
import wbs.clients.apn.chat.contact.model.ChatUserInitiationReason;
import wbs.clients.apn.chat.core.logic.ChatMiscLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.platform.daemon.SleepingDaemonService;
import wbs.platform.exception.logic.ExceptionLogLogic;

import com.google.common.base.Optional;

@Log4j
@SingletonComponent ("chatUserQuietDaemon")
public
class ChatUserQuietDaemon
	extends SleepingDaemonService {

	@Inject
	ChatMiscLogic chatLogic;

	@Inject
	ChatMessageLogic chatMessageLogic;

	@Inject
	ChatUserInitiationLogObjectHelper chatUserInitiationLogHelper;

	@Inject
	ChatUserObjectHelper chatUserHelper;

	@Inject
	Database database;

	@Inject
	ExceptionLogLogic exceptionLogic;

	@Inject
	ObjectManager objectManager;

	@Override
	protected
	String getThreadName () {
		return "ChatUserQuiet";
	}

	@Override
	protected
	int getDelayMs () {
		return 60 * 1000;
	}

	@Override
	protected
	String generalErrorSource () {
		return "chat user quiet daemon";
	}

	@Override
	protected
	String generalErrorSummary () {
		return "error checking for quiet chat users to send a message to";
	}

	@Override
	protected
	void runOnce () {

		log.debug ("Looking for quiet users");

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				this);

		// get a list of users who are past their outbound timestamp

		List<ChatUserRec> chatUsers =
			chatUserHelper.findWantingQuietOutbound ();

		transaction.close ();

		// then do each one

		for (ChatUserRec chatUser : chatUsers) {

			try {

				doUser (chatUser.getId ());

			} catch (Exception exception) {

				exceptionLogic.logThrowable (
					"daemon",
					"Chat daemon",
					exception,
					Optional.<Integer>absent (),
					false);

			}

		}

	}

	private
	void doUser (
			int chatUserId) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		// find the user

		ChatUserRec user =
			chatUserHelper.find (
				chatUserId);

		String userPath =
			objectManager.objectPath (user);

		// check and clear the outbound message flag

		if (user.getNextQuietOutbound () == null
				|| new Date ().getTime ()
					< user.getNextQuietOutbound ().getTime ())
			return;

		user.setNextQuietOutbound (null);

		// check if they have been barred

		if (user.getBarred ()) {

			log.info (
				stringFormat (
					"Skipping quiet alarm for %s: barred",
					userPath));

			transaction.commit ();

			return;
		}

		if (user.getCreditMode () == ChatUserCreditMode.barred) {

			log.info (
				stringFormat (
					"Skipping quiet alarm for %s: barred",
					userPath));

			transaction.commit ();

			return;

		}

		// check if they are a "good" user

		if (user.getCreditSuccess () < 300) {

			log.info (
				stringFormat (
					"Skipping quiet alarm for %s: low credit success",
					userPath));

			transaction.commit ();

			return;
		}

		// find a monitor

		ChatUserRec monitor =
			chatLogic.getOnlineMonitorForOutbound (
				user);

		if (monitor == null) {

			log.info (
				stringFormat (
					"Skipping quiet alarm for %s: no available monitor",
					userPath));

			transaction.commit ();

			return;
		}

		String monitorPath =
			objectManager.objectPath (
				monitor);

		// create or update the inbox

		ChatMonitorInboxRec chatMonitorInbox =
			chatMessageLogic.findOrCreateChatMonitorInbox (
				monitor,
				user,
				true);

		chatMonitorInbox.setOutbound (true);

		// create a log

		chatUserInitiationLogHelper.insert (
			new ChatUserInitiationLogRec ()

			.setChatUser (
				user)

			.setMonitorChatUser (
				monitor)

			.setReason (
				ChatUserInitiationReason.quietUser)

			.setTimestamp (
				new Date ()));

		// and return

		log.info (
			stringFormat (
				"Setting quiet alarm for %s with %s",
				userPath,
				monitorPath));

		transaction.commit ();

	}

}
