package wbs.apn.chat.user.core.daemon;

import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.time.TimeUtils.earlierThan;

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

import wbs.apn.chat.bill.model.ChatUserCreditMode;
import wbs.apn.chat.contact.logic.ChatMessageLogic;
import wbs.apn.chat.contact.model.ChatMonitorInboxRec;
import wbs.apn.chat.contact.model.ChatUserInitiationLogObjectHelper;
import wbs.apn.chat.contact.model.ChatUserInitiationReason;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;

@SingletonComponent ("chatUserQuietDaemon")
public
class ChatUserQuietDaemon
	extends SleepingDaemonService {

	// singleton dependencies

	@SingletonDependency
	ChatMiscLogic chatLogic;

	@SingletonDependency
	ChatMessageLogic chatMessageLogic;

	@SingletonDependency
	ChatUserInitiationLogObjectHelper chatUserInitiationLogHelper;

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
		return "chat-user.quiet-outbound";
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

			taskLogger.logicFormat (
				"Looking for quiet users");

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
					"getChatUserIds");

		) {

			return iterableMapToList (
				ChatUserRec::getId,
				chatUserHelper.findWantingQuietOutbound (
					transaction,
					transaction.now ()));

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
					"Chat daemon",
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
					"doChatUserReal");

		) {

			// find the user

			ChatUserRec user =
				chatUserHelper.findRequired (
					transaction,
					chatUserId);

			String userPath =
				objectManager.objectPath (
					transaction,
					user);

			// check and clear the outbound message flag

			if (

				isNull (
					user.getNextQuietOutbound ())

				|| earlierThan (
					transaction.now (),
					user.getNextQuietOutbound ())

			) {
				return;
			}

			user

				.setNextQuietOutbound (
					null);

			// check if they have been barred

			if (user.getBarred ()) {

				transaction.noticeFormat (
					"Skipping quiet alarm for %s: barred",
					userPath);

				transaction.commit ();

				return;
			}

			if (user.getCreditMode () == ChatUserCreditMode.barred) {

				transaction.noticeFormat (
					"Skipping quiet alarm for %s: barred",
					userPath);

				transaction.commit ();

				return;

			}

			// check if they are a "good" user

			if (user.getCreditSuccess () < 300) {

				transaction.noticeFormat (
					"Skipping quiet alarm for %s: low credit success",
					userPath);

				transaction.commit ();

				return;
			}

			// find a monitor

			ChatUserRec monitor =
				chatLogic.getOnlineMonitorForOutbound (
					transaction,
					user);

			if (monitor == null) {

				transaction.noticeFormat (
					"Skipping quiet alarm for %s: no available monitor",
					userPath);

				transaction.commit ();

				return;
			}

			String monitorPath =
				objectManager.objectPath (
					transaction,
					monitor);

			// create or update the inbox

			ChatMonitorInboxRec chatMonitorInbox =
				chatMessageLogic.findOrCreateChatMonitorInbox (
					transaction,
					monitor,
					user,
					true);

			chatMonitorInbox

				.setOutbound (
					true);

			// create a log

			chatUserInitiationLogHelper.insert (
				transaction,
				chatUserInitiationLogHelper.createInstance ()

				.setChatUser (
					user)

				.setMonitorChatUser (
					monitor)

				.setReason (
					ChatUserInitiationReason.quietUser)

				.setTimestamp (
					transaction.now ())

			);

			// and return

			transaction.noticeFormat (
				"Setting quiet alarm for %s with %s",
				userPath,
				monitorPath);

			transaction.commit ();

		}

	}

}
