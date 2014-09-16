package wbs.apn.chat.user.core.daemon;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import lombok.Cleanup;
import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.bill.model.ChatUserCreditMode;
import wbs.apn.chat.contact.logic.ChatMessageLogic;
import wbs.apn.chat.contact.model.ChatMonitorInboxRec;
import wbs.apn.chat.contact.model.ChatUserInitiationLogObjectHelper;
import wbs.apn.chat.contact.model.ChatUserInitiationLogRec;
import wbs.apn.chat.contact.model.ChatUserInitiationReason;
import wbs.apn.chat.user.core.model.ChatUserAlarmObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserAlarmRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.daemon.SleepingDaemonService;
import wbs.platform.exception.logic.ExceptionLogic;

@SingletonComponent ("chatUserAlarmDaemon")
public
class ChatUserAlarmDaemon
	extends SleepingDaemonService {

	// dependencies

	@Inject
	ChatCreditLogic chatCreditLogic;

	@Inject
	ChatMessageLogic chatMessageLogic;

	@Inject
	ChatUserAlarmObjectHelper chatUserAlarmHelper;

	@Inject
	ChatUserInitiationLogObjectHelper chatUserInitiationLogHelper;

	@Inject
	Database database;

	@Inject
	ExceptionLogic exceptionLogic;

	// details

	@Override
	protected
	String getThreadName () {
		return "ChatUserAlarm";
	}

	@Override
	protected
	int getDelayMs () {
		return 1000 * 10;
	}

	@Override
	protected
	String generalErrorSource () {
		return "chat user alarm daemon";
	}

	@Override
	protected
	String generalErrorSummary () {
		return "error checking for alarms to trigger";
	}

	// implementation

	@Override
	protected
	void runOnce () {

		// get a list of alarms which are ready to go off

		@Cleanup
		Transaction transaction =
			database.beginReadOnly ();

		List<ChatUserAlarmRec> alarms =
			chatUserAlarmHelper.findPending ();

		transaction.close ();

		// then do each one

		for (ChatUserAlarmRec alarm
				: alarms) {

			try {

				doOneAlarm (
					alarm.getId ());

			} catch (Exception exception) {

				exceptionLogic.logThrowable (
					"daemon",
					stringFormat (
						"chatUserAlarm %s",
						alarm.getId ()),
					exception,
					null,
					false);

			}

		}

	}

	void doOneAlarm (
			int alarmId) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		// find the alarm and stuff

		ChatUserAlarmRec alarm =
			chatUserAlarmHelper.find (
				alarmId);

		ChatUserRec user =
			alarm.getChatUser ();

		ChatUserRec monitor =
			alarm.getMonitorChatUser ();

		// delete the alarm

		chatUserAlarmHelper.remove (
			alarm);

		// check whether to ignore this alarm

		boolean ignore =

			user.getBarred ()

			|| user.getCreditMode () == ChatUserCreditMode.barred

			|| user.getBlockAll ()

			|| ! chatCreditLogic.userSpendCheck (
				user,
				false,
				null,
				false);

		ChatUserInitiationReason reason =
			ignore
				? ChatUserInitiationReason.alarmIgnore
				: ChatUserInitiationReason.alarm;

		// create or update the cmi

		if (! ignore) {

			ChatMonitorInboxRec chatMonitorInbox =
				chatMessageLogic.findOrCreateChatMonitorInbox (
					monitor,
					user,
					true);

			chatMonitorInbox
				.setOutbound (true);

		}

		// create a log

		chatUserInitiationLogHelper.insert (
			new ChatUserInitiationLogRec ()

			.setChatUser (
				user)

			.setMonitorChatUser (
				monitor)

			.setReason (
				reason)

			.setTimestamp (
				new Date ())

			.setAlarmTime (
				alarm.getAlarmTime ()));

		// and commit

		transaction.commit ();

	}

}
