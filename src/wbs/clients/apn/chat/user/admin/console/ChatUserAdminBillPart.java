package wbs.clients.apn.chat.user.admin.console;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.millisToInstant;

import java.util.List;

import javax.inject.Inject;

import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import wbs.clients.apn.chat.bill.logic.ChatCreditLogic;
import wbs.clients.apn.chat.bill.model.ChatUserBillLogObjectHelper;
import wbs.clients.apn.chat.bill.model.ChatUserBillLogRec;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.ChatUserType;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.utils.TimeFormatter;

@PrototypeComponent ("chatUserAdminBillPart")
public
class ChatUserAdminBillPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	ChatCreditLogic chatCreditLogic;

	@Inject
	ChatUserBillLogObjectHelper chatUserBillLogHelper;

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	ConsoleObjectManager consoleObjectManager;

	@Inject
	Database database;

	@Inject
	TimeFormatter timeFormatter;

	// state

	ChatUserRec chatUser;
	ChatRec chat;

	List<ChatUserBillLogRec> todayBillLogs;
	List<ChatUserBillLogRec> allBillLogs;
	boolean billLimitReached;

	// implementation

	@Override
	public
	void prepare () {

		Transaction transaction =
			database.currentTransaction ();

		chatUser =
			chatUserHelper.findRequired (
				requestContext.stuffInt (
					"chatUserId"));

		if (
			equal (
				chatUser.getType (),
				ChatUserType.monitor)
		) {
			return;
		}

		DateTimeZone timezone =
			chatUserLogic.timezone (
				chatUser);

		LocalDate today =
			transaction
				.now ()
				.toDateTime (timezone)
				.toLocalDate ();

		Instant startTime =
			today
				.toDateTimeAtStartOfDay (timezone)
				.toInstant ();

		Instant endTime =
			today
				.plusDays (1)
				.toDateTimeAtStartOfDay (timezone)
				.toInstant ();

		todayBillLogs =
			chatUserBillLogHelper.findByTimestamp (
				chatUser,
				new Interval (
					startTime,
					endTime));

		allBillLogs =
			chatUserBillLogHelper.findByTimestamp (
				chatUser,
				new Interval (
					millisToInstant (0),
					endTime));

		billLimitReached =
			chatCreditLogic.userBillLimitApplies (
				chatUser);

	}

	@Override
	public
	void renderHtmlBodyContent () {

		if (
			equal (
				chatUser.getType (),
				ChatUserType.monitor)
		) {

			printFormat (
				"<p>This is a monitor and cannot be billed.</p>\n");

			return;

		}

		boolean dailyAdminRebillLimitReached =
			todayBillLogs.size () >= 3;

		boolean canBypassDailyAdminRebillLimit =
			requestContext.canContext ("chat.manage");

		if (billLimitReached) {

			printFormat (
				"<p>Daily billed message limit reached.</p>\n");

		}

		if (dailyAdminRebillLimitReached) {

			printFormat (
				"<p>Daily admin rebill limit reached<br>\n",

				"%h admin rebills have been actioned today</p>\n",
				todayBillLogs.size ());

		}

		if (
			! billLimitReached
			&& (
				! dailyAdminRebillLimitReached
				|| canBypassDailyAdminRebillLimit
			)
		) {

			printFormat (
				"<form",
				" method=\"post\"",
				" action=\"%h\"",
				requestContext.resolveLocalUrl (
					"/chatUser.admin.bill"),
				">\n",

				"<p><input",
				" type=\"submit\"",
				" value=\"reset billing\"",
				"></p>\n",

				"</form>\n");

		}

		printFormat (
			"<h2>History</h2>\n");

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>Date</th>\n",
			"<th>Time</th>\n",
			"<th>User</th>\n",
			"</tr>\n");

		for (ChatUserBillLogRec billLog
				: allBillLogs) {

			printFormat (
				"<tr>\n",

				"<td>%h</td>\n",
				timeFormatter.dateStringShort (
					chatUserLogic.timezone (
						chatUser),
					billLog.getTimestamp ()),

				"<td>%h</td>\n",
				timeFormatter.timeString (
					chatUserLogic.timezone (
						chatUser),
					billLog.getTimestamp ()),

				"%s\n",
				consoleObjectManager.tdForObjectMiniLink (
					billLog.getUser ()),

				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}