package wbs.apn.chat.user.image.console;

import static wbs.utils.web.HtmlBlockUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenPost;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellOpen;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenList;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;

import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.platform.media.console.MediaConsoleLogic;

@PrototypeComponent ("chatUserImageRotatePart")
public
class ChatUserImageRotatePart
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
	void renderHtmlBodyContent() {

		if (chatUser.getChatUserImageList ().isEmpty ()) {

			formatWriter.writeLineFormat (
				"<p>No photo to rotate</p>");

			return;

		}

		htmlFormOpenPost ();

		formatWriter.writeLineFormat (
			"<input",
			" type=\"hidden\"",
			" name=\"chatUserImageId\"",
			" value=\"%h\"",
			chatUser.getChatUserImageList ().get (0).getId (),
			">");

		htmlTableOpenList ();

		htmlTableHeaderRowWrite (
			"",
			"Image",
			"Rotation");

		// no rotation

		htmlTableRowOpen ();

		htmlTableCellOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" type=\"radio\"",
			" name=\"rotate\"",
			" value=\"0\"",
			">");

		htmlTableCellClose ();

		htmlTableCellOpen ();

		mediaConsoleLogic.writeMediaThumb100 (
			chatUser.getChatUserImageList ().get (0).getMedia ());

		htmlTableCellClose ();

		formatWriter.writeLineFormat (
			"<td>Original image</td>");

		htmlTableRowClose ();

		// rotate 90 degrees clockwise

		htmlTableRowOpen ();

		htmlTableCellOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" type=\"radio\"",
			" name=\"rotate\"",
			" value=\"90\"",
			">");

		htmlTableCellClose ();

		htmlTableCellOpen ();

		mediaConsoleLogic.writeMediaThumb100 (
			chatUser.getChatUserImageList ().get (0).getMedia (),
			"90");

		htmlTableCellClose ();

		formatWriter.writeLineFormat (
			"<td>90 degrees clockwise</td>");


		htmlTableRowClose ();

		// rotate 180 degrees

		htmlTableRowOpen ();

		htmlTableCellOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" type=\"radio\"",
			" name=\"rotate\"",
			" value=\"180\"",
			">");

		htmlTableCellClose ();

		htmlTableCellOpen ();

		mediaConsoleLogic.writeMediaThumb100 (
			chatUser.getChatUserImageList ().get (0).getMedia (),
			"180");

		htmlTableCellClose ();

		formatWriter.writeLineFormat (
			"<td>180 degrees</td>");

		htmlTableRowClose ();

		// rotate 90 degrees counter-clockwise

		htmlTableRowOpen ();

		htmlTableCellOpen ();

		formatWriter.writeFormat (
			"<input",
			" type=\"radio\"",
			" name=\"rotate\"",
			" value=\"270\"",
			">");

		htmlTableCellClose ();

		htmlTableCellOpen ();

		mediaConsoleLogic.writeMediaThumb100 (
			chatUser.getChatUserImageList ().get (0).getMedia (),
			"270");

		htmlTableCellClose ();

		formatWriter.writeLineFormat (
			"<td>90 degrees counter-clockwise</td>");

		htmlTableRowClose ();

		// end form

		htmlParagraphOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" value=\"rotate image\"",
			">");

		htmlParagraphClose ();

		htmlFormClose ();

	}

}
