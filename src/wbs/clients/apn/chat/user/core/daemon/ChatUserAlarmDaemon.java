package wbs.clients.apn.chat.user.core.daemon;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.List;

import com.google.common.base.Optional;

import lombok.Cleanup;
import lombok.NonNull;

import org.joda.time.Duration;

import wbs.clients.apn.chat.bill.logic.ChatCreditCheckResult;
import wbs.clients.apn.chat.bill.logic.ChatCreditLogic;
import wbs.clients.apn.chat.contact.logic.ChatMessageLogic;
import wbs.clients.apn.chat.contact.model.ChatMonitorInboxRec;
import wbs.clients.apn.chat.contact.model.ChatUserInitiationLogObjectHelper;
import wbs.clients.apn.chat.contact.model.ChatUserInitiationReason;
import wbs.clients.apn.chat.user.core.model.ChatUserAlarmObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserAlarmRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.platform.daemon.SleepingDaemonService;

@SingletonComponent ("chatUserAlarmDaemon")
public
class ChatUserAlarmDaemon
	extends SleepingDaemonService {

	// singleton dependencies

	@SingletonDependency
	ChatCreditLogic chatCreditLogic;

	@SingletonDependency
	ChatMessageLogic chatMessageLogic;

	@SingletonDependency
	ChatUserAlarmObjectHelper chatUserAlarmHelper;

	@SingletonDependency
	ChatUserInitiationLogObjectHelper chatUserInitiationLogHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	// details

	@Override
	protected
	String getThreadName () {
		return "ChatUserAlarm";
	}

	@Override
	protected
	Duration getSleepDuration () {

		return Duration.standardSeconds (
			10);

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
				"ChatUserAlarmDaemon.runOnce ()",
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
					Optional.absent (),
					GenericExceptionResolution.tryAgainLater);

			}

		}

	}

	void doOneAlarm (
			@NonNull Long alarmId) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ChatUserAlarmDaemon.doOneAlarm (alarmId)",
				this);

		// find the alarm and stuff

		ChatUserAlarmRec alarm =
			chatUserAlarmHelper.findRequired (
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
				Optional.<Long>absent ());

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
				transaction.now ())

			.setAlarmTime (
				alarm.getAlarmTime ()));

		// and commit

		transaction.commit ();

	}

}
