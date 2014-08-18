package wbs.apn.chat.supervisor.console;

import static wbs.framework.utils.etc.Misc.anyOf;
import static wbs.framework.utils.etc.Misc.dateToInstant;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.Misc.spacify;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import wbs.apn.chat.contact.model.ChatMessageObjectHelper;
import wbs.apn.chat.contact.model.ChatMessageRec;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.core.model.ChatObjectHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.scheme.model.ChatSchemeRec;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.media.console.MediaConsoleLogic;

@PrototypeComponent ("chatSupervisorConversationPart")
public
class ChatSupervisorConversationPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	ChatObjectHelper chatHelper;

	@Inject
	ChatMessageObjectHelper chatMessageHelper;

	@Inject
	ChatMiscLogic chatMiscLogic;

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	MediaConsoleLogic mediaConsoleLogic;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	TimeFormatter timeFormatter;

	// state

	DateTimeZone timeZone;

	ChatRec chat;
	ChatUserRec userChatUser;
	ChatUserRec monitorChatUser;
	List<ChatMessageRec> chatMessages;

	// implementation

	@Override
	public
	void prepare () {

		chat =
			chatHelper.find (
				requestContext.stuffInt (
					"chatId"));

		int chatUserId1 =
			Integer.parseInt (
				requestContext.parameter (
					"chatUserId1"));

		int chatUserId2 =
			Integer.parseInt (
				requestContext.parameter (
					"chatUserId2"));

		userChatUser =
			chatUserHelper.find (
				chatUserId1);

		monitorChatUser =
			chatUserHelper.find (
				chatUserId2);

		if (
			anyOf (
				notEqual (
					userChatUser.getChat (),
					chat),
				notEqual (
					monitorChatUser.getChat (),
					chat))
		) {

			throw new RuntimeException (
				stringFormat (
					"Chat users %s and %s do not match chat %s",
					userChatUser.getId (),
					monitorChatUser.getId (),
					chat.getId ()));

		}

		ChatSchemeRec chatScheme =
			userChatUser.getChatScheme ();

		timeZone =
			DateTimeZone.forID (
				chatScheme.getTimezone ());

		chatMessages =
			new ArrayList<ChatMessageRec> ();

		chatMessages.addAll (
			chatMessageHelper.find (
				userChatUser,
				monitorChatUser));

		chatMessages.addAll (
			chatMessageHelper.find (
				monitorChatUser,
				userChatUser));

		Collections.sort (
			chatMessages);

	}

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",
			"<th>Party</th>\n",
			"<td><strong>monitor</strong></td>\n",
			"<td><strong>user</strong></td>\n",
			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>User number</th>\n",

			"%s\n",
			objectManager.tdForObject (
				monitorChatUser,
				chat,
				true,
				true),

			"%s\n",
			objectManager.tdForObject (
				userChatUser,
				chat,
				true,
				true),

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Name</th>\n",

			"<td>%h</td>\n",
			monitorChatUser.getName (),

			"<td>%h</td>\n",
			userChatUser.getName (),

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Info</th>\n",

			"<td>%h</td>\n",
			monitorChatUser.getInfoText (),

			"<td>%h</td>\n",
			userChatUser.getInfoText (),

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Pic</th>\n",

			"<td>%s</td>\n",
			monitorChatUser.getChatUserImageList ().isEmpty ()
				? "-"
				: mediaConsoleLogic.mediaThumb100 (
					monitorChatUser.getChatUserImageList ().get (0).getMedia ()),

			"<td>%s</td>\n",
			userChatUser.getChatUserImageList ().isEmpty ()
				? "-"
				: mediaConsoleLogic.mediaThumb100 (
					userChatUser.getChatUserImageList ().get (0).getMedia ()),

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Location</th>\n",

			"<td>%h</td>\n",
			monitorChatUser.getLocPlace (),

			"<td>%h</td>\n",
			userChatUser.getLocPlace (),

			"</tr>\n");

		printFormat (
			"</table>\n");

		printFormat (
			"<h2>History</h2>\n");

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>Time</th>\n",
			"<th>Message</th>\n",
			"<th>Monitor</th>\n",
			"</tr>\n");

		DateTimeZone timezone =
			chatMiscLogic.timezone (
				chat);

		LocalDate previousDate = null;

		for (ChatMessageRec chatMessage
				: chatMessages) {

			LocalDate nextDate =
				dateToInstant (
					chatMessage.getTimestamp ())
				.toDateTime (
					timezone)
				.toLocalDate ();

			if (
				notEqual (
					nextDate,
					previousDate)
			) {

				printFormat (
					"<tr class=\"sep\">\n");

				printFormat (
					"<tr style=\"font-weight: bold\">\n",

					"<td colspan=\"3\">%h</td>\n",
					timeFormatter.instantToDateStringLong (
						timeZone,
						dateToInstant (
							chatMessage.getTimestamp ())),

					"</td>\n",

					"</tr>\n");

			}

			printFormat (
				"<tr class=\"%h\">\n",
				chatMessage.getFromUser () == userChatUser
					? "message-in"
					: "message-out");

			printFormat (
				"<td>%h</td>\n",
				timeFormatter.instantToTimeString (
					timeZone,
					dateToInstant (
						chatMessage.getTimestamp ())));

			printFormat (
				"<td>%h</td>\n",
				chatMessage.getEditedText () != null
					? spacify (
						chatMessage.getEditedText ().getText ())
					: "-");

			printFormat (
				"<td>%h</td>\n",
				chatMessage.getSender () != null
					? chatMessage.getSender ().getUsername ()
					: "-");

			printFormat (
				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
