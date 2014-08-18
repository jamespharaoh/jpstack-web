package wbs.apn.chat.user.core.console;

import static wbs.framework.utils.etc.Misc.dateToInstant;
import static wbs.framework.utils.etc.Misc.joinWithoutSeparator;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.Misc.spacify;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import wbs.apn.chat.contact.logic.ChatMessageLogic;
import wbs.apn.chat.contact.model.ChatMessageObjectHelper;
import wbs.apn.chat.contact.model.ChatMessageRec;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserType;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.Html;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.part.AbstractPagePart;

@PrototypeComponent ("chatUserHistoryPart")
public
class ChatUserHistoryPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	ChatMessageObjectHelper chatMessageHelper;

	@Inject
	ChatMessageLogic chatMessageLogic;

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	TimeFormatter timeFormatter;

	ChatUserRec chatUser;
	List<ChatMessageRec> chatMessages;

	// implementation

	@Override
	public
	void prepare () {

		chatUser =
			chatUserHelper.find (
				requestContext.stuffInt (
					"chatUserId"));

		int chatMessageCount =
			chatMessageHelper.count (
				chatUser);

		chatMessages =
			chatMessageHelper.findLimit (
				chatUser,
				1000);

		if (chatMessageCount > 1000) {

			requestContext.addWarning (
				stringFormat (
					"Only showing %s of %s total messages",
					1000,
					chatMessages.size ()));

		}

	}

	// TODO move this somewhere
	public
	String genHtmlColor (
			String source) {

		return String.format (
			"#%06x",
			source.hashCode () & 0x00ffffff);

	}

	@Override
	public
	void goBodyStuff () {

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
			chatUserLogic.timezone (
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
				dateToInstant (chatMessage.getTimestamp ())
					.toDateTime (timezone)
					.toLocalDate ();

			if (
				notEqual (
					nextDate, previousDate)
			) {

				previousDate =
					nextDate;

				printFormat (
					"<tr class=\"sep\">\n");

				printFormat (
					"<tr style=\"font-weight: bold\">\n",

					"<td colspan=\"5\">%h</td>\n",
					timeFormatter.instantToDateStringLong (
						chatUserLogic.timezone (
							chatUser),
						dateToInstant (
							chatMessage.getTimestamp ())),

					"</tr>\n");

			}

			String rowClass =
				chatMessage.getFromUser () == chatUser
					? "message-in"
					: "message-out";

			String color =
				genHtmlColor (
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
				timeFormatter.instantToTimeString (
					chatUserLogic.timezone (
						chatUser),
					dateToInstant (
						chatMessage.getTimestamp ())),

				"<td>%s</td>\n",
				Html.nbsp (
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
