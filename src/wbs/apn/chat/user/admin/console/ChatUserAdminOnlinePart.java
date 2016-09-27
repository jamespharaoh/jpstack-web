package wbs.apn.chat.user.admin.console;

import static wbs.utils.web.HtmlBlockUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphWrite;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphWriteFormat;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenPostAction;

import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserType;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;

@PrototypeComponent ("chatUserAdminOnlinePart")
public
class ChatUserAdminOnlinePart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	// state

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
	void renderHtmlBodyContent () {

		htmlFormOpenPostAction (
			requestContext.resolveLocalUrl (
				"/chatUser.admin.online"));

		if (chatUser.getOnline ()) {

			htmlParagraphWrite (
				"This user is online");

			htmlParagraphOpen ();

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" name=\"offline\"",
				" value=\"take offline\"",
				">");

			htmlParagraphClose ();

		} else {

			htmlParagraphWrite (
				"This user is offline");

			if (
				chatUser.getType () == ChatUserType.user
				&& chatUser.getFirstJoin () == null
			) {

				htmlParagraphWriteFormat (
					"This user has never been online before, please don't ",
					"bring them online unless you are sure!");

			}

			htmlParagraphOpen ();

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" name=\"online\"",
				" value=\"bring online\"",
				">");

			htmlParagraphClose ();

		}

		htmlFormClose ();

	}

}
