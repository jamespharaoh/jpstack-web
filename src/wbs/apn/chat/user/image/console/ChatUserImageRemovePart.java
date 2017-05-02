package wbs.apn.chat.user.image.console;

import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPost;

import lombok.NonNull;

import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

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
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			chatUser =
				chatUserHelper.findFromContextRequired (
					transaction);

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			htmlFormOpenPost ();

			if (chatUser.getChatUserImageList ().isEmpty ()) {

				formatWriter.writeLineFormat (
					"<p>No photo to remove.</p>");

			} else {

				htmlParagraphOpen ();

				mediaConsoleLogic.writeMediaContent (
					transaction,
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
					transaction,
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

}
