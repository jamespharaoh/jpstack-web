package wbs.clients.apn.chat.bill.daemon;

import static wbs.framework.utils.etc.IterableUtils.iterableMapToList;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.List;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.clients.apn.chat.bill.logic.ChatCreditLogic;
import wbs.clients.apn.chat.core.model.ChatObjectHelper;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.daemon.SleepingDaemonService;

@Log4j
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
	void runOnce () {

		log.debug (
			"Checking for all users with negative credit");

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				"ChatCreditDaemon.runOnce ()",
				this);

		List <Long> chatIds =
			iterableMapToList (
				ChatRec::getId,
				chatHelper.findAll ());

		transaction.close ();

		chatIds.forEach (
			this::doChat);

	}

	private
	void doChat (
			@NonNull Long chatId) {

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				stringFormat (
					"%s.%s (%s)",
					"ChatCreditDaemon",
					"doChat",
					stringFormat (
						"chatId = %s",
						chatId)),
				this);

		ChatRec chat =
			chatHelper.findRequired (
				chatId);

		Instant cutoffTime =
			transaction.now ().minus (
				Duration.standardSeconds (
					chat.getBillTimeLimit ()));

		log.debug (
			stringFormat (
				"Chat billing after %s",
				cutoffTime));

		List <Long> chatUserIds =
			iterableMapToList (
				ChatUserRec::getId,
				chatUserHelper.findWantingBill (
					chat,
					cutoffTime));

		transaction.close ();

		log.debug (
			stringFormat (
				"Found %s users",
				chatUserIds.size ()));

		chatUserIds.forEach (
			this::doUserCredit);

	}

	private
	void doUserCredit (
			@NonNull Long chatUserId) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				stringFormat (
					"%s.%s (%s)",
					"ChatCreditDaemon",
					"doUserCredit",
					stringFormat (
						"chatUserId = %s",
						chatUserId)),
				this);

		ChatUserRec chatUser =
			chatUserHelper.findRequired (
				chatUserId);

		chatCreditLogic.userBill (
			chatUser,
			false);

		transaction.commit ();

	}

}
