package wbs.clients.apn.chat.user.image.console;

import javax.inject.Inject;

import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.media.console.MediaConsoleLogic;

@PrototypeComponent ("chatUserImageRotatePart")
public
class ChatUserImageRotatePart
	extends AbstractPagePart {

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	MediaConsoleLogic mediaConsoleLogic;

	ChatUserRec chatUser;

	@Override
	public
	void prepare () {

		chatUser =
			chatUserHelper.findRequired (
				requestContext.stuffInteger (
					"chatUserId"));

	}

	@Override
	public
	void renderHtmlBodyContent() {

		if (chatUser.getChatUserImageList ().isEmpty ()) {

			printFormat (
				"<p>No photo to rotate</p>\n");

			return;

		}

		printFormat (
			"<form",
			" method=\"post\"",
			">\n");

		printFormat (
			"<input",
			" type=\"hidden\"",
			" name=\"chatUserImageId\"",
			" value=\"%h\"",
			chatUser.getChatUserImageList ().get (0).getId (),
			">\n");

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>&nbsp;</th>\n",
			"<th>Image</th>\n",
			"<th>Rotation</th>\n",
			"</tr>\n");

		printFormat (
			"<tr>\n",

			"<td><input",
			" type=\"radio\"",
			" name=\"rotate\"",
			" value=\"0\"",
			"></td>\n",

			"<td>%s</td>\n",
			mediaConsoleLogic.mediaThumb100 (
				chatUser.getChatUserImageList ().get (0).getMedia ()),

			"<td>Original image</td>\n",

			"</tr>\n");

		printFormat (
			"<tr>\n",

			"<td><input",
			" type=\"radio\"",
			" name=\"rotate\"",
			" value=\"90\"",
			"></td>\n",

			"<td>%s</td>\n",
			mediaConsoleLogic.mediaThumb100 (
				chatUser.getChatUserImageList ().get (0).getMedia (),
				"90"),

			"<td>90 degrees clockwise</td>\n",

			"</tr>\n");

		printFormat (
			"<tr>\n",

			"<td><input",
			" type=\"radio\"",
			" name=\"rotate\"",
			" value=\"180\"",
			"></td>",

			"<td>%s</td>\n",
			mediaConsoleLogic.mediaThumb100 (
				chatUser.getChatUserImageList ().get (0).getMedia (),
				"180"),

			"<td>180 degrees</td>",

			"</tr>");

		printFormat (
			"<tr>\n",

			"<td><input",
			" type=\"radio\"",
			" name=\"rotate\"",
			" value=\"270\"",
			"></td>\n",

			"<td>%s</td>\n",
			mediaConsoleLogic.mediaThumb100 (
				chatUser.getChatUserImageList ().get (0).getMedia (),
				"270"),

			"<td>90 degrees counter-clockwise</td>\n",

			"</tr>\n");

		printFormat (
			"</table>\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"rotate image\"",
			"></p>\n");

		printFormat (
			"</form>\n");

	}

}
