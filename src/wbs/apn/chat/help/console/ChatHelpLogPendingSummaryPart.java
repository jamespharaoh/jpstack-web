package wbs.apn.chat.help.console;

import static wbs.utils.etc.LogicUtils.booleanToYesNo;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElseEmDash;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.time.TimeUtils.localDateNotEqual;
import static wbs.utils.web.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWriteRaw;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenList;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowSeparatorWrite;
import static wbs.utils.web.HtmlUtils.htmlColourFromObject;

import java.util.Collection;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import wbs.apn.chat.bill.logic.ChatCreditCheckResult;
import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.core.console.ChatConsoleLogic;
import wbs.apn.chat.help.model.ChatHelpLogRec;
import wbs.apn.chat.scheme.model.ChatSchemeRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.platform.currency.logic.CurrencyLogic;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.sms.message.core.console.MessageConsoleLogic;

@PrototypeComponent ("chatHelpLogPendingSummaryPart")
public
class ChatHelpLogPendingSummaryPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatConsoleLogic chatConsoleLogic;

	@SingletonDependency
	ChatCreditLogic chatCreditLogic;

	@SingletonDependency
	ChatHelpLogConsoleHelper chatHelpLogHelper;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	CurrencyLogic currencyLogic;

	@SingletonDependency
	MessageConsoleLogic messageConsoleLogic;

	@SingletonDependency
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
			chatHelpLogHelper.findRequired (
				requestContext.stuffInteger (
					"chatHelpLogId"));

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

		renderDetails ();

		renderHistory ();

	}

	private
	void renderDetails () {

		htmlTableOpenDetails ();

		htmlTableDetailsRowWriteRaw (
			"User",
			() -> objectManager.writeTdForObjectMiniLink (
				chatUser,
				chatUser.getChat ()));

		htmlTableDetailsRowWrite (
			"Date mode",
			chatUser.getDateMode ().name ());

		htmlTableDetailsRowWrite (
			"Online",
			booleanToYesNo (
				chatUser.getOnline ()));

		htmlTableDetailsRowWrite (
			"Gender",
			ifNotNullThenElseEmDash (
				chatUser.getGender (),
				() -> chatUser.getGender ().name ()));

		htmlTableDetailsRowWrite (
			"Orientation",
			ifNotNullThenElseEmDash (
				chatUser.getOrient (),
				() -> chatUser.getOrient ().name ()));

		htmlTableDetailsRowWrite (
			"Name",
			ifNotNullThenElseEmDash (
				chatUser.getName (),
				() -> chatUser.getName ()));

		htmlTableDetailsRowWrite (
			"Info",
			ifNotNullThenElseEmDash (
				chatUser.getInfoText (),
				() -> chatUser.getInfoText ().getText ()));

		htmlTableDetailsRowWriteHtml (
			"Credit",
			currencyLogic.formatHtml (
				chatUser.getChat ().getCurrency (),
				chatUser.getCredit ()));

		htmlTableDetailsRowWrite (
			"Barred",
			booleanToYesNo (
				chatUser.getBarred ()));

		htmlTableDetailsRowWrite (
			"Credit mode",
			chatUser.getCreditMode ().name ());

		ChatCreditCheckResult creditCheckResult =
			chatCreditLogic.userCreditCheck (
				chatUser);

		htmlTableDetailsRowWrite (
			"Credit check",
			creditCheckResult.details ());

		htmlTableClose ();

	}

	private
	void renderHistory () {

		htmlHeadingTwoWrite (
			"History");

		htmlTableOpenList ();

		htmlTableHeaderRowWrite (
			"",
			"Time",
			"Text",
			"Our number",
			"User");

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

				isNull (
					previousDate)

				|| localDateNotEqual (
					nextDate,
					previousDate)

			) {

				previousDate =
					nextDate;

				htmlTableRowSeparatorWrite ();

				formatWriter.writeLineFormat (
					"<tr style=\"font-weight: bold\">");

				formatWriter.writeLineFormat (
					"<td colspan=\"5\">%h</td>",
					userConsoleLogic.dateStringLong (
						chatHelpLog.getTimestamp ()));

				htmlTableRowClose ();

			}

			String rowClass =
				messageConsoleLogic.classForMessageDirection (
					chatHelpLog.getDirection ());

			formatWriter.writeLineFormat (
				"<tr class=\"%h\">",
				rowClass);

			formatWriter.writeLineFormat (
				"<td",
				" style=\"background: %h\"",
				htmlColourFromObject (
					chatHelpLog.getOurNumber ()),
				">&nbsp;</td>");

			htmlTableCellWrite (
				userConsoleLogic.timeString (
					chatHelpLog.getTimestamp ()));

			htmlTableCellWrite (
				chatHelpLog.getText ());

			htmlTableCellWrite (
				chatHelpLog.getOurNumber ());

			htmlTableCellWrite (
				ifNotNullThenElseEmDash (
					chatHelpLog.getUser (),
					() -> chatHelpLog.getUser ().getUsername ()));

			htmlTableRowClose ();

		}

		htmlTableClose ();

	}

}
