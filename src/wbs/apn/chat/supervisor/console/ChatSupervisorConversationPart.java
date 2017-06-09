package wbs.apn.chat.supervisor.console;

import static wbs.utils.etc.LogicUtils.anyOf;
import static wbs.utils.etc.LogicUtils.ifNotEmptyThenElse;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElseEmDash;
import static wbs.utils.etc.LogicUtils.ifNullThenEmDash;
import static wbs.utils.etc.LogicUtils.referenceNotEqualWithClass;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.spacify;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.time.TimeUtils.localDateNotEqual;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWriteHtml;
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

import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.media.console.MediaConsoleLogic;

import wbs.utils.string.FormatWriter;
import wbs.utils.time.TimeFormatter;

import wbs.apn.chat.contact.console.ChatMessageConsoleHelper;
import wbs.apn.chat.contact.model.ChatMessageRec;
import wbs.apn.chat.core.console.ChatConsoleHelper;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.scheme.model.ChatSchemeRec;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;

@PrototypeComponent ("chatSupervisorConversationPart")
public
class ChatSupervisorConversationPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatConsoleHelper chatHelper;

	@SingletonDependency
	ChatMessageConsoleHelper chatMessageHelper;

	@SingletonDependency
	ChatMiscLogic chatMiscLogic;

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MediaConsoleLogic mediaConsoleLogic;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	ConsoleRequestContext requestContext;

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

			long chatUserId1 =
				requestContext.parameterIntegerRequired (
					"chatUserId1");

			long chatUserId2 =
				requestContext.parameterIntegerRequired (
					"chatUserId2");

			userChatUser =
				chatUserHelper.findRequired (
					transaction,
					chatUserId1);

			monitorChatUser =
				chatUserHelper.findRequired (
					transaction,
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
				new ArrayList<> ();

			chatMessages.addAll (
				chatMessageHelper.findFromTo (
					transaction,
					userChatUser,
					monitorChatUser));

			chatMessages.addAll (
				chatMessageHelper.findFromTo (
					transaction,
					monitorChatUser,
					userChatUser));

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

			renderDetails (
				transaction,
				formatWriter);

			renderHistory (
				transaction,
				formatWriter);

		}

	}

	private
	void renderDetails (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderDetails");

		) {

			htmlTableOpenDetails (
				formatWriter);

			// party

			htmlTableHeaderRowWriteHtml (
				formatWriter,
				"Party",
				"<strong>monitor</strong>",
				"<strong>user</strong>");

			htmlTableRowOpen (
				formatWriter);

			htmlTableHeaderCellWrite (
				formatWriter,
				"User number");

			objectManager.writeTdForObjectMiniLink (
				transaction,
				formatWriter,
				monitorChatUser,
				chat);

			objectManager.writeTdForObjectMiniLink (
				transaction,
				formatWriter,
				userChatUser,
				chat);

			htmlTableRowClose (
				formatWriter);

			// name

			htmlTableRowOpen (
				formatWriter);

			htmlTableHeaderCellWrite (
				formatWriter,
				"Name");

			htmlTableCellWrite (
				formatWriter,
				ifNullThenEmDash (
					monitorChatUser.getName ()));

			htmlTableCellWrite (
				formatWriter,
				ifNullThenEmDash (
					userChatUser.getName ()));

			htmlTableRowClose (
				formatWriter);

			// info

			htmlTableRowOpen (
				formatWriter);

			htmlTableHeaderCellWrite (
				formatWriter,
				"Info");

			htmlTableCellWrite (
				formatWriter,
				ifNotNullThenElseEmDash (
					monitorChatUser.getInfoText (),
					() -> monitorChatUser.getInfoText ().getText ()));

			htmlTableCellWrite (
				formatWriter,
				ifNotNullThenElseEmDash (
					userChatUser.getInfoText (),
					() -> userChatUser.getInfoText ().getText ()));

			htmlTableRowClose (
				formatWriter);

			// pic

			htmlTableRowOpen (
				formatWriter);

			htmlTableHeaderCellWrite (
				formatWriter,
				"Pic");

			htmlTableCellWriteHtml (
				formatWriter,
				ifNotEmptyThenElse (
					monitorChatUser.getChatUserImageList (),

				() -> mediaConsoleLogic.writeMediaThumb100 (
					transaction,
					formatWriter,
					monitorChatUser.getChatUserImageList ().get (0)
						.getMedia ()),

				() -> formatWriter.writeFormat (
					"—")

			));

			htmlTableCellWriteHtml (
				formatWriter,
				ifNotEmptyThenElse (
					userChatUser.getChatUserImageList (),

				() -> mediaConsoleLogic.writeMediaThumb100 (
					transaction,
					formatWriter,
					userChatUser.getChatUserImageList ().get (0).getMedia ()),

				() -> formatWriter.writeFormat (
					"—")

			));

			htmlTableRowClose (
				formatWriter);

			// location

			htmlTableRowOpen (
				formatWriter);

			htmlTableHeaderCellWrite (
				formatWriter,
				"Location");

			htmlTableCellWrite (
				formatWriter,
				ifNullThenEmDash (
					monitorChatUser.getLocationPlace ()));

			htmlTableCellWrite (
				formatWriter,
				ifNullThenEmDash (
					userChatUser.getLocationPlace ()));

			htmlTableRowClose (
				formatWriter);

			// close table

			htmlTableClose (
				formatWriter);

		}

	}

	private
	void renderHistory (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHistory");

		) {

			htmlHeadingTwoWrite (
				formatWriter,
				"History");

			// open table

			htmlTableOpenList (
				formatWriter);

			// header row

			htmlTableHeaderRowWrite (
				formatWriter,
				"Time",
				"Message",
				"Monitor");

			DateTimeZone timezone =
				chatMiscLogic.timezone (
					transaction,
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

					htmlTableRowSeparatorWrite (
						formatWriter);

					formatWriter.writeLineFormat (
						"<tr style=\"font-weight: bold\">");

					formatWriter.increaseIndent ();

					formatWriter.writeLineFormat (
						"<td colspan=\"3\">%h</td>",
						timeFormatter.dateStringLong (
							chatTimezone,
							chatMessage.getTimestamp ()));

					htmlTableRowClose (
						formatWriter);

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
					formatWriter,
					timeFormatter.timeString (
						chatTimezone,
						chatMessage.getTimestamp ()));

				htmlTableCellWrite (
					formatWriter,
					ifNotNullThenElseEmDash (
						chatMessage.getEditedText (),
						() -> spacify (
							chatMessage.getEditedText ().getText ())));

				htmlTableCellWrite (
					formatWriter,
					ifNotNullThenElseEmDash (
						chatMessage.getSender (),
						() -> chatMessage.getSender ().getUsername ()));

				htmlTableRowClose (
					formatWriter);

			}

			htmlTableRowClose (
				formatWriter);

		}

	}

}
