package wbs.apn.chat.user.core.daemon;

import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.string.StringUtils.keyEqualsDecimalInteger;
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
import wbs.framework.logging.OwnedTaskLogger;
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

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"runOnce");

		) {

			List <Long> chatUserIds =
				getChatUsers (
					taskLogger);

			chatUserIds.forEach (
				chatUserId ->
					doChatUser (
						taskLogger,
						chatUserId));

		}

	}

	private
	List <Long> getChatUsers (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnlyWithoutParameters (
					logContext,
					parentTaskLogger,
					"getChatUsers");

		) {

			return iterableMapToList (
				chatUserHelper.findWantingJoinOutbound (
					transaction,
					transaction.now ()),
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
					stringFormat (
						"chat user ",
						integerToDecimalString (
							chatUserId)),
					exception,
					optionalAbsent (),
					GenericExceptionResolution.tryAgainLater);

			}

		}

	}

	void doChatUserReal (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long chatUserId) {

		try (

			OwnedTransaction transaction =
				database.beginReadWriteWithParameters (
					logContext,
					parentTaskLogger,
					"doChatUserReal",
					keyEqualsDecimalInteger (
						"chatUserId",
						chatUserId));

		) {

			// find the user

			ChatUserRec user =
				chatUserHelper.findRequired (
					transaction,
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
					transaction,
					user);

			if (monitor == null) {

				transaction.commit ();

				return;

			}

			// create or update the cmi

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
					ChatUserInitiationReason.joinUser)

				.setTimestamp (
					transaction.now ())

			);

			transaction.commit ();

		}

	}

}
