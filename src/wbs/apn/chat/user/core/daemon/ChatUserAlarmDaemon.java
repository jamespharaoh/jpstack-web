package wbs.apn.chat.user.core.daemon;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import lombok.Cleanup;
import lombok.NonNull;

import org.joda.time.Duration;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.daemon.SleepingDaemonService;

import wbs.apn.chat.bill.logic.ChatCreditCheckResult;
import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.contact.logic.ChatMessageLogic;
import wbs.apn.chat.contact.model.ChatMonitorInboxRec;
import wbs.apn.chat.contact.model.ChatUserInitiationLogObjectHelper;
import wbs.apn.chat.contact.model.ChatUserInitiationReason;
import wbs.apn.chat.user.core.model.ChatUserAlarmObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserAlarmRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

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

	@ClassSingletonDependency
	LogContext logContext;

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
	void runOnce (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"runOnce ()");

		// get a list of alarms which are ready to go off

		try (

			Transaction transaction =
				database.beginReadOnly (
					"ChatUserAlarmDaemon.runOnce ()",
					this);

		) {

			List <ChatUserAlarmRec> alarms =
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
						taskLogger,
						alarm.getId ());

				} catch (Exception exception) {

					exceptionLogger.logThrowable (
						taskLogger,
						"daemon",
						stringFormat (
							"chatUserAlarm %s",
							integerToDecimalString (
								alarm.getId ())),
						exception,
						optionalAbsent (),
						GenericExceptionResolution.tryAgainLater);

				}

			}

		}

	}

	void doOneAlarm (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long alarmId) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"doOneAlarm");

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
				taskLogger,
				user,
				false,
				optionalAbsent ());

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
					taskLogger,
					monitor,
					user,
					true);

			chatMonitorInbox

				.setOutbound (
					true);

		}

		// create a log

		chatUserInitiationLogHelper.insert (
			taskLogger,
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
