package wbs.clients.apn.chat.user.core.console;

import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.StringUtils.joinWithoutSeparator;
import static wbs.framework.utils.etc.StringUtils.spacify;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.TimeUtils.localDateNotEqual;

import java.util.List;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import wbs.clients.apn.chat.contact.logic.ChatMessageLogic;
import wbs.clients.apn.chat.contact.model.ChatMessageObjectHelper;
import wbs.clients.apn.chat.contact.model.ChatMessageRec;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.ChatUserType;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.utils.TimeFormatter;
import wbs.framework.utils.etc.Html;

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
	void prepare () {

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

			requestContext.addWarning (
				stringFormat (
					"Only showing %s of %s total messages",
					1000,
					chatMessages.size ()));

		}

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>&nbsp;</th>\n",
			"<th>Time</th>\n",
			"<th>User</th>\n",
			"<th>Message</th>\n",
			"<th>Monitor</th>\n",
			"</tr>\n");

		DateTimeZone timezone =
			chatUserLogic.getTimezone (
				chatUser);

		LocalDate previousDate = null;

		for (ChatMessageRec chatMessage
				: chatMessages) {

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

				printFormat (
					"<tr class=\"sep\">\n");

				printFormat (
					"<tr style=\"font-weight: bold\">\n",

					"<td colspan=\"5\">%h</td>\n",
					timeFormatter.dateStringLong (
						chatUserLogic.getTimezone (
							chatUser),
						chatMessage.getTimestamp ()),

					"</tr>\n");

			}

			String rowClass =
				chatMessage.getFromUser () == chatUser
					? "message-in"
					: "message-out";

			String color =
				Html.genHtmlColor (
					fromUser.getCode ().compareTo (toUser.getCode ()) < 0

					? joinWithoutSeparator (
						fromUser.getCode (),
						toUser.getCode ())

					: joinWithoutSeparator (
						toUser.getCode (),
						fromUser.getCode ()));

			printFormat (
				"<tr class=\"%h\">\n",
				rowClass,

				"<td style=\"background-color: %h\">&nbsp;</td>\n",
				color,

				"<td>%h</td>\n",
				timeFormatter.timeString (
					chatUserLogic.getTimezone (
						chatUser),
					chatMessage.getTimestamp ()),

				"<td>%s</td>\n",
				Html.nonBreakingWhitespace (
					Html.encode (
						otherUserId)),

				"<td>%h</td>\n",
				spacify (
					chatMessage.getOriginalText ().getText ()));

			if (fromUser.getType () == ChatUserType.monitor) {

				printFormat (
					"<td>%h</td>\n",
					chatMessage.getSender () != null
						? chatMessage.getSender ().getUsername ()
						: "-");

			} else if (toUser.getType () == ChatUserType.monitor) {

				printFormat (
					"<td>(yes)</td>\n");

			} else {

				printFormat (
					"<td>&nbsp;</td>\n");

			}

			printFormat (
				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
