package wbs.clients.apn.chat.help.console;

import static wbs.framework.utils.etc.Misc.emptyStringIfNull;
import static wbs.framework.utils.etc.Misc.notEqual;

import java.util.Collection;

import javax.inject.Inject;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import wbs.clients.apn.chat.bill.logic.ChatCreditCheckResult;
import wbs.clients.apn.chat.bill.logic.ChatCreditLogic;
import wbs.clients.apn.chat.core.console.ChatConsoleLogic;
import wbs.clients.apn.chat.help.model.ChatHelpLogRec;
import wbs.clients.apn.chat.scheme.model.ChatSchemeRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.Html;
import wbs.platform.currency.logic.CurrencyLogic;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.sms.message.core.console.MessageConsoleLogic;

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
	MessageConsoleLogic messageConsoleLogic;

	@Inject
	UserConsoleLogic userConsoleLogic;

	// state

	DateTimeZone timezone;

	ChatHelpLogRec chatHelpLog;
	ChatUserRec chatUser;
	Collection<ChatHelpLogRec> chatHelpLogs;

	// implementation

	@Override
	public
	void prepare () {

		chatHelpLog =
			chatHelpLogHelper.findOrNull (
				requestContext.stuffInt ("chatHelpLogId"));

		chatUser =
			chatHelpLog.getChatUser ();

		ChatSchemeRec chatScheme =
			chatUser.getChatScheme ();

		timezone =
			DateTimeZone.forID (
				chatScheme.getTimezone ());

		chatHelpLogs =
			chatUser.getChatHelpLogs ();

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",
			"<th>User</th>\n",
			"%s\n",
			objectManager.tdForObjectMiniLink (
				chatUser,
				chatUser.getChat ()),
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
			chatUser.getGender () != null
				? chatUser.getGender ()
				: "-",

			"</tr>");

		printFormat (
			"<tr>\n",
			"<th>Orientation</th>\n",

			"<td>%h</td>\n",
			chatUser.getOrient () != null
				? chatUser.getOrient ()
				: "-",

			"</tr>");

		printFormat (
			"<tr>\n",
			"<th>Name</th>\n",
			"<td>%h</td>\n",
			emptyStringIfNull (
				chatUser.getName ()),
			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Info</th>\n",

			"<td>%h</td>\n",
			chatUser.getInfoText () != null
				? chatUser.getInfoText ().getText ()
				: "",

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Credit</th>\n",

			"<td>%s</td>\n",
			currencyLogic.formatHtml (
				chatUser.getChat ().getCurrency (),
				(long) chatUser.getCredit ()),

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

		ChatCreditCheckResult creditCheckResult =
			chatCreditLogic.userCreditCheck (
				chatUser);

		printFormat (
			"<tr>\n",
			"<th>Credit check</th>\n",

			"<td>%h</td>\n",
			creditCheckResult.details (),

			"</tr>\n");

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

		LocalDate previousDate = null;

		for (
			ChatHelpLogRec chatHelpLog
				: chatHelpLogs
		) {

			LocalDate nextDate =
				chatHelpLog.getTimestamp ()
					.toDateTime (timezone)
					.toLocalDate ();

			if (
				notEqual (
					nextDate,
					previousDate)
			) {

				previousDate =
					nextDate;

				printFormat (
					"<tr class=\"sep\">\n");

				printFormat (
					"<tr style=\"font-weight: bold\">\n");

				printFormat (
					"<td colspan=\"5\">%h</td>\n",
					userConsoleLogic.dateStringLong (
						chatHelpLog.getTimestamp ()));

				printFormat (
					"</tr>\n");

			}

			String rowClass =
				messageConsoleLogic.classForMessageDirection (
					chatHelpLog.getDirection ());

			printFormat (
				"<tr class=\"%h\">\n",
				rowClass);

			printFormat (
				"<td style=\"background: %h\">&nbsp;</td>\n",
				Html.genHtmlColor (
					chatHelpLog.getOurNumber ()));

			printFormat (
				"<td>%h</td>\n",
				userConsoleLogic.timeString (
					chatHelpLog.getTimestamp ()));

			printFormat (
				"<td>%h</td>\n",
				chatHelpLog.getText ());

			printFormat (
				"<td>%h</td>\n",
				chatHelpLog.getOurNumber ());

			printFormat (
				"<td>%h</td>\n",
				chatHelpLog.getUser () == null
					? ""
					: chatHelpLog.getUser ().getUsername ());

			printFormat (
				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
