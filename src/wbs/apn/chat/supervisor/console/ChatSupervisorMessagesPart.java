package wbs.apn.chat.supervisor.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.web.utils.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlDataAttribute;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import org.joda.time.Interval;

import wbs.console.html.MagicTableScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.part.AbstractPagePart;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

import wbs.utils.string.FormatWriter;
import wbs.utils.time.TimeFormatter;

import wbs.apn.chat.contact.console.ChatMessageConsoleHelper;
import wbs.apn.chat.contact.model.ChatMessageRec;
import wbs.apn.chat.core.console.ChatConsoleHelper;
import wbs.apn.chat.core.console.ChatConsoleLogic;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.core.model.ChatRec;

@PrototypeComponent ("chatSupervisorMessagesPart")
public
class ChatSupervisorMessagesPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatConsoleLogic chatConsoleLogic;

	@SingletonDependency
	ChatConsoleHelper chatHelper;

	@SingletonDependency
	ChatMessageConsoleHelper chatMessageHelper;

	@SingletonDependency
	ChatMiscLogic chatMiscLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

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
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			chat =
				chatHelper.findFromContextRequired (
					transaction);

			Interval interval =
				timeFormatter.isoStringToInterval (
					requestContext.parameterRequired (
						"interval"));

			UserRec senderUser =
				userHelper.findRequired (
					transaction,
					requestContext.parameterIntegerRequired (
						"user_id"));

			chatMessages =
				chatMessageHelper.findBySenderAndTimestamp (
					transaction,
					chat,
					senderUser,
					interval);

			Collections.sort (
				chatMessages);

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			// table open

			htmlTableOpenList (
				formatWriter);

			// table header

			htmlTableHeaderRowWrite (
				formatWriter,
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
					formatWriter,
					htmlClassAttribute (
						"magic-table-row"),
					htmlDataAttribute (
						"target-href",
						requestContext.resolveLocalUrlFormat (
							"/chat.supervisorConversation",
							"?chatUserId1=%u",
							integerToDecimalString (
								chatMessage.getToUser ().getId ()),
							"&chatUserId2=%u",
							integerToDecimalString (
								chatMessage.getFromUser ().getId ()))));

				htmlTableCellWrite (
					formatWriter,
					chatConsoleLogic.textForChatUser (
						chatMessage.getFromUser ()));

				htmlTableCellWrite (
					formatWriter,
					chatConsoleLogic.textForChatUser (
						chatMessage.getToUser ()));

				htmlTableCellWrite (
					formatWriter,
					userConsoleLogic.timeString (
						transaction,
						chatMessage.getTimestamp ()));

				htmlTableCellWrite (
					formatWriter,
					chatMessage.getOriginalText ().getText ());

				htmlTableRowClose (
					formatWriter);

			}

			// table close

			htmlTableClose (
				formatWriter);

		}

	}

}