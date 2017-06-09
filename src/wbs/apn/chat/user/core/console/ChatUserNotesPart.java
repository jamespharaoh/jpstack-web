package wbs.apn.chat.user.core.console;

import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import java.util.List;

import lombok.NonNull;

import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;
import wbs.utils.time.TimeFormatter;

import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserNoteRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

@PrototypeComponent ("chatUserNotesPart")
public
class ChatUserNotesPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ConsoleObjectManager consoleObjectManager;

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	ChatUserNoteConsoleHelper chatUserNoteHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	TimeFormatter timeFormatter;

	// state

	ChatUserRec chatUser;

	List <ChatUserNoteRec> chatUserNotes;

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

			chatUser =
				chatUserHelper.findFromContextRequired (
					transaction);

			chatUserNotes =
				chatUserNoteHelper.find (
					transaction,
					chatUser);

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

			renderCreateForm (
				transaction,
				formatWriter);

			renderHistory (
				transaction,
				formatWriter);

		}

	}

	private
	void renderCreateForm (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderCreateForm");

		) {

			htmlFormOpenPostAction (
				formatWriter,
				requestContext.resolveLocalUrl (
					"/chatUser.notes"));

			htmlHeadingTwoWrite (
				formatWriter,
				"Create note");

			htmlTableOpenDetails (
				formatWriter);

			htmlTableDetailsRowWriteHtml (
				formatWriter,
				"Note",
				stringFormat (
					"<textarea",
					" name=\"note\"",
					">%h</textarea>",
					requestContext.formOrEmptyString (
						"note")));

			htmlTableClose (
				formatWriter);

			formatWriter.writeFormat (
				"<p><input",
				" type=\"submit\"",
				" name=\"createNote\"",
				" value=\"create note\"",
				"></p>\n");

			htmlFormClose (
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
				"Existing notes");

			htmlTableOpenList (
				formatWriter);

			htmlTableHeaderRowWrite (
				formatWriter,
				"Timestamp",
				"Note",
				"User");

			for (
				ChatUserNoteRec chatUserNote
					: chatUserNotes
			) {

				htmlTableRowOpen (
					formatWriter);

				htmlTableCellWrite (
					formatWriter,
					timeFormatter.timestampTimezoneString (
						chatUserLogic.getTimezone (
							chatUser),
						chatUserNote.getTimestamp ()));

				htmlTableCellWrite (
					formatWriter,
					chatUserNote.getText ().getText ());

				consoleObjectManager.writeTdForObjectMiniLink (
					transaction,
					formatWriter,
					chatUserNote.getUser ());

				htmlTableRowClose (
					formatWriter);

			}

			htmlTableClose (
				formatWriter);

		}

	}

}
