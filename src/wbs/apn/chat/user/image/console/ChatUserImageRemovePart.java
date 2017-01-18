package wbs.apn.chat.user.image.console;

import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPost;

import lombok.NonNull;

import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

import wbs.platform.media.console.MediaConsoleLogic;

import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;

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
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		chatUser =
			chatUserHelper.findFromContextRequired ();

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

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
