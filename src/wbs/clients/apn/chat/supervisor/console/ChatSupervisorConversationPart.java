package wbs.clients.apn.chat.supervisor.console;

import static wbs.framework.utils.etc.LogicUtils.anyOf;
import static wbs.framework.utils.etc.LogicUtils.referenceNotEqualSafe;
import static wbs.framework.utils.etc.StringUtils.emptyStringIfNull;
import static wbs.framework.utils.etc.StringUtils.spacify;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.TimeUtils.localDateNotEqual;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import wbs.clients.apn.chat.contact.model.ChatMessageObjectHelper;
import wbs.clients.apn.chat.contact.model.ChatMessageRec;
import wbs.clients.apn.chat.core.logic.ChatMiscLogic;
import wbs.clients.apn.chat.core.model.ChatObjectHelper;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.scheme.model.ChatSchemeRec;
import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.TimeFormatter;
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

	DateTimeZone chatTimezone;

	ChatRec chat;
	ChatUserRec userChatUser;
	ChatUserRec monitorChatUser;
	List<ChatMessageRec> chatMessages;

	// implementation

	@Override
	public
	void prepare () {

		chat =
			chatHelper.findRequired (
				requestContext.stuffInteger (
					"chatId"));

		long chatUserId1 =
			requestContext.parameterIntegerRequired (
				"chatUserId1");

		long chatUserId2 =
			requestContext.parameterIntegerRequired (
				"chatUserId2");

		userChatUser =
			chatUserHelper.findRequired (
				chatUserId1);

		monitorChatUser =
			chatUserHelper.findRequired (
				chatUserId2);

		if (anyOf (

			() -> referenceNotEqualSafe (
				userChatUser.getChat (),
				chat),

			() -> referenceNotEqualSafe (
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

		chatTimezone =
			timeFormatter.timezone (
				chatScheme.getTimezone ());

		chatMessages =
			new ArrayList<ChatMessageRec> ();

		chatMessages.addAll (
			chatMessageHelper.findFromTo (
				userChatUser,
				monitorChatUser));

		chatMessages.addAll (
			chatMessageHelper.findFromTo (
				monitorChatUser,
				userChatUser));

		Collections.sort (
			chatMessages);

	}

	@Override
	public
	void renderHtmlBodyContent () {

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
			objectManager.tdForObjectMiniLink (
				monitorChatUser,
				chat),

			"%s\n",
			objectManager.tdForObjectMiniLink (
				userChatUser,
				chat),

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Name</th>\n",

			"<td>%h</td>\n",
			emptyStringIfNull (
				monitorChatUser.getName ()),

			"<td>%h</td>\n",
			emptyStringIfNull (
				userChatUser.getName ()),

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
			emptyStringIfNull (
				monitorChatUser.getLocationPlace ()),

			"<td>%h</td>\n",
			emptyStringIfNull (
				userChatUser.getLocationPlace ()),

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

		for (
			ChatMessageRec chatMessage
				: chatMessages
		) {

			LocalDate nextDate =
				chatMessage.getTimestamp ()

				.toDateTime (
					timezone)

				.toLocalDate ();

			if (
				localDateNotEqual (
					nextDate,
					previousDate)
			) {

				printFormat (
					"<tr class=\"sep\">\n");

				printFormat (
					"<tr style=\"font-weight: bold\">\n",

					"<td colspan=\"3\">%h</td>\n",
					timeFormatter.dateStringLong (
						chatTimezone,
						chatMessage.getTimestamp ()),

					"</td>\n",

					"</tr>\n");

				previousDate =
					nextDate;

			}

			printFormat (
				"<tr class=\"%h\">\n",
				chatMessage.getFromUser () == userChatUser
					? "message-in"
					: "message-out");

			printFormat (
				"<td>%h</td>\n",
				timeFormatter.timeString (
					chatTimezone,
					chatMessage.getTimestamp ()));

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
