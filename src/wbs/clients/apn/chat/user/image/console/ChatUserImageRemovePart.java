package wbs.clients.apn.chat.user.image.console;

import javax.inject.Inject;

import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.media.console.MediaConsoleLogic;

@PrototypeComponent ("chatUserImageRemovePart")
public
class ChatUserImageRemovePart
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
			chatUserHelper.find (
				requestContext.stuffInt ("chatUserId"));

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<form",
			" method=\"post\"",
			">\n");

		if (chatUser.getChatUserImageList ().isEmpty ()) {

			printFormat (
				"<p>No photo to remove.</p>");

		} else {

			printFormat (
				"<p>%s</p>\n",
				mediaConsoleLogic.mediaContent (
					chatUser.getChatUserImageList ().get (0).getMedia ()));

			printFormat (
				"<p><input",
				" type=\"submit\"",
				" name=\"remove_photo\"",
				" value=\"remove photo\"",
				"></p>\n");

		}

		if (chatUser.getChatUserVideoList ().isEmpty ()) {

			printFormat (
				"<p>No video to remove.</p>");

		} else {

			printFormat (
				"<p>%s</p>\n",
				mediaConsoleLogic.mediaContent (
					chatUser.getChatUserVideoList ().get (0).getMedia ()));

			printFormat (
				"<p><input",
				" type=\"submit\"",
				" name=\"remove_video\"",
				" value=\"remove video\"",
				"></p>\n");

		}

		printFormat (
			"</form>\n");

	}

}
