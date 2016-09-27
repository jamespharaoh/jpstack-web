package wbs.apn.chat.user.image.console;

import static wbs.utils.web.HtmlBlockUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenPost;

import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.platform.media.console.MediaConsoleLogic;

@PrototypeComponent ("chatUserImageRemovePart")
public
class ChatUserImageRemovePart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	MediaConsoleLogic mediaConsoleLogic;

	// state

	ChatUserRec chatUser;

	// implementation

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
	void renderHtmlBodyContent () {

		htmlFormOpenPost ();

		if (chatUser.getChatUserImageList ().isEmpty ()) {

			formatWriter.writeLineFormat (
				"<p>No photo to remove.</p>");

		} else {

			htmlParagraphOpen ();

			mediaConsoleLogic.writeMediaContent (
				chatUser.getChatUserImageList ().get (0).getMedia ());

			htmlParagraphClose ();

			htmlParagraphOpen ();

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" name=\"remove_photo\"",
				" value=\"remove photo\"",
				">");

			htmlParagraphClose ();

		}

		if (chatUser.getChatUserVideoList ().isEmpty ()) {

			formatWriter.writeLineFormat (
				"<p>No video to remove.</p>");

		} else {

			htmlParagraphOpen ();

			mediaConsoleLogic.writeMediaContent (
				chatUser.getChatUserVideoList ().get (0).getMedia ());

			htmlParagraphClose ();

			htmlParagraphOpen ();

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" name=\"remove_video\"",
				" value=\"remove video\"",
				">");

			htmlParagraphClose ();

		}

		htmlFormClose ();

	}

}
