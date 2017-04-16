package wbs.apn.chat.user.core.daemon;

import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.time.TimeUtils.earlierThan;

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

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"runOnce ()");

		taskLogger.debugFormat (
			"Looking for quiet users");

		try (

			Transaction transaction =
				database.beginReadOnly (
					"ChatUserQuietDaemon.runOnce ()",
					this);

		) {

			// get a list of users who are past their outbound timestamp

			List <ChatUserRec> chatUsers =
				chatUserHelper.findWantingQuietOutbound (
					transaction.now ());

			transaction.close ();

			// then do each one

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
						"Chat daemon",
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
			logContext.nestTaskLogger (
				parentTaskLogger,
				"doUser");

		try (

			Transaction transaction =
				database.beginReadWrite (
					"ChatUserQuietDaemon.doUser (chatUserId)",
					this);

		) {

			// find the user

			ChatUserRec user =
				chatUserHelper.findRequired (
					chatUserId);

			String userPath =
				objectManager.objectPath (
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

				taskLogger.noticeFormat (
					"Skipping quiet alarm for %s: barred",
					userPath);

				transaction.commit ();

				return;
			}

			if (user.getCreditMode () == ChatUserCreditMode.barred) {

				taskLogger.noticeFormat (
					"Skipping quiet alarm for %s: barred",
					userPath);

				transaction.commit ();

				return;

			}

			// check if they are a "good" user

			if (user.getCreditSuccess () < 300) {

				taskLogger.noticeFormat (
					"Skipping quiet alarm for %s: low credit success",
					userPath);

				transaction.commit ();

				return;
			}

			// find a monitor

			ChatUserRec monitor =
				chatLogic.getOnlineMonitorForOutbound (
					user);

			if (monitor == null) {

				taskLogger.noticeFormat (
					"Skipping quiet alarm for %s: no available monitor",
					userPath);

				transaction.commit ();

				return;
			}

			String monitorPath =
				objectManager.objectPath (
					monitor);

			// create or update the inbox

			ChatMonitorInboxRec chatMonitorInbox =
				chatMessageLogic.findOrCreateChatMonitorInbox (
					taskLogger,
					monitor,
					user,
					true);

			chatMonitorInbox

				.setOutbound (
					true);

			// create a log

			chatUserInitiationLogHelper.insert (
				taskLogger,
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

			taskLogger.noticeFormat (
				"Setting quiet alarm for %s with %s",
				userPath,
				monitorPath);

			transaction.commit ();

		}

	}

}
