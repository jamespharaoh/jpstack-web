package wbs.clients.apn.chat.bill.daemon;

import static wbs.framework.utils.etc.Misc.instantToDate;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.clients.apn.chat.bill.logic.ChatCreditLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.daemon.SleepingDaemonService;

@Log4j
@SingletonComponent ("chatCreditDaemon")
public
class ChatCreditDaemon
	extends SleepingDaemonService {

	// dependencies

	@Inject
	ChatCreditLogic chatCreditLogic;

	@Inject
	ChatUserObjectHelper chatUserHelper;

	@Inject
	Database database;

	// details

	@Override
	protected
	int getDelayMs () {

		return 15 * 1000;

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

	private
	void doUserCredit (
			int chatUserId) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		ChatUserRec chatUser =
			chatUserHelper.find (
				chatUserId);

		chatCreditLogic.userBill (
			chatUser,
			false);

		transaction.commit ();

	}

	@Override
	protected
	void runOnce () {

		log.debug (
			"Checking for all users with negative credit");

		@Cleanup
		Transaction transaction =
			database.beginReadOnly ();

		Instant threeMonthsAgo =
			transaction
				.now ()
				.minus (Duration.standardDays (90));

		log.debug (
			stringFormat (
				"Chat billing after %s",
				threeMonthsAgo));

		List<ChatUserRec> users =
			chatUserHelper.findWantingBill (
				instantToDate (
					threeMonthsAgo));

		transaction.close ();

		log.debug (
			stringFormat (
				"Chat billing after %s",
				users.size ()));

		for (ChatUserRec chatUser
				: users) {

			doUserCredit (
				chatUser.getId ());

		}

	}

}
