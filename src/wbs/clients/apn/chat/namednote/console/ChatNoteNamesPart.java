package wbs.clients.apn.chat.namednote.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;

import wbs.clients.apn.chat.core.model.ChatObjectHelper;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.namednote.model.ChatNoteNameObjectHelper;
import wbs.clients.apn.chat.namednote.model.ChatNoteNameRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.part.AbstractPagePart;

@PrototypeComponent ("chatNoteNamesPart")
public
class ChatNoteNamesPart
	extends AbstractPagePart {

	@Inject
	ChatObjectHelper chatHelper;

	@Inject
	ChatNoteNameObjectHelper chatNoteNameHelper;

	List<ChatNoteNameRec> noteNames;

	@Override
	public
	void prepare () {

		ChatRec chat =
			chatHelper.find (
				requestContext.stuffInt ("chatId"));

		noteNames =
			chatNoteNameHelper.findNotDeleted (
				chat);

	}

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"chat.settings.noteNames\"",
			">\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" name=\"saveChanges\"",
			" value=\"save changes\"",
			"></p>\n");

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>Name</th>\n",
			"<th>Options</th>\n",
			"</tr>\n");

		for (ChatNoteNameRec noteName
				: noteNames) {

			printFormat (
				"<tr>\n");

			printFormat (
				"<td><input",
				" type=\"text\"",
				" name=\"noteName%d\"",
				noteName.getId (),
				" value=\"%h\"",
				requestContext.getForm (
					stringFormat (
						"noteName%d",
						noteName.getId ()),
					noteName.getName ()),
				"></td>\n");

			printFormat (
				"<td>%s</td>\n",

				stringFormat (
					"<input",
					" type=\"submit\"",
					" name=\"noteMoveUp%d\"",
					noteName.getId (),
					" value=\"&uarr;\"",
					">",

					"<input",
					" type=\"submit\"",
					" name=\"noteMoveDown%d\"",
					noteName.getId (),
					" value=\"&darr;\"",
					">",

					"<input",
					" type=\"submit\"",
					" name=\"noteDelete%d\"",
					noteName.getId (),
					" value=\"&times;\"",
					">"),

				"</tr>\n");

		}

		printFormat (
			"<tr>\n",

			"<td><input",
			" type=\"text\"",
			" name=\"noteNameNew\"",
			" value=\"%h\"",
			requestContext.getForm ("noteNameNew"),
			"></td>\n",

			"<td><input",
			" type=\"submit\"",
			" name=\"saveChanges\"",
			" value=\"add new\"",
			"></td>\n",

			"</tr>\n");

		printFormat (
			"</table>\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" name=\"saveChanges\"",
			" value=\"save changes\"",
			"></p>\n");

		printFormat (
			"</form>\n");

	}

}
