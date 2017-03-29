package wbs.apn.chat.help.console;

import static wbs.utils.etc.LogicUtils.booleanToYesNo;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElseEmDash;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.time.TimeUtils.localDateNotEqual;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteRaw;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowSeparatorWrite;
import static wbs.web.utils.HtmlUtils.htmlColourFromObject;

import java.util.Collection;

import lombok.NonNull;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.currency.logic.CurrencyLogic;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.sms.message.core.console.MessageConsoleLogic;

import wbs.apn.chat.bill.logic.ChatCreditCheckResult;
import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.core.console.ChatConsoleLogic;
import wbs.apn.chat.help.model.ChatHelpLogRec;
import wbs.apn.chat.scheme.model.ChatSchemeRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

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
	CurrencyLogic currencyLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MessageConsoleLogic messageConsoleLogic;

	@SingletonDependency
	ConsoleObjectManager objectManager;

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
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		chatHelpLog =
			chatHelpLogHelper.findFromContextRequired ();

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
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"renderHtmlBodyContent");

		renderDetails (
			taskLogger);

		renderHistory ();

	}

	private
	void renderDetails (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"renderDetails");

		htmlTableOpenDetails ();

		htmlTableDetailsRowWriteRaw (
			"User",
			() -> objectManager.writeTdForObjectMiniLink (
				taskLogger,
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
				taskLogger,
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
