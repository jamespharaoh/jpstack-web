package wbs.clients.apn.chat.supervisor.console;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import org.joda.time.Interval;

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
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.utils.TimeFormatter;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

@PrototypeComponent ("chatSupervisorMessagesPart")
public
class ChatSupervisorMessagesPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatConsoleLogic chatConsoleLogic;

	@SingletonDependency
	ChatObjectHelper chatHelper;

	@SingletonDependency
	ChatMessageObjectHelper chatMessageHelper;

	@SingletonDependency
	ChatMiscLogic chatMiscLogic;

	@SingletonDependency
	TimeFormatter timeFormatter;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserObjectHelper userHelper;

	// state

	ChatRec chat;

	List <ChatMessageRec> chatMessages;

	// details

	@Override
	public
	Set <ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef> builder ()

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
			chatHelper.findRequired (
				requestContext.stuffInteger (
					"chatId"));

		Interval interval =
			timeFormatter.isoStringToInterval (
				requestContext.parameterRequired (
					"interval"));

		UserRec senderUser =
			userHelper.findRequired (
				requestContext.parameterIntegerRequired (
					"user_id"));

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