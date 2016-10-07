package wbs.apn.chat.user.core.console;

import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenList;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;

import java.util.List;

import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserNoteObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserNoteRec;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.utils.time.TimeFormatter;

@PrototypeComponent ("chatUserNotesPart")
public
class ChatUserNotesPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ConsoleObjectManager consoleObjectManager;

	@SingletonDependency
	ChatUserObjectHelper chatUserHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	ChatUserNoteObjectHelper chatUserNoteHelper;

	@SingletonDependency
	TimeFormatter timeFormatter;

	// state

	ChatUserRec chatUser;

	List <ChatUserNoteRec> chatUserNotes;

	// implementation

	@Override
	public
	void prepare () {

		chatUser =
			chatUserHelper.findRequired (
				requestContext.stuffInteger (
					"chatUserId"));

		chatUserNotes =
			chatUserNoteHelper.find (
				chatUser);

	}

	@Override
	public
	void renderHtmlBodyContent () {

		renderCreateForm ();

		renderHistory ();

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
	void renderHistory () {

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
				chatUserNote.getUser ());

			htmlTableRowClose ();

		}

		htmlTableClose ();

	}

}
