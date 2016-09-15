package wbs.apn.chat.namednote.console;

import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import wbs.apn.chat.core.model.ChatObjectHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.namednote.model.ChatNoteNameObjectHelper;
import wbs.apn.chat.namednote.model.ChatNoteNameRec;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;

@PrototypeComponent ("chatNoteNamesPart")
public
class ChatNoteNamesPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatObjectHelper chatHelper;

	@SingletonDependency
	ChatNoteNameObjectHelper chatNoteNameHelper;

	// state

	List <ChatNoteNameRec> noteNames;

	// implementation

	@Override
	public
	void prepare () {

		ChatRec chat =
			chatHelper.findRequired (
				requestContext.stuffInteger (
					"chatId"));

		noteNames =
			chatNoteNameHelper.findNotDeleted (
				chat);

	}

	@Override
	public
	void renderHtmlBodyContent () {

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

		for (
			ChatNoteNameRec noteName
				: noteNames
		) {

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
				"<td><input",
				" type=\"submit\"",
				" name=\"noteMoveUp%d\"",
				noteName.getId (),
				" value=\"&uarr;\"",
				">\n");

			printFormat (
				"<input",
				" type=\"submit\"",
				" name=\"noteMoveDown%d\"",
				noteName.getId (),
				" value=\"&darr;\"",
				">\n");

			printFormat (
				"<input",
				" type=\"submit\"",
				" name=\"noteDelete%d\"",
				noteName.getId (),
				" value=\"&times;\"",
				"></td>\n");

			printFormat (
				"</tr>\n");

		}

		printFormat (
			"<tr>\n");

		printFormat (
			"<td><input",
			" type=\"text\"",
			" name=\"noteNameNew\"",
			" value=\"%h\"",
			requestContext.getForm (
				"noteNameNew",
				""),
			"></td>\n");

		printFormat (
			"<td><input",
			" type=\"submit\"",
			" name=\"saveChanges\"",
			" value=\"add new\"",
			"></td>\n");

		printFormat (
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
