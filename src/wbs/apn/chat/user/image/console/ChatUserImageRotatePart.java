package wbs.apn.chat.user.image.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPost;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import lombok.NonNull;

import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.media.console.MediaConsoleLogic;

import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;

@PrototypeComponent ("chatUserImageRotatePart")
public
class ChatUserImageRotatePart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@ClassSingletonDependency
	LogContext logContext;

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

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"renderHtmlBodyContent");

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
			integerToDecimalString (
				chatUser.getChatUserImageList ().get (0).getId ()),
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
			taskLogger,
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
			taskLogger,
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
			taskLogger,
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
			taskLogger,
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
