package wbs.clients.apn.chat.user.core.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;

import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserNoteObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserNoteRec;
import wbs.clients.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.part.AbstractPagePart;

@PrototypeComponent ("chatUserNotesPart")
public
class ChatUserNotesPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	ConsoleObjectManager consoleObjectManager;

	@Inject
	ChatUserObjectHelper chatUserHelper;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	ChatUserNoteObjectHelper chatUserNoteHelper;

	@Inject
	TimeFormatter timeFormatter;

	// state

	ChatUserRec chatUser;

	List<ChatUserNoteRec> chatUserNotes;

	// implementation

	@Override
	public
	void prepare () {

		chatUser =
			chatUserHelper.find (
				requestContext.stuffInt ("chatUserId"));

		chatUserNotes =
			chatUserNoteHelper.find (
				chatUser);

	}

	@Override
	public
	void renderHtmlBodyContent () {

		// create note

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				"/chatUser.notes"),
			">\n");

		printFormat (
			"<h2>Create note</h2>\n");

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",

			"<th>Note</th>\n",

			"<td>%s</td>\n",
			stringFormat (
				"<textarea name=\"note\">%h</textarea>",
				requestContext.getForm ("note")),

			"</tr>\n");

		printFormat (
			"</table>\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" name=\"createNote\"",
			" value=\"create note\"",
			"></p>\n");

		printFormat (
			"</form>\n");

		// note history

		printFormat (
			"<h2>Existing notes</h2>\n");

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>Timestamp</th>\n",
			"<th>Note</th>\n",
			"<th>User</th>\n",
			"</tr>\n");

		for (ChatUserNoteRec chatUserNote
				: chatUserNotes) {

			printFormat (
				"<tr>\n",

				"<td>%h</td>\n",
				timeFormatter.instantToTimestampString (
					chatUserLogic.timezone (
						chatUser),
					chatUserNote.getTimestamp ()),

				"<td>%h</td> ",
				chatUserNote.getText ().getText (),

				"%s",
				consoleObjectManager.tdForObjectMiniLink (
					chatUserNote.getUser ()),

				"</tr>\n");
		}

		printFormat (
			"</table>\n");

	}

}
