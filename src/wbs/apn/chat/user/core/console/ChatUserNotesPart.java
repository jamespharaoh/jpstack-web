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

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

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
	TimeFormatter timeFormatter;

	// state

	ChatUserRec chatUser;

	List <ChatUserNoteRec> chatUserNotes;

	// implementation

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		chatUser =
			chatUserHelper.findFromContextRequired ();

		chatUserNotes =
			chatUserNoteHelper.find (
				chatUser);

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"renderHtmlBodyContent");

		renderCreateForm ();

		renderHistory (
			taskLogger);

	}

	private
	void renderCreateForm () {

		htmlFormOpenPostAction (
			requestContext.resolveLocalUrl (
				"/chatUser.notes"));

		htmlHeadingTwoWrite (
			"Create note");

		htmlTableOpenDetails ();

		htmlTableDetailsRowWriteHtml (
			"Note",
			stringFormat (
				"<textarea",
				" name=\"note\"",
				">%h</textarea>",
				requestContext.formOrEmptyString (
					"note")));

		htmlTableClose ();

		formatWriter.writeFormat (
			"<p><input",
			" type=\"submit\"",
			" name=\"createNote\"",
			" value=\"create note\"",
			"></p>\n");

		htmlFormClose ();

	}

	private
	void renderHistory (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"renderHistory");

		htmlHeadingTwoWrite (
			"Existing notes");

		htmlTableOpenList ();

		htmlTableHeaderRowWrite (
			"Timestamp",
			"Note",
			"User");

		for (
			ChatUserNoteRec chatUserNote
				: chatUserNotes
		) {

			htmlTableRowOpen ();

			htmlTableCellWrite (
				timeFormatter.timestampTimezoneString (
					chatUserLogic.getTimezone (
						chatUser),
					chatUserNote.getTimestamp ()));

			htmlTableCellWrite (
				chatUserNote.getText ().getText ());

			consoleObjectManager.writeTdForObjectMiniLink (
				taskLogger,
				chatUserNote.getUser ());

			htmlTableRowClose ();

		}

		htmlTableClose ();

	}

}
