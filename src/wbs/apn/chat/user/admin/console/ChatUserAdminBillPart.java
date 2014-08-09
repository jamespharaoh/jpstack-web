package wbs.apn.chat.user.admin.console;

import static wbs.framework.utils.etc.Misc.dateToInstant;
import static wbs.framework.utils.etc.Misc.equal;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.inject.Inject;

import org.joda.time.Instant;
import org.joda.time.Interval;

import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.bill.model.ChatUserBillLogObjectHelper;
import wbs.apn.chat.bill.model.ChatUserBillLogRec;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserType;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.part.AbstractPagePart;

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
	ConsoleObjectManager consoleObjectManager;

	@Inject
	TimeFormatter timeFormatter;

	// state

	ChatUserRec chatUser;

	List<ChatUserBillLogRec> todayBillLogs;
	List<ChatUserBillLogRec> allBillLogs;
	boolean billLimitReached;

	// implementation

	@Override
	public
	void prepare () {

		chatUser =
			chatUserHelper.find (
				requestContext.stuffInt ("chatUserId"));

		if (
			equal (
				chatUser.getType (),
				ChatUserType.monitor)
		) {
			return;
		}

		Calendar calendar =
			new GregorianCalendar ();

		calendar.set (
			Calendar.HOUR_OF_DAY,
			0);

		calendar.set (
			Calendar.MINUTE,
			0);

		calendar.set (
			Calendar.SECOND,
			0);

		calendar.set (
			Calendar.MILLISECOND,
			0);

		Date from =
			calendar.getTime ();

		calendar.add (
			Calendar.DATE,
			1);

		Date to =
			calendar.getTime ();

		todayBillLogs =
			chatUserBillLogHelper.findByTimestamp (
				chatUser,
				new Interval (
					dateToInstant (from),
					dateToInstant (to)));

		allBillLogs =
			chatUserBillLogHelper.findByTimestamp (
				chatUser,
				new Interval (
					new Instant (0),
					dateToInstant (to)));

		billLimitReached =
			chatCreditLogic.userBillLimitApplies (
				chatUser);

	}

	@Override
	public
	void goBodyStuff () {

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
				timeFormatter.instantToDateStringShort (
					dateToInstant (billLog.getTimestamp ())),

				"<td>%h</td>\n",
				timeFormatter.instantToTimeString (
					dateToInstant (billLog.getTimestamp ())),

				"%s\n",
				consoleObjectManager.tdForObject (
					billLog.getUser (),
					null,
					true,
					true),

				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}