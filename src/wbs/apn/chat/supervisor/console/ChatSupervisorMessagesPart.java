package wbs.apn.chat.supervisor.console;

import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.utils.web.HtmlAttributeUtils.htmlDataAttribute;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenList;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import org.joda.time.Interval;

import wbs.apn.chat.contact.model.ChatMessageObjectHelper;
import wbs.apn.chat.contact.model.ChatMessageRec;
import wbs.apn.chat.core.console.ChatConsoleLogic;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.core.model.ChatObjectHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.console.html.MagicTableScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;
import wbs.utils.time.TimeFormatter;

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

		// table open

		htmlTableOpenList ();

		// table header

		htmlTableHeaderRowWrite (
			"From",
			"To",
			"Time",
			"Message");

		// table content

		for (
			ChatMessageRec chatMessage
				: chatMessages
		) {

			htmlTableRowOpen (
				htmlClassAttribute (
					"magic-table-row"),
				htmlDataAttribute (
					"target-href",
					requestContext.resolveLocalUrl (
						stringFormat (
							"/chat.supervisorConversation",
							"?chatUserId1=%u",
							chatMessage.getToUser ().getId (),
							"&chatUserId2=%u",
							chatMessage.getFromUser ().getId ()))));

			htmlTableCellWrite (
				chatConsoleLogic.textForChatUser (
					chatMessage.getFromUser ()));

			htmlTableCellWrite (
				chatConsoleLogic.textForChatUser (
					chatMessage.getToUser ()));

			htmlTableCellWrite (
				userConsoleLogic.timeString (
					chatMessage.getTimestamp ()));

			htmlTableCellWrite (
				chatMessage.getOriginalText ().getText ());

			htmlTableRowClose ();

		}

		// table close

		htmlTableClose ();

	}

}