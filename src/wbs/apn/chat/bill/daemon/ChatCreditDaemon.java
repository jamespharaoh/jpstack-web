package wbs.apn.chat.bill.daemon;

import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import lombok.NonNull;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
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
	Duration getSleepDuration () {

		return Duration.standardSeconds (
			30);

	}

	@Override
	protected
	String generalErrorSource () {

		return "chat credit daemon";

	}

	@Override
	protected
	String generalErrorSummary () {

		return "error finding users with negative credit";

	}

	@Override
	protected
	String getThreadName () {

		return "ChatCredit";

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
			"Checking for all users with negative credit");

		List <Long> chatIds;

		try (

			Transaction transaction =
				database.beginReadOnly (
					"ChatCreditDaemon.runOnce ()",
					this);

		) {

			chatIds =
				iterableMapToList (
					ChatRec::getId,
					chatHelper.findAll ());

			transaction.close ();

		}

		chatIds.forEach (
			chatId ->
				doChat (
					taskLogger,
					chatId));

	}

	private
	void doChat (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long chatId) {

		TaskLogger taskLogger =
			logContext.nestTaskLoggerFormat (
				parentTaskLogger,
				"doChat (%s)",
				integerToDecimalString (
					chatId));

		try (

			Transaction transaction =
				database.beginReadOnly (
					stringFormat (
						"%s.%s (%s)",
						"ChatCreditDaemon",
						"doChat",
						stringFormat (
							"chatId = %s",
							integerToDecimalString (
								chatId))),
					this);

		) {

			ChatRec chat =
				chatHelper.findRequired (
					chatId);

			Instant cutoffTime =
				transaction.now ().minus (
					Duration.standardSeconds (
						chat.getBillTimeLimit ()));

			taskLogger.debugFormat (
				"Chat billing after %s",
				timeFormatter.timestampSecondStringIso (
					cutoffTime));

			List <Long> chatUserIds =
				iterableMapToList (
					ChatUserRec::getId,
					chatUserHelper.findWantingBill (
						chat,
						cutoffTime));

			transaction.close ();

			taskLogger.debugFormat (
				"Found %s users",
				integerToDecimalString (
					chatUserIds.size ()));

			chatUserIds.forEach (
				chatUserId ->
					doUserCredit (
						taskLogger,
						chatUserId));

		}

	}

	private
	void doUserCredit (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long chatUserId) {

		TaskLogger taskLogger =
			logContext.nestTaskLoggerFormat (
				parentTaskLogger,
				"doUserCredit (%s)",
				integerToDecimalString (
					chatUserId));

		try (

			Transaction transaction =
				database.beginReadWrite (
					stringFormat (
						"%s.%s (%s)",
						"ChatCreditDaemon",
						"doUserCredit",
						stringFormat (
							"chatUserId = %s",
							integerToDecimalString (
								chatUserId))),
					this);

		) {

			ChatUserRec chatUser =
				chatUserHelper.findRequired (
					chatUserId);

			chatCreditLogic.userBill (
				taskLogger,
				chatUser,
				new BillCheckOptions ());

			transaction.commit ();

		}

	}

}
