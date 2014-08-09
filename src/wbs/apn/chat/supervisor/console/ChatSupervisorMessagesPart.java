package wbs.apn.chat.supervisor.console;

import static wbs.framework.utils.etc.Misc.dateToInstant;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.joda.time.Interval;

import wbs.apn.chat.contact.model.ChatMessageObjectHelper;
import wbs.apn.chat.contact.model.ChatMessageRec;
import wbs.apn.chat.core.console.ChatConsoleLogic;
import wbs.apn.chat.core.model.ChatObjectHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.Html;
import wbs.platform.console.html.ObsoleteDateField;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

@PrototypeComponent ("chatSupervisorMessagesPart")
public
class ChatSupervisorMessagesPart
	extends AbstractPagePart {

	@Inject
	ChatConsoleLogic chatConsoleLogic;

	@Inject
	ChatObjectHelper chatHelper;

	@Inject
	ChatMessageObjectHelper chatMessageHelper;

	@Inject
	TimeFormatter timeFormatter;

	@Inject
	UserObjectHelper userHelper;

	List<ChatMessageRec> chatMessages;

	@Override
	public
	void prepare () {

		ChatRec chat =
			chatHelper.find (
				requestContext.stuffInt ("chatId"));

		ObsoleteDateField dateField =
			ObsoleteDateField.parse (
				requestContext.parameter ("date"));

		int hour =
			Integer.parseInt (
				requestContext.parameter ("hour"));

		int senderUserId =
			Integer.parseInt (
				requestContext.parameter ("user_id"));

		Calendar calendar =
			Calendar.getInstance ();

		calendar.setTime (
			dateField.date);

		calendar.add (
			Calendar.HOUR,
			hour);

		Date startTime =
			calendar.getTime ();

		calendar.add (
			Calendar.HOUR,
			1);

		Date endTime =
			calendar.getTime ();

		UserRec senderUser =
			userHelper.find (
				senderUserId);

		chatMessages =
			chatMessageHelper.findBySenderAndTimestamp (
				chat,
				senderUser,
				new Interval (
					dateToInstant (startTime),
					dateToInstant (endTime)));

		Collections.sort (
			chatMessages);

	}

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<table class=\"list\">");

		printFormat (
			"<tr>\n",
			"<th>From</th>\n",
			"<th>To</th>\n",
			"<th>Time</th>\n",
			"<th>Message</th>\n",
			"</tr>\n");

		for (ChatMessageRec chatMessage
				: chatMessages) {

			printFormat (
				"%s\n",
				Html.magicTr (
					requestContext.resolveLocalUrl (
						stringFormat (
							"/chat.supervisorConversation",
							"?chatUserId1=%u",
							chatMessage.getToUser ().getId (),
							"&chatUserId2=%u",
							chatMessage.getFromUser ().getId ())),
					false),

				"<td>%h</td>\n",
				chatConsoleLogic.textForChatUser (
					chatMessage.getFromUser ()),

				"<td>%h</td>\n",
				chatConsoleLogic.textForChatUser (
					chatMessage.getToUser ()),

				"<td>%h</td>\n",
				timeFormatter.instantToTimeString (
					dateToInstant (chatMessage.getTimestamp ())),

				"<td>%h</td>\n",
				chatMessage.getOriginalText ().getText (),

				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}