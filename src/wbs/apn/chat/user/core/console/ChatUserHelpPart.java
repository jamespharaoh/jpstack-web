package wbs.apn.chat.user.core.console;

import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElseEmDash;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.time.TimeUtils.localDateNotEqual;
import static wbs.web.utils.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlStyleAttribute;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWrite;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleRuleEntry;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowSeparatorWrite;
import static wbs.web.utils.HtmlUtils.htmlColourFromObject;

import java.util.Set;
import java.util.TreeSet;

import lombok.NonNull;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import wbs.console.part.AbstractPagePart;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.sms.message.core.console.MessageConsoleLogic;

import wbs.utils.string.FormatWriter;
import wbs.utils.time.TimeFormatter;

import wbs.apn.chat.help.model.ChatHelpLogRec;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;

@PrototypeComponent ("chatUserHelpPart")
public
class ChatUserHelpPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MessageConsoleLogic messageConsoleLogic;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	TimeFormatter timeFormatter;

	// state

	ChatUserRec chatUser;

	Set <ChatHelpLogRec> chatHelpLogs;

	// implementation

	@Override
	public
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			chatUser =
				chatUserHelper.findFromContextRequired (
					transaction);

			chatHelpLogs =
				new TreeSet<> (
					chatUser.getChatHelpLogs ());

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			String link =
				requestContext.resolveLocalUrl (
					"/chatUser.helpForm");

			htmlParagraphOpen (
				formatWriter);

			formatWriter.writeFormat (
				"<button",
				" onclick=\"%h\"",
				stringFormat (
					"top.frames['inbox'].location='%j';",
					link),
				">send message</button>");

			htmlParagraphClose (
				formatWriter);

			if (
				collectionIsEmpty (
					chatHelpLogs)
			) {

				htmlParagraphWrite (
					formatWriter,
					"No history to display.");

				return;

			}

			// table open

			htmlTableOpenList (
				formatWriter);

			// table headers

			htmlTableHeaderRowWrite (
				formatWriter,
				"",
				"Time",
				"Message",
				"Our number",
				"User");

			// table content

			LocalDate previousDate = null;

			DateTimeZone timezone =
				chatUserLogic.getTimezone (
					chatUser);

			for (
				ChatHelpLogRec chatHelpLog
					: chatHelpLogs
			) {

				LocalDate nextDate =
					chatHelpLog.getTimestamp ()

					.toDateTime (
						timezone)

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

					htmlTableRowSeparatorWrite (
						formatWriter);

					htmlTableRowOpen (
						formatWriter,
						htmlStyleAttribute (
							htmlStyleRuleEntry (
								"font-weight",
								"bold")));

					htmlTableCellWrite (
						formatWriter,
						timeFormatter.dateStringLong (
							chatUserLogic.getTimezone (
								chatUser),
							chatHelpLog.getTimestamp ()),
						htmlColumnSpanAttribute (5l));

					htmlTableRowClose (
						formatWriter);

				}

				String rowClass =
					messageConsoleLogic.classForMessageDirection (
						chatHelpLog.getDirection ());

				htmlTableRowOpen (
					formatWriter,
					htmlClassAttribute (
						rowClass));

				htmlTableCellWriteHtml (
					formatWriter,
					"&nbsp;",
					htmlStyleAttribute (
						htmlStyleRuleEntry (
							"background",
							htmlColourFromObject (
								ifNull (
									chatHelpLog.getOurNumber (),
									0)))));

				htmlTableCellWrite (
					formatWriter,
					timeFormatter.timeString (
						chatUserLogic.getTimezone (
							chatUser),
						chatHelpLog.getTimestamp ()));


				htmlTableCellWrite (
					formatWriter,
					chatHelpLog.getText ());

				htmlTableCellWrite (
					formatWriter,
					chatHelpLog.getOurNumber ());

				htmlTableCellWrite (
					formatWriter,
					ifNotNullThenElseEmDash (
						chatHelpLog.getUser (),
					() ->
						chatHelpLog.getUser ().getUsername ()));

				htmlTableRowClose (
					formatWriter);

			}

			htmlTableClose (
				formatWriter);

		}

	}

}
