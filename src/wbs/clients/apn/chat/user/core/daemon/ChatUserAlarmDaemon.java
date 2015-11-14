package wbs.clients.apn.chat.user.core.daemon;

import static wbs.framework.utils.etc.Misc.instantToDate;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;

import lombok.Cleanup;

import com.google.common.base.Optional;

import wbs.clients.apn.chat.bill.logic.ChatCreditCheckResult;
import wbs.clients.apn.chat.bill.logic.ChatCreditLogic;
import wbs.clients.apn.chat.contact.logic.ChatMessageLogic;
import wbs.clients.apn.chat.contact.model.ChatMonitorInboxRec;
import wbs.clients.apn.chat.contact.model.ChatUserInitiationLogObjectHelper;
import wbs.clients.apn.chat.contact.model.ChatUserInitiationReason;
import wbs.clients.apn.chat.user.core.model.ChatUserAlarmObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserAlarmRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.daemon.SleepingDaemonService;
import wbs.platform.exception.logic.ExceptionLogger;
import wbs.platform.exception.model.ExceptionResolution;

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
	ExceptionLogger exceptionLogger;

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
			database.beginReadOnly (
				this);

		List<ChatUserAlarmRec> alarms =
			chatUserAlarmHelper.findPending (
				transaction.now ());

		transaction.close ();

		// then do each one

		for (
			ChatUserAlarmRec alarm
				: alarms
		) {

			try {

				doOneAlarm (
					alarm.getId ());

			} catch (Exception exception) {

				exceptionLogger.logThrowable (
					"daemon",
					stringFormat (
						"chatUserAlarm %s",
						alarm.getId ()),
					exception,
					Optional.<Integer>absent (),
					ExceptionResolution.tryAgainLater);

			}

		}

	}

	void doOneAlarm (
			int alarmId) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

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

		ChatCreditCheckResult creditCheckResult =
			chatCreditLogic.userSpendCreditCheck (
				user,
				false,
				Optional.<Integer>absent ());

		boolean ignore =
			creditCheckResult.failed ();

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

				.setOutbound (
					true);

		}

		// create a log

		chatUserInitiationLogHelper.insert (
			chatUserInitiationLogHelper.createInstance ()

			.setChatUser (
				user)

			.setMonitorChatUser (
				monitor)

			.setReason (
				reason)

			.setTimestamp (
				instantToDate (
					transaction.now ()))

			.setAlarmTime (
				alarm.getAlarmTime ()));

		// and commit

		transaction.commit ();

	}

}
