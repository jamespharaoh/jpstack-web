package wbs.apn.chat.supervisor.console;

import static wbs.utils.etc.LogicUtils.anyOf;
import static wbs.utils.etc.LogicUtils.ifNotEmptyThenElse;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElseEmDash;
import static wbs.utils.etc.LogicUtils.ifNullThenEmDash;
import static wbs.utils.etc.LogicUtils.referenceNotEqualWithClass;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.spacify;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.time.TimeUtils.localDateNotEqual;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowSeparatorWrite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.NonNull;

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
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.utils.time.TimeFormatter;

@PrototypeComponent ("chatSupervisorConversationPart")
public
class ChatSupervisorConversationPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatObjectHelper chatHelper;

	@SingletonDependency
	ChatMessageObjectHelper chatMessageHelper;

	@SingletonDependency
	ChatMiscLogic chatMiscLogic;

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	MediaConsoleLogic mediaConsoleLogic;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	TimeFormatter timeFormatter;

	// state

	DateTimeZone chatTimezone;

	ChatRec chat;
	ChatUserRec userChatUser;
	ChatUserRec monitorChatUser;
	List <ChatMessageRec> chatMessages;

	// implementation

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

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

			() ->
				referenceNotEqualWithClass (
					ChatRec.class,
					userChatUser.getChat (),
					chat),

			() ->
				referenceNotEqualWithClass (
					ChatRec.class,
					monitorChatUser.getChat (),
					chat))

		) {

			throw new RuntimeException (
				stringFormat (
					"Chat users %s and %s do not match chat %s",
					integerToDecimalString (
						userChatUser.getId ()),
					integerToDecimalString (
						monitorChatUser.getId ()),
					integerToDecimalString (
						chat.getId ())));

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
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		renderDetails ();

		renderHistory ();

	}

	private
	void renderDetails () {

		htmlTableOpenDetails ();

		// party

		htmlTableHeaderRowHtml (
			"Party",
			"<strong>monitor</strong>",
			"<strong>user</strong>");

		htmlTableRowOpen ();

		htmlTableHeaderCellWrite (
			"User number");

		objectManager.writeTdForObjectMiniLink (
			monitorChatUser,
			chat);

		objectManager.writeTdForObjectMiniLink (
			userChatUser,
			chat);

		htmlTableRowClose ();

		// name

		htmlTableRowOpen ();

		htmlTableHeaderCellWrite (
			"Name");

		htmlTableCellWrite (
			ifNullThenEmDash (
				monitorChatUser.getName ()));

		htmlTableCellWrite (
			ifNullThenEmDash (
				userChatUser.getName ()));

		htmlTableRowClose ();

		// info

		htmlTableRowOpen ();

		htmlTableHeaderCellWrite (
			"Info");

		htmlTableCellWrite (
			ifNotNullThenElseEmDash (
				monitorChatUser.getInfoText (),
				() -> monitorChatUser.getInfoText ().getText ()));

		htmlTableCellWrite (
			ifNotNullThenElseEmDash (
				userChatUser.getInfoText (),
				() -> userChatUser.getInfoText ().getText ()));

		htmlTableRowClose ();

		// pic

		htmlTableRowOpen ();

		htmlTableHeaderCellWrite (
			"Pic");

		htmlTableCellWriteHtml (
			ifNotEmptyThenElse (
				monitorChatUser.getChatUserImageList (),

			() -> mediaConsoleLogic.writeMediaThumb100 (
				formatWriter,
				monitorChatUser.getChatUserImageList ().get (0)
					.getMedia ()),

			() -> formatWriter.writeFormat (
				"—")

		));

		htmlTableCellWriteHtml (
			ifNotEmptyThenElse (
				userChatUser.getChatUserImageList (),

			() -> mediaConsoleLogic.writeMediaThumb100 (
				formatWriter,
				userChatUser.getChatUserImageList ().get (0).getMedia ()),

			() -> formatWriter.writeFormat (
				"—")

		));

		htmlTableRowClose ();

		// location

		htmlTableRowOpen ();

		htmlTableHeaderCellWrite (
			"Location");

		htmlTableCellWrite (
			ifNullThenEmDash (
				monitorChatUser.getLocationPlace ()));

		htmlTableCellWrite (
			ifNullThenEmDash (
				userChatUser.getLocationPlace ()));

		htmlTableRowClose ();

		// close table

		htmlTableClose ();

	}

	private
	void renderHistory () {

		htmlHeadingTwoWrite (
			"History");

		// open table

		htmlTableOpenList ();

		// header row

		htmlTableHeaderRowWrite (
			"Time",
			"Message",
			"Monitor");

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

				isNull (
					previousDate)

				|| localDateNotEqual (
					nextDate,
					previousDate)

			) {

				htmlTableRowSeparatorWrite ();

				formatWriter.writeLineFormat (
					"<tr style=\"font-weight: bold\">");

				formatWriter.increaseIndent ();

				formatWriter.writeLineFormat (
					"<td colspan=\"3\">%h</td>",
					timeFormatter.dateStringLong (
						chatTimezone,
						chatMessage.getTimestamp ()));

				htmlTableRowClose ();

				previousDate =
					nextDate;

			}

			formatWriter.writeLineFormat (
				"<tr class=\"%h\">",
				chatMessage.getFromUser () == userChatUser
					? "message-in"
					: "message-out");

			formatWriter.increaseIndent ();

			htmlTableCellWrite (
				timeFormatter.timeString (
					chatTimezone,
					chatMessage.getTimestamp ()));

			htmlTableCellWrite (
				ifNotNullThenElseEmDash (
					chatMessage.getEditedText (),
					() -> spacify (
						chatMessage.getEditedText ().getText ())));

			htmlTableCellWrite (
				ifNotNullThenElseEmDash (
					chatMessage.getSender (),
					() -> chatMessage.getSender ().getUsername ()));

			htmlTableRowClose ();

		}

		htmlTableRowClose ();

	}

}
