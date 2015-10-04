package wbs.clients.apn.chat.supervisor.console;

import static wbs.framework.utils.etc.Misc.dateToInstant;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import wbs.clients.apn.chat.contact.model.ChatMessageObjectHelper;
import wbs.clients.apn.chat.contact.model.ChatMessageRec;
import wbs.clients.apn.chat.core.console.ChatConsoleLogic;
import wbs.clients.apn.chat.core.logic.ChatMiscLogic;
import wbs.clients.apn.chat.core.model.ChatObjectHelper;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.Html;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

@PrototypeComponent ("chatSupervisorMessagesPart")
public
class ChatSupervisorMessagesPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	ChatConsoleLogic chatConsoleLogic;

	@Inject
	ChatObjectHelper chatHelper;

	@Inject
	ChatMessageObjectHelper chatMessageHelper;

	@Inject
	ChatMiscLogic chatMiscLogic;

	@Inject
	TimeFormatter timeFormatter;

	@Inject
	UserObjectHelper userHelper;

	// state

	ChatRec chat;

	List<ChatMessageRec> chatMessages;

	// implementation

	@Override
	public
	void prepare () {

		chat =
			chatHelper.find (
				requestContext.stuffInt ("chatId"));

		int hour =
			Integer.parseInt (
				requestContext.parameter ("hour"));

		int senderUserId =
			Integer.parseInt (
				requestContext.parameter ("user_id"));

		LocalDate date =
			LocalDate.parse (
				requestContext.parameter ("date"));

		DateTime startTime =
			date.toDateTimeAtStartOfDay ().plusHours (hour);

		DateTime endTime =
			startTime.plusHours (1);

		UserRec senderUser =
			userHelper.find (
				senderUserId);

		chatMessages =
			chatMessageHelper.findBySenderAndTimestamp (
				chat,
				senderUser,
				new Interval (
					startTime,
					endTime));

		Collections.sort (
			chatMessages);

	}

	@Override
	public
	void renderHtmlBodyContent () {

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
					chatMiscLogic.timezone (
						chat),
					dateToInstant (
						chatMessage.getTimestamp ())),

				"<td>%h</td>\n",
				chatMessage.getOriginalText ().getText (),

				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}