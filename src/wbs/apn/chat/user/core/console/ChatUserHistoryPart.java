package wbs.apn.chat.user.core.console;

import static wbs.utils.etc.LogicUtils.comparableLessThan;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElseEmDash;
import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.joinWithoutSeparator;
import static wbs.utils.string.StringUtils.spacify;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.time.TimeUtils.localDateNotEqual;
import static wbs.web.utils.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlStyleAttribute;
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
import static wbs.web.utils.HtmlUtils.htmlEncodeNonBreakingWhitespace;

import java.util.List;

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

import wbs.utils.string.FormatWriter;
import wbs.utils.time.TimeFormatter;

import wbs.apn.chat.contact.logic.ChatMessageLogic;
import wbs.apn.chat.contact.model.ChatMessageObjectHelper;
import wbs.apn.chat.contact.model.ChatMessageRec;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserType;

@PrototypeComponent ("chatUserHistoryPart")
public
class ChatUserHistoryPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatMessageObjectHelper chatMessageHelper;

	@SingletonDependency
	ChatMessageLogic chatMessageLogic;

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	TimeFormatter timeFormatter;

	// state

	ChatUserRec chatUser;

	List <ChatMessageRec> chatMessages;

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

			Long chatMessageCount =
				chatMessageHelper.count (
					transaction,
					chatUser);

			chatMessages =
				chatMessageHelper.findLimit (
					transaction,
					chatUser,
					1000l);

			if (chatMessageCount > 1000l) {

				requestContext.addWarningFormat (
					"Only showing %s of %s total messages",
					integerToDecimalString (
						1000l),
					integerToDecimalString (
						chatMessages.size ()));

			}

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

			// table open

			htmlTableOpenList (
				formatWriter);

			// table header

			htmlTableHeaderRowWrite (
				formatWriter,
				"",
				"Time",
				"User",
				"Message",
				"Monitor");

			// table content

			DateTimeZone timezone =
				chatUserLogic.getTimezone (
					chatUser);

			LocalDate previousDate = null;

			for (
				ChatMessageRec chatMessage
					: chatMessages
			) {

				ChatUserRec fromUser =
					chatMessage.getFromUser ();

				ChatUserRec toUser =
					chatMessage.getToUser ();

				ChatUserRec otherUser =
					fromUser != chatUser
						? fromUser : toUser;

				String otherUserId =
					otherUser.getName () != null
						? stringFormat (
							"%s %s",
							otherUser.getCode (),
							otherUser.getName ())
						: otherUser.getCode ();

				LocalDate nextDate =
					chatMessage.getTimestamp ()

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
							chatMessage.getTimestamp ()),
						htmlColumnSpanAttribute (5l));

					htmlTableRowClose (
						formatWriter);

				}

				String rowClass =
					chatMessage.getFromUser () == chatUser
						? "message-in"
						: "message-out";

				String colour =
					htmlColourFromObject (
						ifThenElse (
							comparableLessThan (
								fromUser.getCode (),
								toUser.getCode ()),

					() -> joinWithoutSeparator (
						fromUser.getCode (),
						toUser.getCode ()),

					() -> joinWithoutSeparator (
						toUser.getCode (),
						fromUser.getCode ())

				));

				htmlTableRowOpen (
					formatWriter,
					htmlClassAttribute (
						rowClass));

				htmlTableCellWrite (
					formatWriter,
					"",
					htmlStyleAttribute (
						htmlStyleRuleEntry (
							"background-color",
							colour)));

				htmlTableCellWrite (
					formatWriter,
					timeFormatter.timeString (
						chatUserLogic.getTimezone (
							chatUser),
						chatMessage.getTimestamp ()));

				htmlTableCellWriteHtml (
					formatWriter,
					htmlEncodeNonBreakingWhitespace (
						otherUserId));

				htmlTableCellWrite (
					formatWriter,
					spacify (
						chatMessage.getOriginalText ().getText ()));

				if (fromUser.getType () == ChatUserType.monitor) {

					htmlTableCellWrite (
						formatWriter,
						ifNotNullThenElseEmDash (
							chatMessage.getSender (),
							() -> chatMessage.getSender ().getUsername ()));

				} else if (toUser.getType () == ChatUserType.monitor) {

					htmlTableCellWrite (
						formatWriter,
						"(yes)");

				} else {

					htmlTableCellWrite (
						formatWriter,
						"");

				}

				htmlTableRowClose (
					formatWriter);

			}

			// table close

			htmlTableClose (
				formatWriter);

		}

	}

}
