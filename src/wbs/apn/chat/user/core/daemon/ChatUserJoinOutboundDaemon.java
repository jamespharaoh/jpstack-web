package wbs.apn.chat.user.core.daemon;

import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.string.StringUtils.stringFormat;
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
import wbs.framework.logging.TaskLogger;

import wbs.platform.daemon.SleepingDaemonService;

import wbs.apn.chat.contact.logic.ChatMessageLogic;
import wbs.apn.chat.contact.model.ChatMonitorInboxRec;
import wbs.apn.chat.contact.model.ChatUserInitiationLogObjectHelper;
import wbs.apn.chat.contact.model.ChatUserInitiationReason;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;

@SingletonComponent ("chatUserJoinOutboundDaemon")
public
class ChatUserJoinOutboundDaemon
	extends SleepingDaemonService {

	// singleton dependencies

	@SingletonDependency
	ChatMessageLogic chatMessageLogic;

	@SingletonDependency
	ChatMiscLogic chatMiscLogic;

	@SingletonDependency
	ChatUserObjectHelper chatUserHelper;

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
		return "chat-user.join-outbound";
	}

	// implementation

	@Override
	protected
	void runOnce (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"runOnce ()");

			OwnedTransaction transaction =
				database.beginReadOnly (
					taskLogger,
					"ChatUserJoinOutboundDaemon.runOnce ()",
					this);

		) {

			List <ChatUserRec> chatUsers =
				chatUserHelper.findWantingJoinOutbound (
					transaction.now ());

			transaction.close ();

			// then do each one

			for (
				ChatUserRec chatUser
					: chatUsers
			) {

				try {

					doChatUserJoinOutbound (
						taskLogger,
						chatUser.getId ());

				} catch (Exception exception) {

					exceptionLogger.logThrowable (
						taskLogger,
						"daemon",
						stringFormat (
							"chat user ",
							integerToDecimalString (
								chatUser.getId ())),
						exception,
						optionalAbsent (),
						GenericExceptionResolution.tryAgainLater);

				}

			}

		}

	}

	void doChatUserJoinOutbound (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long chatUserId) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"doChatUserJoinOutbound");

			OwnedTransaction transaction =
				database.beginReadWrite (
					taskLogger,
					stringFormat (
						"%s.%s (%s)",
						"ChatUserJoinOutboundDaemon",
						"doChatUserJoinOutbound",
						stringFormat (
							"chatUserId = %s",
							integerToDecimalString (
								chatUserId))),
					this);

		) {

			// find the user

			ChatUserRec user =
				chatUserHelper.findRequired (
					chatUserId);

			// check and clear the outbound message flag

			if (

				isNull (
					user.getNextJoinOutbound ())

				|| earlierThan (
					transaction.now (),
					user.getNextJoinOutbound ())

			) {

				return;

			}

			user

				.setNextJoinOutbound (
					null);

			// find a monitor

			ChatUserRec monitor =
				chatMiscLogic.getOnlineMonitorForOutbound (
					user);

			if (monitor == null) {

				transaction.commit ();

				return;

			}

			// create or update the cmi

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
					ChatUserInitiationReason.joinUser)

				.setTimestamp (
					transaction.now ())

			);

			transaction.commit ();

		}

	}

}
