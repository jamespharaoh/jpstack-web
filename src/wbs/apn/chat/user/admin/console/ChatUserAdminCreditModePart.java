package wbs.apn.chat.user.admin.console;

import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenDetails;

import javax.inject.Named;

import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserType;
import wbs.console.helper.EnumConsoleHelper;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;

@PrototypeComponent ("chatUserAdminCreditModePart")
public
class ChatUserAdminCreditModePart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	@Named
	EnumConsoleHelper <?> chatUserCreditModeConsoleHelper;

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

		if (
			enumEqualSafe (
				chatUser.getType (),
				ChatUserType.monitor)
		) {

			htmlParagraphOpen ();

			formatWriter.writeFormat (
				"This is a monitor and has no credit mode.");

			htmlParagraphClose ();

			return;

		}

		htmlFormOpenPostAction (
			requestContext.resolveLocalUrl (
				"/chatUser.admin.creditMode"));

		htmlTableOpenDetails ();

		htmlTableDetailsRowWriteHtml (
			"Credit mode",
			() -> chatUserCreditModeConsoleHelper.writeSelect (
				"creditMode",
				requestContext.formOrElse (
					"creditMode",
					() -> camelToSpaces (
						chatUser.getCreditMode ().name ()))));

		htmlTableDetailsRowWriteHtml (
			"Actions",
			stringFormat (
				"<input",
				" type=\"submit\"",
				" value=\"change mode\"",
				">"));

		htmlTableClose ();

		htmlFormClose ();

	}

}
