package wbs.apn.chat.help.console;

import static wbs.framework.utils.etc.Misc.dateToInstant;

import java.util.Calendar;
import java.util.Collection;

import javax.inject.Inject;

import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.bill.model.ChatUserCreditMode;
import wbs.apn.chat.core.console.ChatConsoleLogic;
import wbs.apn.chat.help.model.ChatHelpLogRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.Html;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.currency.logic.CurrencyLogic;
import wbs.sms.message.core.console.MessageConsoleStuff;

@PrototypeComponent ("chatHelpLogPendingSummaryPart")
public
class ChatHelpLogPendingSummaryPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	ChatConsoleLogic chatConsoleLogic;

	@Inject
	ChatCreditLogic chatCreditLogic;

	@Inject
	ChatHelpLogConsoleHelper chatHelpLogHelper;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	CurrencyLogic currencyLogic;

	@Inject
	TimeFormatter timeFormatter;

	// state

	ChatHelpLogRec chatHelpLog;
	ChatUserRec chatUser;
	Collection<ChatHelpLogRec> chatHelpLogs;

	// implementation

	@Override
	public
	void prepare () {

		chatHelpLog =
			chatHelpLogHelper.find (
				requestContext.stuffInt ("chatHelpLogId"));

		chatUser =
			chatHelpLog.getChatUser ();

		chatHelpLogs =
			chatUser.getChatHelpLogs ();

	}

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",
			"<th>User</th>\n",
			"%s\n",
			objectManager.tdForObject (
				chatUser,
				chatUser.getChat (),
				true,
				true),
			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Date mode</th>\n",

			"<td>%h</td>\n",
			chatUser.getDateMode (),

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Online</th>\n",

			"<td>%s</td>\n",
			chatUser.getOnline ()
				? "yes"
				: "no",

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Gender</th>\n",

			"<td>%h</td>\n",
			chatUser.getGender (),

			"</tr>");

		printFormat (
			"<tr>\n",
			"<th>Orientation</th>\n",

			"<td>%h</td>\n",
			chatUser.getOrient (),

			"</tr>");

		printFormat (
			"<tr>\n",
			"<th>Name</th>\n",

			"<td>%h</td>\n",
			chatUser.getName (),

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Info</th>\n",

			"<td>%h</td>\n",
			chatUser.getInfoText (),

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Credit</th>\n",

			"<td>%s</td>\n",
			currencyLogic.formatHtml (
				chatUser.getChat ().getCurrency (),
				chatUser.getCredit ()),

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Barred</th>\n",

			"<td>%h</td>\n",
			chatUser.getBarred ()
				? "yes"
				: "no",

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Credit mode</th>\n",

			"<td>%h</td>\n",
			chatUser.getCreditMode (),

			"</tr>\n");

		if (chatUser.getCreditMode () == ChatUserCreditMode.strict) {

			printFormat (
				"<tr>\n",

				"<th>Temp barred</th>\n",

				"<td>%h</td>\n",
				chatCreditLogic.userStrictCreditOk (chatUser) ? "no" : "yes",

				"</tr>\n");

		}

		printFormat (
			"</table>\n");

		printFormat (
			"<h2>History</h2>\n");

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>&nbsp;</th>\n",
			"<th>Time</th>\n",
			"<th>Text</th>\n",
			"<th>Our number</th>\n",
			"<th>User</th>\n");

		int dayNumber = 0;

		Calendar calendar =
			Calendar.getInstance ();

		for (ChatHelpLogRec help : chatHelpLogs) {

			calendar.setTime (
				help.getTimestamp ());

			int newDayNumber =
				+ (calendar.get (Calendar.YEAR) << 9)
				+ calendar.get (Calendar.DAY_OF_YEAR);

			if (newDayNumber != dayNumber) {

				dayNumber =
					newDayNumber;

				printFormat (
					"<tr class=\"sep\">\n",

					"<tr style=\"font-weight: bold\">\n",

					"<td colspan=\"5\">%h</td>\n",
					timeFormatter.instantToDateStringLong (
						dateToInstant (help.getTimestamp ())),

					"</tr>\n");

			}

			String rowClass =
				MessageConsoleStuff.classForMessageDirection (
					help.getDirection ());

			printFormat (
				"<tr class=\"%h\">\n",
				rowClass,

				"<td style=\"background: %h\">&nbsp;</td>\n",
				Html.genHtmlColor (
					help.getOurNumber ()),

				"<td>%h</td>\n",
				timeFormatter.instantToTimeString (
					dateToInstant (help.getTimestamp())),

				"<td>%h</td>\n",
				help.getText (),

				"<td>%h</td>\n",
				help.getOurNumber (),

				"<td>%h</td>\n",
				help.getUser () == null
					? ""
					: help.getUser ().getUsername (),

				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
