package wbs.apn.chat.user.core.daemon;

import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
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
	String backgroundProcessName () {
		return "chat-user.alarms";
	}

	// implementation

	@Override
	protected
	void runOnce (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"runOnce");

		) {

			List <Long> chatUserAlarmIds =
				getPendingAlarms (
					taskLogger);

			chatUserAlarmIds.forEach (
				chatUserAlarmId ->
					doAlarm (
						taskLogger,
						chatUserAlarmId));

		}

	}

	private
	List <Long> getPendingAlarms (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnly (
					logContext,
					parentTaskLogger,
					"getPendingAlarms");

		) {

			return iterableMapToList (
				ChatUserAlarmRec::getId,
				chatUserAlarmHelper.findPending (
					transaction,
					transaction.now ()));

		}

	}

	private
	void doAlarm (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long chatUserAlarmId) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"doAlarm");

		) {

			try {

				doAlarmReal (
					taskLogger,
					chatUserAlarmId);

			} catch (Exception exception) {

				exceptionLogger.logThrowable (
					taskLogger,
					"daemon",
					stringFormat (
						"chatUserAlarm %s",
						integerToDecimalString (
							chatUserAlarmId)),
					exception,
					optionalAbsent (),
					GenericExceptionResolution.tryAgainLater);

			}

		}

	}

	void doAlarmReal (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long chatUserAlarmId) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"doAlarmReal");

		) {

			// find the alarm and stuff

			ChatUserAlarmRec alarm =
				chatUserAlarmHelper.findRequired (
					transaction,
					chatUserAlarmId);

			ChatUserRec user =
				alarm.getChatUser ();

			ChatUserRec monitor =
				alarm.getMonitorChatUser ();

			// delete the alarm

			chatUserAlarmHelper.remove (
				transaction,
				alarm);

			// check whether to ignore this alarm

			ChatCreditCheckResult creditCheckResult =
				chatCreditLogic.userSpendCreditCheck (
					transaction,
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
						transaction,
						monitor,
						user,
						true);

				chatMonitorInbox

					.setOutbound (
						true);

			}

			// create a log

			chatUserInitiationLogHelper.insert (
				transaction,
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

}
