package wbs.clients.apn.chat.supervisor.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.joda.time.Interval;

import com.google.common.collect.ImmutableSet;

import wbs.clients.apn.chat.contact.model.ChatMessageObjectHelper;
import wbs.clients.apn.chat.contact.model.ChatMessageRec;
import wbs.clients.apn.chat.core.console.ChatConsoleLogic;
import wbs.clients.apn.chat.core.logic.ChatMiscLogic;
import wbs.clients.apn.chat.core.model.ChatObjectHelper;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.console.html.MagicTableScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.TimeFormatter;
import wbs.platform.user.console.UserConsoleLogic;
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
	UserConsoleLogic userConsoleLogic;

	@Inject
	UserObjectHelper userHelper;

	// state

	ChatRec chat;

	List<ChatMessageRec> chatMessages;

	// details

	@Override
	public
	Set<ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				JqueryScriptRef.instance)

			.add (
				MagicTableScriptRef.instance)

			.build ();

	}

	// implementation

	@Override
	public
	void prepare () {

		chat =
			chatHelper.find (
				requestContext.stuffInt (
					"chatId"));

		Interval interval =
			timeFormatter.isoStringToInterval (
				requestContext.parameterOrNull (
					"interval"));

		int senderUserId =
			Integer.parseInt (
				requestContext.parameterOrNull (
					"user_id"));

		UserRec senderUser =
			userHelper.find (
				senderUserId);

		chatMessages =
			chatMessageHelper.findBySenderAndTimestamp (
				chat,
				senderUser,
				interval);

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

		for (
			ChatMessageRec chatMessage
				: chatMessages
		) {

			printFormat (
				"<tr",
				" class=\"magic-table-row\"",
				" data-target-href=\"%h\"",
				requestContext.resolveLocalUrl (
					stringFormat (
						"/chat.supervisorConversation",
						"?chatUserId1=%u",
						chatMessage.getToUser ().getId (),
						"&chatUserId2=%u",
						chatMessage.getFromUser ().getId ())),
				">\n");

			printFormat (
				"<td>%h</td>\n",
				chatConsoleLogic.textForChatUser (
					chatMessage.getFromUser ()));

			printFormat (
				"<td>%h</td>\n",
				chatConsoleLogic.textForChatUser (
					chatMessage.getToUser ()));

			printFormat (
				"<td>%h</td>\n",
				userConsoleLogic.timeString (
					chatMessage.getTimestamp ()));

			printFormat (
				"<td>%h</td>\n",
				chatMessage.getOriginalText ().getText ());

			printFormat (
				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}