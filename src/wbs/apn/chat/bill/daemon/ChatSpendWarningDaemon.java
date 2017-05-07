package wbs.apn.chat.bill.daemon;

import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;

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

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"runOnce");

		) {

			List <Long> chatUserIds =
				getChatUserIds (
					taskLogger);

			chatUserIds.forEach (
				chatUserId ->
					doChatUser (
						taskLogger,
						chatUserId));

		}

	}

	private
	List <Long> getChatUserIds (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnly (
					logContext,
					parentTaskLogger,
					"runOnce");

		) {

			transaction.debugFormat (
				"Looking for users to send spend warning to");

			return iterableMapToList (
				chatUserHelper.findWantingWarning (
					transaction),
				ChatUserRec::getId);

		}

	}

	private
	void doChatUser (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long chatUserId) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"doChatUser");

		) {

			try {

				doChatUserReal (
					taskLogger,
					chatUserId);

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

	private
	void doChatUserReal (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long chatUserId) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"doUser");

		) {

			ChatUserRec chatUser =
				chatUserHelper.findRequired (
					transaction,
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

			transaction.noticeFormat (
				"Sending warning to user %s",
				objectManager.objectPathMini (
					transaction,
					chatUser));

			// send message

			chatSendLogic.sendSystemRbFree (
				transaction,
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
