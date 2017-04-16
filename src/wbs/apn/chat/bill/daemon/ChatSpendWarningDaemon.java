package wbs.apn.chat.bill.daemon;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;

import java.util.List;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectManager;

import wbs.platform.daemon.SleepingDaemonService;

import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.contact.logic.ChatSendLogic.TemplateMissing;
import wbs.apn.chat.scheme.model.ChatSchemeChargesRec;
import wbs.apn.chat.scheme.model.ChatSchemeRec;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;

@SingletonComponent ("chatSpendWarningDaemon")
public
class ChatSpendWarningDaemon
	extends SleepingDaemonService {

	// singleton dependencies

	@SingletonDependency
	ChatSendLogic chatSendLogic;

	@SingletonDependency
	ChatUserObjectHelper chatUserHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ObjectManager objectManager;

	// details

	@Override
	protected
	String backgroundProcessName () {
		return "chat-user.spend-warning";
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

		taskLogger.debugFormat (
			"Looking for users to send spend warning to");

		// get a list of users who need a spend warning

		try (

			Transaction transaction =
				database.beginReadOnly (
					taskLogger,
					"ChatSpendWarningDaemon.runOnce ()",
					this);

		) {

			List <ChatUserRec> chatUsers =
				chatUserHelper.findWantingWarning ();

			transaction.close ();

			for (
				ChatUserRec chatUser
					: chatUsers
			) {

				try {

					doUser (
						taskLogger,
						chatUser.getId ());

				} catch (Exception exception) {

					exceptionLogger.logThrowable (
						taskLogger,
						"daemon",
						"ChatSpendWarningDaemon",
						exception,
						optionalAbsent (),
						GenericExceptionResolution.tryAgainLater);

				}

			}

		}

	}

	private
	void doUser (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long chatUserId) {

		TaskLogger taskLogger =
			logContext.nestTaskLoggerFormat (
				parentTaskLogger,
				"doUser (%s)",
				integerToDecimalString (
					chatUserId));

		try (

			Transaction transaction =
				database.beginReadWrite (
					taskLogger,
					"ChatSpendWarningDaemon.doUser (chatUserId)",
					this);

		) {

			ChatUserRec chatUser =
				chatUserHelper.findRequired (
					chatUserId);

			ChatSchemeRec chatScheme =
				chatUser.getChatScheme ();

			ChatSchemeChargesRec chatSchemeCharges =
				chatScheme.getCharges ();

			// check warning is due

			if (
				chatUser.getValueSinceWarning ()
					< chatSchemeCharges.getSpendWarningEvery ()
			) {
				return;
			}

			// log message

			taskLogger.noticeFormat (
				"Sending warning to user %s",
				objectManager.objectPathMini (
					chatUser));

			// send message

			chatSendLogic.sendSystemRbFree (
				taskLogger,
				chatUser,
				optionalAbsent (),
				chatUser.getNumSpendWarnings () == 0
					? "spend_warning_1"
					: "spend_warning_2",
				TemplateMissing.error,
				emptyMap ());

			// update user

			chatUser

				.setValueSinceWarning (
					+ chatUser.getValueSinceWarning ()
					- chatSchemeCharges.getSpendWarningEvery ())

				.setNumSpendWarnings (
					+ chatUser.getNumSpendWarnings ()
					+ 1);

			// commit and return

			transaction.commit ();

		}

	}

}
