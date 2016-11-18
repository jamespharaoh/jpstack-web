package wbs.apn.chat.user.core.console;

import static wbs.utils.etc.LogicUtils.comparableLessThan;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElseEmDash;
import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.Misc.isNull;
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

import java.util.List;

import lombok.NonNull;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

import wbs.utils.time.TimeFormatter;

import wbs.apn.chat.contact.logic.ChatMessageLogic;
import wbs.apn.chat.contact.model.ChatMessageObjectHelper;
import wbs.apn.chat.contact.model.ChatMessageRec;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserType;
import wbs.web.utils.HtmlUtils;

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

	@SingletonDependency
	TimeFormatter timeFormatter;

	// state

	ChatUserRec chatUser;

	List <ChatMessageRec> chatMessages;

	// implementation

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		chatUser =
			chatUserHelper.findRequired (
				requestContext.stuffInteger (
					"chatUserId"));

		Long chatMessageCount =
			chatMessageHelper.count (
				chatUser);

		chatMessages =
			chatMessageHelper.findLimit (
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

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		// table open

		htmlTableOpenList ();

		// table header

		htmlTableHeaderRowWrite (
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

				htmlTableRowSeparatorWrite ();

				htmlTableRowOpen (
					htmlStyleAttribute (
						htmlStyleRuleEntry (
							"font-weight",
							"bold")));

				htmlTableCellWrite (
					timeFormatter.dateStringLong (
						chatUserLogic.getTimezone (
							chatUser),
						chatMessage.getTimestamp ()),
					htmlColumnSpanAttribute (5l));

				htmlTableRowClose ();

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
				htmlClassAttribute (
					rowClass));

			htmlTableCellWrite (
				"",
				htmlStyleAttribute (
					htmlStyleRuleEntry (
						"background-color",
						colour)));

			htmlTableCellWrite (
				timeFormatter.timeString (
					chatUserLogic.getTimezone (
						chatUser),
					chatMessage.getTimestamp ()));

			htmlTableCellWriteHtml (
				HtmlUtils.htmlNonBreakingWhitespace (
					HtmlUtils.htmlEncode (
						otherUserId)));

			htmlTableCellWrite (
				spacify (
					chatMessage.getOriginalText ().getText ()));

			if (fromUser.getType () == ChatUserType.monitor) {

				htmlTableCellWrite (
					ifNotNullThenElseEmDash (
						chatMessage.getSender (),
						() -> chatMessage.getSender ().getUsername ()));

			} else if (toUser.getType () == ChatUserType.monitor) {

				htmlTableCellWrite (
					"(yes)");

			} else {

				htmlTableCellWrite (
					"");

			}

			htmlTableRowClose ();

		}

		// table close

		htmlTableClose ();

	}

}
