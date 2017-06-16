package wbs.apn.chat.bill.daemon;

import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.keyEqualsDecimalInteger;

import java.util.List;

import lombok.NonNull;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.platform.daemon.SleepingDaemonService;

import wbs.utils.time.TimeFormatter;

import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.bill.logic.ChatCreditLogic.BillCheckOptions;
import wbs.apn.chat.core.model.ChatObjectHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;

@SingletonComponent ("chatCreditDaemon")
public
class ChatCreditDaemon
	extends SleepingDaemonService {

	// singleton dependencies

	@SingletonDependency
	ChatCreditLogic chatCreditLogic;

	@SingletonDependency
	ChatObjectHelper chatHelper;

	@SingletonDependency
	ChatUserObjectHelper chatUserHelper;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	TimeFormatter timeFormatter;

	// details

	@Override
	protected
	String friendlyName () {
		return "Chat credit";
	}

	@Override
	protected
	String backgroundProcessName () {
		return "chat-user.billed-messages";
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

			List <Long> chatIds =
				getChatIds (
					parentTaskLogger);

			chatIds.forEach (
				chatId ->
					doChat (
						taskLogger,
						chatId));

		}

	}

	// private implementation

	private
	List <Long> getChatIds (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnly (
					logContext,
					parentTaskLogger,
					"runOnce");

		) {

			transaction.debugFormat (
				"Checking for all users with negative credit");

			return iterableMapToList (
				chatHelper.findNotDeleted (
					transaction),
				ChatRec::getId);

		}

	}

	private
	void doChat (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long chatId) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"doChat");

		) {

			List <Long> chatUserIds =
				getChatUserIds (
					taskLogger,
					chatId);

			taskLogger.debugFormat (
				"Found %s users",
				integerToDecimalString (
					chatUserIds.size ()));

			chatUserIds.forEach (
				chatUserId ->
					doChatUser (
						taskLogger,
						chatUserId));

		}

	}

	private
	List <Long> getChatUserIds (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long chatId) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnly (
					logContext,
					parentTaskLogger,
					"getChatUserIds",
					keyEqualsDecimalInteger (
						"chatId",
						chatId));

		) {

			ChatRec chat =
				chatHelper.findRequired (
					transaction,
					chatId);

			Instant cutoffTime =
				transaction.now ().minus (
					Duration.standardSeconds (
						chat.getBillTimeLimit ()));

			transaction.debugFormat (
				"Chat billing after %s",
				timeFormatter.timestampSecondStringIso (
					cutoffTime));

			return iterableMapToList (
				chatUserHelper.findWantingBill (
					transaction,
					chat,
					cutoffTime),
				ChatUserRec::getId);

		}

	}

	private
	void doChatUser (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long chatUserId) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"doChatUser",
					keyEqualsDecimalInteger (
						"chatUserId",
						chatUserId));

		) {

			ChatUserRec chatUser =
				chatUserHelper.findRequired (
					transaction,
					chatUserId);

			chatCreditLogic.userBill (
				transaction,
				chatUser,
				new BillCheckOptions ());

			transaction.commit ();

		}

	}

}
