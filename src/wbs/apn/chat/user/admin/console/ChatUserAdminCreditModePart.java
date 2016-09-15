package wbs.apn.chat.user.admin.console;

import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.utils.web.HtmlUtils.htmlFormClose;
import static wbs.utils.web.HtmlUtils.htmlFormOpenMethodAction;
import static wbs.utils.web.HtmlUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlUtils.htmlParagraphOpen;

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

		htmlFormOpenMethodAction (
			"post",
			requestContext.resolveLocalUrl (
				"/chatUser.admin.creditMode"));

		htmlTableOpenDetails ();

		htmlTableDetailsRowWriteHtml (
			"Credit mode",
			() -> chatUserCreditModeConsoleHelper.writeSelect (
				"creditMode",
				ifNull (
					requestContext.getForm ("creditMode"),
					chatUser.getCreditMode ().toString ())));

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
